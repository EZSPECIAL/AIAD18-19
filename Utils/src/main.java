import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class main {

	private static ArrayList<Point> carCoords = new ArrayList<Point>();
	private static String[] types = {"STRICT", "LOWERCOST", "LOWERDIST", "FLEXIBLE"};
	public static void main(String[] args) {
		
		Random r = new Random();
		
		// Init logging
		Logger.getInstance().initLog(Logger.LogMethod.BOTH);
		
		while(carCoords.size() < 100) {
			// Select random world coordinates within bounds
			Point coords = new Point();
			int lBound, hBound;
			do {
				lBound = 0;
				hBound = 20;
				coords.x = generateBetweenBounds(r, lBound, hBound);

				lBound = 0;
				hBound = 20;
				coords.y = generateBetweenBounds(r, lBound, hBound);
			} while(carCoords.contains(coords));

			carCoords.add(coords);
		}
		
		for(Point p : carCoords) {
			
			int lBound = 1;
			int hBound = 16;
			int maxHourlyCost = generateBetweenBounds(r, lBound, hBound);
			
			lBound = 5;
			hBound = 21;
			int maxDistance = generateBetweenBounds(r, lBound, hBound);
			
			lBound = 1;
			hBound = 6;
			int hoursNeeded = generateBetweenBounds(r, lBound, hBound);
			
			Logger.getInstance().logPrint("");
		}
	}
	
	private static int generateBetweenBounds(Random r, int lBound, int hBound) {
		return r.nextInt(hBound - lBound) + lBound;
	}
}
