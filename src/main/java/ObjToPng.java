import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ObjToPng {
	
	public static void main(String[] args) {
		try {
			List<PolygonClass> polys = getObjects("voronoi.obj");
			
			for (PolygonClass poly : polys) {
				Polygon p = poly.toPolygon();
				Rectangle2D rec = p.getBounds2D();
				BufferedImage bi = new BufferedImage((int)rec.getWidth(), (int)rec.getHeight(), BufferedImage.TYPE_INT_RGB);
				
				Graphics2D g = bi.createGraphics();
				g.setBackground(Color.BLACK);
				g.setColor(Color.WHITE);
				g.fill(p);
				
				ImageIO.write(bi, "PNG", new File("voronoi\\" + poly.name + ".PNG"));
			}
		  
		} catch (IOException e) {
		  e.printStackTrace();
		}
	}
	
	public static List<PolygonClass> getObjects(String file) throws IOException {
		List<PolygonClass> result = new ArrayList<>();
		List<String> lines = Files.readAllLines(Paths.get(file), Charset.defaultCharset());
		PolygonClass poly = null;
		boolean first = true;
		boolean jump = false;
		for (String line : lines) {
			if (line.startsWith("g")) {
				//Neues Objekt
				if (poly != null) {
					if (!jump) {
						poly.transform();
						result.add(poly);
					} else {
						jump = false;
					}
				}
				first = true;
				poly = new PolygonClass();
				String[] split = line.split(" ");
				poly.name = split[1];
			} else if (line.startsWith("v")) {
				if (first) {
					first = false;
					continue;
				}
				if (jump)
					continue;
				
				//Poly
				String[] split = line.split(" ");
				String xS = split[1];
				String zS = split[3];
				double x = Double.parseDouble(xS) * 1000;
				double z = Double.parseDouble(zS) * 1000;
				poly.addPoint((int)x, (int)z);
				if (x > 20000 || z > 20000)
					jump = true;
			}
		}
		return result;
	}
	
	public static class PolygonClass {
		public String name;
		public List<Integer> x = new ArrayList<>();
		public List<Integer> y = new ArrayList<>();
		public void addPoint(int x, int y) {
			this.x.add(x);
			this.y.add(y);
		}
		public void transform() {
			int minX = 0;
			int minY = 0;
			for (int xx : x) {
				if (xx < minX)
					minX = xx;
			}
			for (int yy : y) {
				if (yy < minY)
					minY = yy;
			}
			if (minX < 0) {
				List<Integer> oldX = x;
				x = new ArrayList<>();
				for (int xx : oldX) {
					x.add(xx + -minX);
				}
			}
			if (minY < 0) {
				List<Integer> oldY = y;
				y = new ArrayList<>();
				for (int yy : oldY) {
					y.add(yy + -minY);
				}
			}
		}
		public Polygon toPolygon() {
			Polygon result = new Polygon();
			for (int i = 0; i < x.size(); i++) {
				result.addPoint(x.get(i), y.get(i));
			}
			return result;
		}
	}
	
}