import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestGenerator {
	
	public static class GrayImage extends JPanel {
		  public GrayImage() {
		  }

		  public void paint(Graphics g) {
		    Image myImage = new ImageIcon("Island.raw").getImage();
		    BufferedImage bufferedImage = new BufferedImage(1025, 1025, BufferedImage.TYPE_BYTE_BINARY);

		    Graphics gi = bufferedImage.getGraphics();
		    gi.drawImage(myImage, 0, 0, null);
		    gi.dispose();
		    
		    Graphics2D g2d = (Graphics2D) g;
		    g2d.drawImage(bufferedImage, null, 0, 0);
		  }

		  public static void main(String[] args) {
		    JFrame frame = new JFrame();
		    frame.add(new GrayImage());
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.setSize(1025,1025);
		    frame.setLocationRelativeTo(null);
		    frame.setVisible(true);
		  }
		}
	
	public static void main(String[] args) throws IOException {
		//GrayImage.main(args);
//		try (FileReader fr = new FileReader("Island.raw")) {
//			int i;
//			int max = Integer.MIN_VALUE;
//			int min = Integer.MAX_VALUE;
//			Map<Integer, Integer> map = new HashMap<>();
//			StringBuilder sb = new StringBuilder();
//			int count = 0;
//			int spacer = 6;
//			while ((i = fr.read()) > -1) {
//				String num = Integer.toString(i);
//				sb.append(num);
//				for (int s = num.length(); s < spacer; s++) {
//					sb.append(' ');
//				}
//				if (count++ == 1025) {
//					count = 0;
//					sb.append('\n');
//				}
//				
//				//System.out.println(i);
//				Integer e = map.get(i);
//				if (e == null)
//					e = 0;
//				
//				map.put(i, ++e);
//				
//				max = Math.max(max, i);
//				min = Math.min(min, i);
//			}
//			try (FileWriter fw = new FileWriter("Island.txt")) {
//				fw.write(sb.toString());
//				fw.flush();
//			}
//			map.forEach((o, t) -> System.out.println(String.format("%s: %s", o, t)));
//			
//			System.out.println("Max: " + max);
//			System.out.println("Min: " + min);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		
		Voronoi.main("-2572113142372506746", "1000", "30", "test.obj");
		// terran_-2572113142372506746_3500_8
//		for (int i = 1; i <= 10; i++) {
//			System.out.println(i);
//			Random r = new Random();
//			PolyGenerator.main("" + r.nextLong(), "3500", "8");
//		}
	}
	
}