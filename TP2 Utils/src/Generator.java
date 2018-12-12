import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class Generator {

	private static ArrayList<Point> carCoords = new ArrayList<Point>();
	private static String[] types = {"STRICT", "LOWERCOST", "LOWERDIST", "FLEXIBLE"};

	public static void main(String[] args) {

		Random r = new Random();

		// Init logging
		Logger.getInstance().initLog(Logger.LogMethod.FILE);

		// Select 100 coordinates
		while(carCoords.size() < 100) {

			Point coords = new Point();
			int lBound, hBound;
			do {
				lBound = 0;
				hBound = 15;
				coords.x = generateBetweenBounds(r, lBound, hBound);

				lBound = 0;
				hBound = 15;
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

		for(int i = 0; i < 15; i++) {
			for(int j = 0; j < 15; j++) {
				if(!carCoords.contains(new Point(i, j))) {
					System.out.println(i + ", " + j);
				}
			}
		}
	}

	private static int generateBetweenBounds(Random r, int lBound, int hBound) {
		return r.nextInt(hBound - lBound) + lBound;
	}
}
