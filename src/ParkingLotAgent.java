import java.awt.Point;

import jade.core.Agent;

public class ParkingLotAgent extends Agent {

	// Parking lot agent argument indices
	private static final int coordsXI = 0;
	private static final int coordsYI = 1;
	private static final int spotsI = 2;
	private static final int regularPercentI = 3;
	private static final int luxuryPercentI = 4;
	private static final int handicapPercentI = 5;
	private static final int hourlyCostI = 6;
	private static final int luxuryCostPercentI = 7;
	private static final int regularSpotI = 8;
	private static final int luxurySpotI = 9;
	private static final int handicapSpotI = 10;
	
	// Parking lot parameters
	// TODO breakdown spots
	private Point coords;
	private int spots;
	private int regularPercent;
	private int luxuryPercent;
	private int handicapPercent;
	private int hourlyCost;
	private int luxuryCostPercent;
	private boolean regularSpot;
	private boolean luxurySpot;
	private boolean handicapSpot;
	
	public void setup() {
		
		// TODO
		Logger.getInstance().logPrint("I'm a parking lot");
		this.initArgs();
		this.logParkingLotAgent();
	}
	
	/**
	 * Initialises parking lot agent with generated randomised parameters.
	 */
	private void initArgs() {
		
		// Fetch arguments and convert to integer
		Object[] args = this.getArguments();
		
		// Assign values
		coords = new Point((int) args[coordsXI], (int) args[coordsYI]);
		spots = (int) args[spotsI];
		regularPercent = (int) args[regularPercentI];
		luxuryPercent = (int) args[luxuryPercentI];
		handicapPercent = (int) args[handicapPercentI];
		hourlyCost = (int) args[hourlyCostI];
		luxuryCostPercent = (int) args[luxuryCostPercentI];
		regularSpot = ((int) args[regularSpotI] != 0) ? false : true;
		luxurySpot = ((int) args[luxurySpotI] != 0) ? false : true;
		handicapSpot = ((int) args[handicapSpotI] != 0) ? false : true;
	}
	
	/**
	 * Logs parking lot agent parameters.
	 */
	private void logParkingLotAgent() {
		
		Logger logger = Logger.getInstance();
		
		logger.logPrint("PARKING LOT ARGS START");
		logger.logPrint("Coords: (" + coords.x + ", " + coords.y + ")");
		logger.logPrint("Available spots: " + spots);
		logger.logPrint("Regular %: " + regularPercent + " Luxury %: " + luxuryPercent + " Handicap %: " + handicapPercent);
		logger.logPrint("Hourly cost: " + hourlyCost);
		logger.logPrint("Luxury cost modifier: " + luxuryCostPercent);
		logger.logPrint("Regular: " + regularSpot + " Luxury: " + luxurySpot + " Handicap: " + handicapSpot);
		logger.logPrint("PARKING LOT ARGS END" + System.lineSeparator());
	}
}
