import java.awt.Point;

import jade.core.Agent;

public class CarAgent extends Agent {
	
	// Car agent argument indices
	private static final int coordsXI = 0;
	private static final int coordsYI = 1;
	private static final int maxHourlyCostI = 2;
	private static final int maxDistanceI = 3;
	private static final int hoursNeededI = 4;
	private static final int regularSpotI = 5;
	private static final int luxurySpotI = 6;
	private static final int handicapSpotI = 7;
	
	// Car agent paremeters
	private Point coords;
	private int maxHourlyCost;
	private int maxDistance;
	private int hoursNeeded;
	private boolean regularSpot;
	private boolean luxurySpot;
	private boolean handicapSpot;
	
	public void setup() {
		
		// TODO
		Logger.getInstance().logPrint("I'm a car");
		this.initArgs();
		this.logCarAgent();
	}
	
	/**
	 * Initialises car agent with generated randomised parameters.
	 */
	private void initArgs() {
		
		// Fetch arguments and convert to integer
		Object[] args = this.getArguments();

		// Assign values
		coords = new Point((int) args[coordsXI], (int) args[coordsYI]);
		maxHourlyCost = (int) args[maxHourlyCostI];
		maxDistance = (int) args[maxDistanceI];
		hoursNeeded = (int) args[hoursNeededI];
		regularSpot = ((int) args[regularSpotI] != 0) ? true : false;
		luxurySpot = ((int) args[luxurySpotI] != 0) ? true : false;
		handicapSpot = ((int) args[handicapSpotI] != 0) ? true : false;
	}
	
	/**
	 * Logs car agent parameters.
	 */
	private void logCarAgent() {
		
		Logger logger = Logger.getInstance();
		
		logger.logPrint("CAR AGENT ARGS START");
		logger.logPrint("Coords: (" + coords.x + ", " + coords.y + ")");
		logger.logPrint("Max hourly cost: " + maxHourlyCost);
		logger.logPrint("Max distance: " + maxDistance);
		logger.logPrint("Hours needed: " + hoursNeeded);
		logger.logPrint("Regular: " + regularSpot + " Luxury: " + luxurySpot + " Handicap: " + handicapSpot);
		logger.logPrint("CAR AGENT ARGS END" + System.lineSeparator());
	}
}
