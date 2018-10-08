import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.flowpowered.math.vector.Vector2l;

import lombok.AllArgsConstructor;
import lombok.Data;

public class PolyGenerator {
	
	//java -jar Generator <seed> <rasterSize> <rasterRadius>
	public static void main(String... args) {
		if (args.length < 3) {
			System.out.println("Usage: <seed> <rasterSize> <rasterRadius>");
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
		
		//RasterRadius 1 = 4 durchl�ufe (x * 2)^2 
		//RasterRadius 2 = 16 durchl�ufe 
		//RasterRadius 3 = 36 durchl�ufe
		
		int runs = (int)Math.pow(rasterRadius * 2, 2);
		System.out.println(runs + " durchläufe.");
		
		List<Chunk> polys = new ArrayList<Chunk>();
		
		//Bestehende Datei auslesen
		String rawName = "terran_" + seed + "_" + rasterSize + "_" + rasterRadius;
		File file = new File(rawName + ".obj");
		if (file.exists()) {
			System.out.println(rawName + " bereits vorhanden. beginne einlesen");
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
					Chunk poly = new Chunk(new Vector2l(x, z), Vector2l.ZERO);
					polys.add(poly);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (polys.size() >= runs) {
				System.out.println("Es k�nnen keine neuen Polygone hinzugef�gt werden, die vorhandene Datei beinhaltet genug/zu viele.");
				return;
			}
			System.out.println("Datei erfolgreich eingelesen.");
		}
		
		
		
		int count = 0;
		
		for (long step = 1; step <= rasterRadius; step++) {
			for (long rasterX = -step; rasterX < step; rasterX++) {
				for (long rasterZ = -step; rasterZ < step; rasterZ++) {
					
					if (count < polys.size()) {
						//UEberspringen der Polygone
						count++;
						continue;
					}
					
					long x = (rasterX * rasterSize);
					long z = (rasterZ * rasterSize);
					
					Chunk poly = new Chunk(new Vector2l(x, z), new Vector2l(rasterX, rasterZ));
					if (!polys.contains(poly)) {
						polys.add(poly);
					}
					count++;
				}
			}
		}

		//Ein bisschen Random
		Random random = new Random(seed);
		
		int rSize = (int)(rasterSize - (rasterSize / 10));
		
		//removeIf missbrauchen für den Transform
		polys.removeIf(p -> p.transform(rSize, random));
		
		//Create .obj fuer blender
		try (FileWriter fw = new FileWriter(file, false)) {
			fw.append("mtllib test.mtl\r\no " + rawName + "\r\n");
			for (Chunk p : polys) {
				fw.append(p.toVortexString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Data
	@AllArgsConstructor
	public static class Chunk {
		private Vector2l poly;
		private Vector2l coord;
		
		public boolean transform(int rSize, Random random) {
			Vector2l transform;
			int r = random.nextInt();
			
			if (r % 3 == 0) {
				// Remove
				return true;
			}
			
			if (random.nextBoolean()) {
				double x = random.nextInt(rSize);
				double y = random.nextInt(rSize);
				transform = new Vector2l(x, y);
			} else {
				//Keine Änderung
				return false;
			}
			
			if (random.nextBoolean()) {
				poly = poly.sub(transform);
			} else {
				poly = poly.add(transform);
			}
			return false;
		}
		
		public String toVortexString() {
			return String.format("v %s 0.0 %s\r\n", (float)poly.getX() / 1000, (float)poly.getY() / 1000);
		}
		
		public List<Vector2l> getNeighbor() {
			List<Vector2l> result = new ArrayList<>();
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					if (x == 0 && y == 0) {
						continue; //Mitte = selber = uninteresant
					}
					result.add(poly.add(x, y));
				}
			}
			return result;
		}
	}
	
}