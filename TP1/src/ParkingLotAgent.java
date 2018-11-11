import java.awt.Point;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ParkingLotAgent extends Agent {

	public enum SpotType {REGULAR, LUXURY, HANDICAP};
	
	private static final long serialVersionUID = -144714414530581727L;
	private static final int numThreads = 50;
	
	// Parking lot agent random config argument indices
	private static final int configTypeI = 0;
	private static final int coordsXI = 1;
	private static final int coordsYI = 2;
	private static final int spotsI = 3;
	private static final int regularPercentI = 4;
	private static final int luxuryPercentI = 5;
	private static final int handicapPercentI = 6;
	private static final int hourlyCostI = 7;
	private static final int luxuryCostPercentI = 8;
	private static final int regularSpotI = 9;
	private static final int luxurySpotI = 10;
	private static final int handicapSpotI = 11;
	
	// Parking lot agent fixed config argument indices
	private static final int regularSpotsI = 3;
	private static final int luxurySpotsI = 4;
	private static final int handicapSpotsI = 5;
	private static final int fixedHourlyCostI = 6;
	private static final int fixedLuxuryCostPercentI = 7;

	// Parking lot constants
	private static final int randomConfig = 0;
	
	// Parking lot parameters
	private Point coords;
	private int spots;
	private int regularSpots;
	private int luxurySpots;
	private int handicapSpots;
	private int hourlyCost;
	private int luxuryCostPercent;
	
	private ConcurrentHashMap<String, SpotType> occupiedSpots = new ConcurrentHashMap<String, SpotType>(16, 0.9f, 1);
	private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(numThreads);
	
	public void setup() {

		if((int) this.getArguments()[configTypeI] == randomConfig) {
			this.initRandomArgs();
		} else this.initFixedArgs();
		this.logParkingLotAgent();
		this.contractNetRespond();
	}
	
	/**
	 * Initiates a ContractNetResponder to handle requests from car agents.
	 */
	private void contractNetRespond() {
		
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new ParkingLotBehavior(this, template));
	}
	
	/**
	 * Initialises parking lot agent with generated randomised parameters.
	 */
	private void initRandomArgs() {
		
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
	 * Initialises parking lot agent with fixed parameters read from config file.
	 */
	private void initFixedArgs() {
		
		// Fetch arguments and convert to integer
		Object[] args = this.getArguments();
		
		// Assign values
		coords = new Point((int) args[coordsXI], (int) args[coordsYI]);
		spots = (int) args[regularSpotsI] + (int) args[luxurySpotsI] + (int) args[handicapSpotsI];
		regularSpots = (int) args[regularSpotsI];
		luxurySpots = (int) args[luxurySpotsI];
		handicapSpots = (int) args[handicapSpotsI];
		hourlyCost = (int) args[fixedHourlyCostI];
		luxuryCostPercent = (int) args[fixedLuxuryCostPercentI];
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

	/**
	 * @return the regular spot number
	 */
	public int getRegularSpots() {
		return regularSpots;
	}

	/**
	 * @return the luxury spot number
	 */
	public int getLuxurySpots() {
		return luxurySpots;
	}

	/**
	 * @return the handicap spot number
	 */
	public int getHandicapSpots() {
		return handicapSpots;
	}
	
	/**
	 * @return the parking lot coords
	 */
	public Point getCoords() {
		return coords;
	}

	/**
	 * @return the hourly cost of a spot
	 */
	public int getHourlyCost() {
		return hourlyCost;
	}

	/**
	 * @return the cost modifier for luxury spots
	 */
	public int getLuxuryCostPercent() {
		return luxuryCostPercent;
	}

	/**
	 * @return the occupied spots map
	 */
	public ConcurrentHashMap<String, SpotType> getOccupiedSpots() {
		return occupiedSpots;
	}

	/**
	 * @return the executor service
	 */
	public static ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}

	/**
	 * @param regularSpots the regular spot number to set
	 */
	public void setRegularSpots(int regularSpots) {
		this.regularSpots = regularSpots;
	}

	/**
	 * @param luxurySpots the luxury spot number to set
	 */
	public void setLuxurySpots(int luxurySpots) {
		this.luxurySpots = luxurySpots;
	}

	/**
	 * @param handicapSpots the handicap spot number to set
	 */
	public void setHandicapSpots(int handicapSpots) {
		this.handicapSpots = handicapSpots;
	}
}
