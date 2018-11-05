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
	private Point coords;
	private int spots;
	private int regularSpots;
	private int luxurySpots;
	private int handicapSpots;
	private int hourlyCost;
	private int luxuryCostPercent;
	
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
		hourlyCost = (int) args[hourlyCostI];
		luxuryCostPercent = (int) args[luxuryCostPercentI];
		
		// Calculate number of spots per spot type
		boolean regularSpot = ((int) args[regularSpotI] != 0) ? true : false;
		boolean luxurySpot = ((int) args[luxurySpotI] != 0) ? true : false;
		boolean handicapSpot = ((int) args[handicapSpotI] != 0) ? true : false;
		
		float leftOverPercent = 0;
		int spotTypes = 0;
		
		if(regularSpot) {
			spotTypes++;
		} else leftOverPercent += (int) args[regularPercentI];

		if(luxurySpot) {
			spotTypes++;
		} else leftOverPercent += (int) args[luxuryPercentI];
		
		if(handicapSpot) {
			spotTypes++;
		} else leftOverPercent += (int) args[handicapPercentI];
		
		// Split leftover between the remaining 2 spot types
		if(spotTypes == 2) leftOverPercent = leftOverPercent / 2.0f;
		
		int spots = (int) args[spotsI];
		if(regularSpot) {
			regularSpots = (int) Math.round(spots * ((int) args[regularPercentI] + leftOverPercent) / 100.0f);
		}
		
		if(luxurySpot) {
			luxurySpots = (int) Math.round(spots * ((int) args[luxuryPercentI] + leftOverPercent) / 100.0f);
		}
		
		if(handicapSpot) {
			handicapSpots = (int) Math.round(spots * ((int) args[handicapPercentI] + leftOverPercent) / 100.0f);
		}
		
		// Fix rounding errors
		int roundError = regularSpots + luxurySpots + handicapSpots - spots;
		if(roundError > 0) {
			if(regularSpot) regularSpots -= roundError;
			else if(luxurySpot) luxurySpots -= roundError;
			else if(handicapSpot) handicapSpots -= roundError;
		}
	}
	
	/**
	 * Logs parking lot agent parameters.
	 */
	private void logParkingLotAgent() {
		
		Logger logger = Logger.getInstance();
		
		logger.logPrint("PARKING LOT ARGS START");
		logger.logPrint("Coords: (" + coords.x + ", " + coords.y + ")");
		logger.logPrint("Available spots: " + spots);
		logger.logPrint("Regular: " + regularSpots + " Luxury: " + luxurySpots + " Handicap: " + handicapSpots);
		logger.logPrint("Hourly cost: " + hourlyCost);
		logger.logPrint("Luxury cost modifier: " + luxuryCostPercent);
		logger.logPrint("PARKING LOT ARGS END" + System.lineSeparator());
	}
}
