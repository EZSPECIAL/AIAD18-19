import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {
	
	// Configuration parameters indices
	private static final int numCarAgentsI = 0;
	private static final int numParkingLotsI = 1;
	private static final int worldSizeXI = 2;
	private static final int worldSizeYI = 3;
	private static final int carMaxHourlyCostLowerBoundI = 4;
	private static final int carMaxHourlyCostUpperBoundI = 5;
	private static final int carMaxDistanceLowerBoundI = 6;
	private static final int carMaxDistanceUpperBoundI = 7;
	private static final int carHoursNeededLowerBoundI = 8;
	private static final int carHoursNeededUpperBoundI = 9;
	private static final int lotSpotsLowerBoundI = 10;
	private static final int lotSpotsUpperBoundI = 11;
	private static final int lotRegularSpotPercentI = 12;
	private static final int lotLuxurySpotPercentI = 13;
	private static final int lotHandicapSpotPercentI = 14;
	private static final int lotHourlyCostLowerBoundI = 15;
	private static final int lotHourlyCostUpperBoundI = 16;
	private static final int lotLuxurySpotCostPercentI = 17;
	private static final int enabledSpotTypesI = 18;
	
	// Configuration parameters
	public static int numCarAgents;
	public static int numParkingLots;
	public static int[] worldSize = new int[2];
	public static int carMaxHourlyCostLowerBound;
	public static int carMaxHourlyCostUpperBound;
	public static int carMaxDistanceLowerBound;
	public static int carMaxDistanceUpperBound;
	public static int carHoursNeededLowerBound;
	public static int carHoursNeededUpperBound;
	public static int lotSpotsLowerBound;
	public static int lotSpotsUpperBound;
	public static int lotRegularSpotPercent;
	public static int lotLuxurySpotPercent;
	public static int lotHandicapSpotPercent;
	public static int lotHourlyCostLowerBound;
	public static int lotHourlyCostUpperBound;
	public static int lotLuxurySpotCostPercent;
	public static boolean regularSpot;
	public static boolean luxurySpot;
	public static boolean handicapSpot;

	/**
	 * Uses the provided filepath as a config file to read agent parameters from.
	 * The parsed information is stored in a number of variables which can be later
	 * used to generate agents with different attributes.
	 * 
	 * @param filepath the filepath to use as config file
	 */
	public static void readConfig(String filepath) throws IOException {
		
		File file = new File(filepath); 

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			System.err.print(filepath + " config file not found!");
			System.exit(1);
		}

		String line;
		while((line = br.readLine()) != null) {
			
			// Lines starting with # are comments
			if(line.startsWith("#")) continue;
			
			// Split arguments and remove whitespace
			String[] args = line.split(",");
			for(int i = 0; i < args.length; i++) {
				args[i] = args[i].trim();
			}

			// Car / Parking lot parameter parsing
			numCarAgents = configToInt(args[numCarAgentsI]);
			numParkingLots = configToInt(args[numParkingLotsI]);
			worldSize[0] = configToInt(args[worldSizeXI]);
			worldSize[1] = configToInt(args[worldSizeYI]);
			carMaxHourlyCostLowerBound = configToInt(args[carMaxHourlyCostLowerBoundI]);
			carMaxHourlyCostUpperBound = configToInt(args[carMaxHourlyCostUpperBoundI]);
			carMaxDistanceLowerBound = configToInt(args[carMaxDistanceLowerBoundI]);
			carMaxDistanceUpperBound = configToInt(args[carMaxDistanceUpperBoundI]);
			carHoursNeededLowerBound = configToInt(args[carHoursNeededLowerBoundI]);
			carHoursNeededUpperBound = configToInt(args[carHoursNeededUpperBoundI]);
			lotSpotsLowerBound = configToInt(args[lotSpotsLowerBoundI]);
			lotSpotsUpperBound = configToInt(args[lotSpotsUpperBoundI]);
			lotRegularSpotPercent = configToInt(args[lotRegularSpotPercentI]);
			lotLuxurySpotPercent = configToInt(args[lotLuxurySpotPercentI]);
			lotHandicapSpotPercent = configToInt(args[lotHandicapSpotPercentI]);
			lotHourlyCostLowerBound = configToInt(args[lotHourlyCostLowerBoundI]);
			lotHourlyCostUpperBound = configToInt(args[lotHourlyCostUpperBoundI]);
			lotLuxurySpotCostPercent = configToInt(args[lotLuxurySpotCostPercentI]);
			
			// Spot types parsing
			char[] spotTypeBooleans = args[enabledSpotTypesI].toCharArray();
			regularSpot = spotTypeBooleans[0] == '0' ? false : true;
			luxurySpot = spotTypeBooleans[1] == '0' ? false : true;
			handicapSpot = spotTypeBooleans[2] == '0' ? false : true;
		}
	}
	
	/**
	 * Prints the config parameters read from the config file. Must be called after {@link #readConfig(String) readConfig} method.
	 */
	public static void printConfig() {
		
		Logger logger = Logger.getInstance();
		
		logger.logPrint("CONFIG FILE PRINT");
		logger.logPrint("Number of car agents: " + numCarAgents);
		logger.logPrint("Number of parking lot agents: " + numParkingLots);
		logger.logPrint("World size: " + worldSize[0] + "x" + worldSize[1]);
		logger.logPrint("Car agent hourly cost lower bound: " + carMaxHourlyCostLowerBound);
		logger.logPrint("Car agent hourly cost upper bound: " + carMaxHourlyCostUpperBound);
		logger.logPrint("Car agent max distance lower bound: " + carMaxDistanceLowerBound);
		logger.logPrint("Car agent max distance upper bound: " + carMaxDistanceUpperBound);
		logger.logPrint("Car agent hours needed lower bound: " + carHoursNeededLowerBound);
		logger.logPrint("Car agent hours needed upper bound: " + carHoursNeededUpperBound);
		logger.logPrint("Parking lot spots lower bound: " + lotSpotsLowerBound);
		logger.logPrint("Parking lot spots upper bound: " + lotSpotsUpperBound);
		logger.logPrint("Parking lot regular spot percent: " + lotRegularSpotPercent + "%");
		logger.logPrint("Parking lot luxury spot percent: " + lotLuxurySpotPercent + "%");
		logger.logPrint("Parking lot handicap spot percent: " + lotHandicapSpotPercent + "%");
		logger.logPrint("Parking lot hourly cost lower bound: " + lotHourlyCostLowerBound);
		logger.logPrint("Parking lot hourly cost upper bound: " + lotHourlyCostUpperBound);
		logger.logPrint("Parking lot luxury spot cost percent: " + lotLuxurySpotCostPercent + "%");
		logger.logPrint("Regular: " + regularSpot + " Luxury: " + luxurySpot + " Handicap: " + handicapSpot);
	}
	
	/**
	 * Parses a string as an integer value and returns it.
	 * 
	 * @param numS string to parse as integer
	 * @return integer value parsed from the string provided
	 */
	private static int configToInt(String numS) {
		
		int num = 0;
		try {
			num = Integer.parseInt(numS);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.err.println("Could not parse config file parameter as integer!");
			System.exit(1);
		}
		return num;
	}
}