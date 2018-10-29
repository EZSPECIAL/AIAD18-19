import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {
	
	// Configuration parameters indices
	public static final int maxCostLowerBoundI = 0;
	public static final int maxCostUpperBoundI = 1;
	public static final int maxDistanceLowerBoundI = 2;
	public static final int maxDistanceUpperBoundI = 3;
	public static final int enabledSpotTypesI = 4;
	
	// Configuration parameters
	public static int maxCostLowerBound;
	public static int maxCostUpperBound;
	public static int maxDistanceLowerBound;
	public static int maxDistanceUpperBound;
	public static boolean regularSpot;
	public static boolean luxurySpot;
	public static boolean handicapSpot;
	
	// TODO comment
	public static void readConfig(String filepath) throws IOException {
		
		File file = new File(filepath); 

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch(FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

			maxCostLowerBound = configToInt(args[maxCostLowerBoundI]);
			maxCostUpperBound = configToInt(args[maxCostUpperBoundI]);
			maxDistanceLowerBound = configToInt(args[maxDistanceLowerBoundI]);
			maxDistanceUpperBound = configToInt(args[maxDistanceUpperBoundI]);
		}
	}
	
	// TODO comment
	public static void printConfig() {
		
		System.out.println("Max cost lower bound: " + maxCostLowerBound);
		System.out.println("Max cost upper bound: " + maxCostUpperBound);
		System.out.println("Max distance lower bound: " + maxDistanceLowerBound);
		System.out.println("Max distance upper bound: " + maxDistanceUpperBound);
		
	}
	
	// TODO comment
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