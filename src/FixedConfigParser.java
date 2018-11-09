import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FixedConfigParser {

	// Car configuration parameters indices
	private static final int carXCoordI = 0;
	private static final int carYCoordI = 1;
	private static final int carMaxHourlyCostI = 2;
	private static final int carMaxDistanceI = 3;
	private static final int carHoursNeededI = 4;
	private static final int carDesiredSpotTypesI = 5;
	private static final int carEvaluatorI = 6;
	
	// Parking lot configuration parameters indices
	private static final int lotXCoordI = 0;
	private static final int lotYCoordI = 1;
	private static final int lotRegularSpotsI = 2;
	private static final int lotLuxurySpotsI = 3;
	private static final int lotHandicapSpotsI = 4;
	private static final int lotHourlyCostI = 5;
	private static final int lotLuxurySpotCostPercentI = 6;
	private static final int lotEnabledSpotTypesI = 7;

	private static FixedConfigParser singleton = new FixedConfigParser();
	
	/**
	 * Private constructor for singleton pattern.
	 */
	private FixedConfigParser() {}
	
	/**
	 * @return the singleton instance of the class
	 */
	public static FixedConfigParser getInstance( ) {
		return singleton;
	}

	/**
	 * Uses the provided filepath as a config file to read parking lot agent  parameters from.
	 * The parsed information is returned as a list of object arrays which can be later
	 * used to generate agents with those attributes.
	 * 
	 * @param filepath the filepath to use as lot config file
	 * @return the list of arguments for each lot
	 */
	public ArrayList<Object[]> readLotConfig(String filepath) throws IOException {
		
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
		ArrayList<Object[]> lots = new ArrayList<Object[]>();
		while((line = br.readLine()) != null) {
			
			// Lines starting with # are comments
			if(line.startsWith("#")) continue;
			
			// Split arguments and remove whitespace
			String[] args = line.split(",");
			for(int i = 0; i < args.length; i++) {
				args[i] = args[i].trim();
			}

			// Car parameter parsing
			Object[] lotArgs = new Object[11];
			lotArgs[0] = 1;
			lotArgs[lotXCoordI + 1] = configToInt(args[lotXCoordI]);
			lotArgs[lotYCoordI + 1] = configToInt(args[lotYCoordI]);
			lotArgs[lotRegularSpotsI + 1] = configToInt(args[lotRegularSpotsI]);
			lotArgs[lotLuxurySpotsI + 1] = configToInt(args[lotLuxurySpotsI]);
			lotArgs[lotHandicapSpotsI + 1] = configToInt(args[lotHandicapSpotsI]);
			lotArgs[lotHourlyCostI + 1] = configToInt(args[lotHourlyCostI]);
			lotArgs[lotLuxurySpotCostPercentI + 1] = configToInt(args[lotLuxurySpotCostPercentI]);

			// Spot types parsing
			char[] spotTypeBooleans = args[lotEnabledSpotTypesI].toCharArray();
			lotArgs[lotEnabledSpotTypesI  + 1] = Character.getNumericValue(spotTypeBooleans[0]);
			lotArgs[lotEnabledSpotTypesI  + 2] = Character.getNumericValue(spotTypeBooleans[1]);
			lotArgs[lotEnabledSpotTypesI  + 3] = Character.getNumericValue(spotTypeBooleans[2]);

			lots.add(lotArgs);
		}
		
		return lots;
	}
	
	/**
	 * Uses the provided filepath as a config file to read car agent  parameters from.
	 * The parsed information is returned as a list of object arrays which can be later
	 * used to generate agents with those attributes.
	 * 
	 * @param filepath the filepath to use as car config file
	 * @return the list of arguments for each car
	 */
	public ArrayList<Object[]> readCarConfig(String filepath) throws IOException {
		
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
		ArrayList<Object[]> cars = new ArrayList<Object[]>();
		while((line = br.readLine()) != null) {
			
			// Lines starting with # are comments
			if(line.startsWith("#")) continue;
			
			// Split arguments and remove whitespace
			String[] args = line.split(",");
			for(int i = 0; i < args.length; i++) {
				args[i] = args[i].trim();
			}

			// Car parameter parsing
			Object[] carArgs = new Object[10];
			carArgs[0] = 1;
			carArgs[carXCoordI + 1] = configToInt(args[carXCoordI]);
			carArgs[carYCoordI + 1] = configToInt(args[carYCoordI]);
			carArgs[carMaxHourlyCostI + 1] = configToInt(args[carMaxHourlyCostI]);
			carArgs[carMaxDistanceI + 1] = configToInt(args[carMaxDistanceI]);
			carArgs[carHoursNeededI + 1] = configToInt(args[carHoursNeededI]);
			
			// Spot types parsing
			char[] spotTypeBooleans = args[carDesiredSpotTypesI].toCharArray();
			carArgs[carDesiredSpotTypesI + 1] = Character.getNumericValue(spotTypeBooleans[0]);
			carArgs[carDesiredSpotTypesI + 2] = Character.getNumericValue(spotTypeBooleans[1]);
			carArgs[carDesiredSpotTypesI + 3] = Character.getNumericValue(spotTypeBooleans[2]);
			
			// Car evaluator parsing
			switch(args[carEvaluatorI]) {
			case "REGULAR":
				carArgs[carDesiredSpotTypesI + 4] = 0;
				break;
			case "LOWERCOST":
				carArgs[carDesiredSpotTypesI + 4] = 1;
				break;
			case "LOWERDIST":
				carArgs[carDesiredSpotTypesI + 4] = 2;
				break;
			case "FLEXIBLE":
				carArgs[carDesiredSpotTypesI + 4] = 3;
				break;
			default:
				carArgs[carDesiredSpotTypesI + 4] = 0;
				break;
			}
			
			cars.add(carArgs);
		}
		
		return cars;
	}
	
	/**
	 * Parses a string as an integer value and returns it.
	 * 
	 * @param numS string to parse as integer
	 * @return integer value parsed from the string provided
	 */
	private int configToInt(String numS) {
		
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
