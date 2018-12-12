import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class Generator {

	private static ArrayList<Point> carCoords = new ArrayList<Point>();
	private static String[] types = {"STRICT", "LOWERCOST", "LOWERDIST", "FLEXIBLE"};
	
	public static void main(String[] args) {
		
		Random r = new Random();
		
		// Init logging
		Logger.getInstance().initLog(Logger.LogMethod.BOTH);
		
		// Select 100 coordinates
		while(carCoords.size() < 100) {

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
		
		// Select 100 arguments for car agents
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
			
			lBound = 0;
			hBound = 4;
			int behaviorIndex = generateBetweenBounds(r, lBound, hBound);
			
			Logger.getInstance().logPrint(p.x + ", " + p.y + ", " + maxHourlyCost + ", " + maxDistance + ", " + hoursNeeded + ", 100, " + types[behaviorIndex]);
		}
	}
	
	private static int generateBetweenBounds(Random r, int lBound, int hBound) {
		return r.nextInt(hBound - lBound) + lBound;
	}
}
