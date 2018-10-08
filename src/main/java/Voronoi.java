
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import kn.uni.voronoitreemap.datastructure.OpenList;
import kn.uni.voronoitreemap.diagram.PowerDiagram;
import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;

public class Voronoi {
	
	private static Map<String, String> rev = new HashMap<>();
	static {
		rev.put(String.format("g %s.%s", 18338, -7617 ), "g 0068d682-5857-4321-95f8-2cf5c0e8b577");
		rev.put(String.format("g %s.%s", 21385, -7210 ), "g a19adc7d-d300-4036-bfc7-c517168d541d");
		rev.put(String.format("g %s.%s", 17995, -4849 ), "g 095938e6-7ca0-4f60-bfe5-c446eb881219");
		rev.put(String.format("g %s.%s", 19835, -4003 ), "g 846c8d17-776e-436d-94c0-667d7bb843f3");
		rev.put(String.format("g %s.%s", 17771, 8006 ), "g e1947edd-8650-4029-8e71-e9a9c52bec7b");
		rev.put(String.format("g %s.%s", 19893, 7691 ), "g 51681a53-8d5b-498e-94e5-f3020f678673");
		rev.put(String.format("g %s.%s", 19929, 10134 ), "g 02e79b2b-3e42-43aa-8241-8f4c725f0ebe");
		rev.put(String.format("g %s.%s", 16612, 11284 ), "g f9479468-e479-4f41-8bbe-cd8cc86ad57b");
		rev.put(String.format("g %s.%s", 20556, 12629 ), "g 9a49e204-ce70-4c6d-8b49-a20a77e36696");
		rev.put(String.format("g %s.%s", 19108, 15117 ), "g 96f9f88b-726e-43e1-9e23-516ce744cc1c");
		rev.put(String.format("g %s.%s", 16414, -1442 ), "g e286ff58-2ddf-4178-a523-ed1dc6c1274f");
		rev.put(String.format("g %s.%s", 16426, 14137 ), "g b9d4d00e-5ac3-4091-9268-d3998cf84e7c");
		rev.put(String.format("g %s.%s", 18914, -1753 ), "g eee4f0c9-5d45-4ac1-b3d1-7ce398172281");
	}

	public static void main(String... args) throws IOException {
		if (args.length < 4) {
			System.out.println("usage: <seed> <rasterSize> <rasterRadius> <file>");
			return;
		}
		
		long seed;
		long rasterSize;
		long rasterRadius;
		try {
			seed = Long.valueOf(args[0]);
			rasterSize = Long.valueOf(args[1]);
			rasterRadius = Long.valueOf(args[2]);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		PowerDiagram diagram = new PowerDiagram();
		
		// create a root polygon which limits the voronoi diagram.
		// here it is just a rectangle.
		PolygonSimple rootPolygon = new PolygonSimple();
		long size = rasterSize * rasterRadius;
		rootPolygon.add(-size, -size);
		rootPolygon.add(size, -size);
		rootPolygon.add(size, size);
		rootPolygon.add(-size, size);
		
		// normal list based on an array
		OpenList sites = new OpenList();
		
		String fileName = args[3];
		File file = new File(fileName);
		if (file.exists()) {
			System.out.println(fileName + " wird eingelesen");
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (!line.startsWith("v "))
						continue; //Alles, was kein Vortex ist, Ignorieren...
					
					String[] split = line.split(" ");
					if (split.length != 4) {
						System.err.println("Can not read: " + line);
						continue;
					}
					long x, z;
					try {
						x = (long)(Double.valueOf(split[1]) * 1000);
						z = (long)(Double.valueOf(split[3]) * 1000);
					} catch (NumberFormatException e) {
						System.err.println("Can not encode: " + line);
						continue;
					}
					
					Site site = new Site(x, z);
					//site.setWeight(random.nextInt(rasterSize));
					sites.add(site);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Datei erfolgreich eingelesen.");
		} else {
			System.out.println("Datei \"" + fileName + "\" nicht gefunden.");
			return;
		}

		// set the list of points (sites), necessary for the power diagram
		diagram.setSites(sites);
		// set the clipping polygon, which limits the power voronoi diagram
		diagram.setClipPoly(rootPolygon);
		
		// do the computation
		diagram.computeDiagram();
		
		// for each site we can no get the resulting polygon of its cell.
		// note that the cell can also be empty, in this case there is no
		// polygon for the corresponding site.

		List<String> out = new ArrayList<>();
		
		long vc = 0;
		List<Vector2d> centerList = new ArrayList<>();
		List<Vector2i> lines = new ArrayList<>();
		List<Vector3i> faces = new ArrayList<>();
		
		Map<Double, Integer> winkel = new HashMap<>();
		
		boolean png = false;
		
		for (int i = 0; i < sites.size; i++) {
			Site site = sites.array[i];
			PolygonSimple polygon = site.getPolygon();
			if (polygon != null) {
				
				Point2D center = polygon.getCentroid();
				String groupName = String.format("g %s.%s", (int)center.getX(), (int)center.getY() );
				if (rev.containsKey(groupName)) {
					groupName = rev.get(groupName);
				}
				out.add(groupName);
				
				List<Point2D> vector = new ArrayList<>();
				vector.add(center);
				long c = ++vc; //Manuelles hinzufügen = Manuelles hochzählen
				
				double[] XA = polygon.getXPoints();
				double[] YA = polygon.getYPoints();
				double xLast = XA[polygon.length - 1]; //Beginnend mit Last Point
				double yLast = YA[polygon.length - 1]; //^^
				long last = 0, first = vc + 1;
				for (int s = 0; s < polygon.length; s++) {
					double x = XA[s];
					double y = YA[s];
					
					int nextPos = s + 1;
					if (nextPos == polygon.length) //Maximum überschritten, ersten punkt nehmen
						nextPos = 0;
					
					double xNext = XA[nextPos];
					double yNext = YA[nextPos];

					Vector2d p1 = transformQ(xLast, yLast, x, y);
					Vector2d p2 = transformR(xLast, yLast, x, y);
					Vector2d p3 = transformQ(x, y, xNext, yNext);
					Vector2d p4 = transformR(x, y, xNext, yNext);
					
					Vector3d l1 = p1.toVector3(1).cross(p2.toVector3(1));
					Vector3d l2 = p3.toVector3(1).cross(p4.toVector3(1));
					
					//Winkel
					double w = Math.round( winkel(x, y, xNext, yNext) / 10 ) * 10;
					w = w % 90;
					if (w < 0)
						w = -w;
					
					Integer wc = winkel.get(w);
					if (wc == null)
						wc = 0;
					wc++;
					
					winkel.put(w, wc);
					//Winkel End
					
					Vector2d sch = schnittpunkt(l1.cross(l2));
					
					double sx = sch.getX();
					double sy = sch.getY();
					
					vector.add(new Point2D(sx, sy));
					last = vc++;
					
					lines.add(new Vector2i(last, vc)); //Border Lines
					faces.add(new Vector3i(last, vc, c));
					lines.add(new Vector2i(c, vc)); //Line To Center7
					
					//Last Point
					xLast = XA[s];
					yLast = YA[s];
				}
				//Vectoren speichern
				vector.stream().map(p -> String.format("v %s 0.0 %s", ((float)((int)p.getX()) / 1000), ((float)((int)p.getY()) / 1000))).forEach(out::add);
				
				lines.add(new Vector2i(vc, first)); //Schliessende Linie
				faces.add(new Vector3i(vc, first, c)); //Schliessendes Face
				
				centerList.add(new Vector2d(center.x / 1000, center.y / 1000));
				
				if (png) {
					double minX = Arrays.stream(polygon.getXPoints()).limit(polygon.length).sorted().min().getAsDouble();
					double minY = Arrays.stream(polygon.getYPoints()).limit(polygon.length).sorted().min().getAsDouble();
					double maxX = Arrays.stream(polygon.getXPoints()).limit(polygon.length).sorted().max().getAsDouble();
					double maxY = Arrays.stream(polygon.getYPoints()).limit(polygon.length).sorted().max().getAsDouble();
//					if (minX < -20_000 || minY < -20_000 || maxX > 20_000 || maxY > 20_000 || minX == maxX || minY == maxY)
//						continue;
					
					polygon.translate(-minX, -minY);
					
					Rectangle2D rec = polygon.getBounds2D();
					BufferedImage bi = new BufferedImage((int)rec.getWidth() + 1, (int)rec.getHeight() + 1, BufferedImage.TYPE_INT_RGB);
					
					Graphics2D g = bi.createGraphics();
					g.setBackground(Color.BLACK);
					g.setColor(Color.WHITE);
					g.fill(polygon);

					String name = String.format("%s.%s", (int)center.getX(), (int)center.getY() );

					UUID uuid = UUID.randomUUID();
					
					Path sektorDir = Paths.get("sektor", uuid.toString());
					if (!Files.exists(sektorDir))
						Files.createDirectories(sektorDir);
					
					String csv = String.format("%s;%s;%s\n", uuid.toString(), (int)center.getX(), (int)center.getY() );
					ImageIO.write(bi, "PNG", new File(sektorDir.toFile(), name + ".png"));
					Files.write(Paths.get(sektorDir.getParent().toString(), "sektor.csv"), csv.getBytes(Charset.defaultCharset()), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				}
			}
		}
		lines.stream().map(v -> String.format("l %s %s", v.getX(), v.getY())).forEach(out::add); //Alle linien speichern
		faces.stream().map(v -> String.format("f %s %s %s", v.getX(), v.getY(), v.getZ())).forEach(out::add);
		
		winkel.forEach((d, c) -> System.out.println(String.format("%s: %s", d, c)));

		try (FileWriter fw = new FileWriter("voronoi.obj")) {
			fw.write("o voronoi.001\n");
			for (String line : out) {
				fw.write(line);
				fw.write('\n');
			}
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (FileWriter fw = new FileWriter("center.obj")) {
			fw.write("o center.001\n");
			List<String> outList = new ArrayList<>();
			centerList.stream().map(p -> String.format("v %s 0.0 %s", (p.getX()), (p.getY()))).forEach(outList::add);
			for (String line : outList) {
				fw.write(line);
				fw.write('\n');
			}
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Vector2d transformQ(double xA, double yA, double xB, double yB) {
		Vector2d A = new Vector2d(xA, yA);
		Vector2d B = new Vector2d(xB, yB);
		
		Vector2d sub = A.sub(B);
		double distance = A.distance(B);
		
		double sx = xA + (50 * sub.getY()/distance);
		double sy = yA - (50 * sub.getX()/distance);
		
		return new Vector2d(sx, sy);
	}
	
	public static Vector2d transformR(double xA, double yA, double xB, double yB) {
		Vector2d A = new Vector2d(xA, yA);
		Vector2d B = new Vector2d(xB, yB);
		
		Vector2d sub = B.sub(A);
		double distance = B.distance(A);
		
		double sx = xB - (50 * sub.getY()/distance);
		double sy = yB + (50 * sub.getX()/distance);
		
		return new Vector2d(sx, sy);
	}
	
	public static double winkel(double xA, double yA, double xB, double yB) {
		Vector2d A = new Vector2d(xA, yA);
		Vector2d B = new Vector2d(xB, yB);
		
		Vector2d sub = A.sub(B);
		
		return Math.toDegrees(Math.atan2(sub.getY(), sub.getX()));
	}
	
	public static Vector2d schnittpunkt(Vector3d s) {
//		sch[0] = (double)s[0] / (double)s[2];
//		sch[1] = (double)s[1] / (double)s[2];
		
		double x = s.getX() / s.getZ();
		double y = s.getY() / s.getZ();
		return new Vector2d(x, y);
	}

}