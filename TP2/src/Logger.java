import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Logger {

	// Enumerator classes for logging management
	public enum LogMethod {CONSOLE, FILE, BOTH}
	
	private static Logger singleton = new Logger();
	
	// CSV strings
	private static String parkData = "";
	private static String carData = "";
	
	// Log management block
	private static LogMethod logMethod = LogMethod.CONSOLE;
	private static final String logFolder = "logFiles";
	private static final String consoleLogFormat = "%-18s - %s\n";
	private static final String fileLogFormat = "%s - %-18s - %s";
	
	/**
	 * Private constructor for singleton pattern.
	 */
	private Logger() {}
	
	/**
	 * @return the singleton instance of the class
	 */
	public static Logger getInstance( ) {
		return singleton;
	}
	
	/**
	 * Initialises logging management with the specified logging level and method.
	 * 
	 * @param logMethod the desired logging method (file / console / both)
	 */
	public void initLog(LogMethod logMethod) {
		
		createDirIfNotExists("./" + logFolder);
	    Logger.logMethod = logMethod;
	    
	    csvWrite(true);
	}
	
	/**
	 * Creates directory specified by path if it doesn't already exist.
	 * 
	 * @param dirPath the directory path to create
	 */
	public void createDirIfNotExists(String dirPath) {
		
	    File directory = new File(dirPath);
	    if(!directory.exists()) {
	        directory.mkdir();
	    }
	}
	
	/**
	 * Handles logging of messages, can print to console and/or a log file
	 * according to the current settings.
	 * 
	 * @param message string to log
	 */
	public synchronized void logPrint(String message) {

		// Console printing handling
		if(logMethod.equals(LogMethod.CONSOLE) || logMethod.equals(LogMethod.BOTH)) {
			System.out.printf(consoleLogFormat, Thread.currentThread().getName(), message);
			System.out.flush();
		}

		// Log file printing handling
		if(logMethod.equals(LogMethod.CONSOLE)) return;

		createDirIfNotExists("./" + logFolder);

		// Create path for log file
		String filepath = "./" + logFolder + "/log.txt";
		File toCreate = new File(filepath);
		Path toWrite = Paths.get(filepath);

		// Get current day and time and append to log message
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		String toPrint = String.format(fileLogFormat, dateFormat.format(date), Thread.currentThread().getName(), message);
		List<String> lines = Arrays.asList(toPrint);

		// Create file only if it doesn't exist and append new lines to it
		try {
			toCreate.createNewFile();
			Files.write(toWrite, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
		} catch(IOException e) {
			System.out.println("IO exception on log write");
			e.printStackTrace();
		}
	}

	/**
	 * Sets the string for park data for later usage in CSV writing. Park data is static for each run of
	 * the program.
	 * 
	 * @param parkingLotZoning the zoning of the parks (9 elements)
	 * @param parkingLotPricing the price range of the parks divided by zones (27 elements)
	 */
	public synchronized void setParkCSVData(int[] parkingLotZoning, int[] parkingLotPricing) {
		
		for(int i : parkingLotZoning) {
			parkData += i + ", ";
		}
		
		for(int i : parkingLotPricing) {
			parkData += i + ", ";
		}
	}

	/**
	 * Sets the string for car data for later usage in CSV writing. For each call of this function, one should
	 * exist to csvWrite() since car data changes between each round of the negotiation.
	 * 
	 * @param carZone the car's zone
	 * @param carCost the car's maximum hourly cost
	 * @param carDist the car's maximum distance
	 * @param carType the car's behaviour type
	 * @param satisfaction the car's satisfaction rating
	 */
	public synchronized void setCarCSVData(int carZone, int carCost, int carDist, int carType, float satisfaction) {
		carData = carZone + ", " + carCost + ", " + carDist + ", " + carType + ", " + satisfaction;
	}
	
	/**
	 * @return the header to use for the CSV file for data mining
	 */
	private String writeHeader() {
		return "Zone1Parks, Zone2Parks, Zone3Parks, Zone4Parks, Zone5Parks, Zone6Parks, Zone7Parks, Zone8Parks, Zone9Parks, " +
				"CheapZone1, ModerateZone1, PremiumZone1, CheapZone2, ModerateZone2, PremiumZone2, CheapZone3, ModerateZone3, " +
				"PremiumZone3, CheapZone4, ModerateZone4, PremiumZone4, CheapZone5, ModerateZone5, PremiumZone5, CheapZone6, " +
				"ModerateZone6, PremiumZone6, CheapZone7, ModerateZone7, PremiumZone7, CheapZone8, ModerateZone8, PremiumZone8, " + 
				"CheapZone9, ModerateZone9, PremiumZone9, CarZone, CarCost, CarDistance, CarType, Satisfaction";
	}

	/**
	 * Handles logging of agent interaction to a CSV file for later usage in Data Mining techniques.
	 * 
	 * @param isHeader whether to print header or not
	 */
	public synchronized void csvWrite(boolean isHeader) {

		createDirIfNotExists("./" + logFolder);

		// Create path for log file
		String filepath = "./" + logFolder + "/log.csv";
		File toCreate = new File(filepath);
		Path toWrite = Paths.get(filepath);

		List<String> lines;
		if(isHeader) {
			lines = Arrays.asList(writeHeader());
		} else lines = Arrays.asList(parkData + carData);

		// Create file only if it doesn't exist and append new lines to it
		try {
			toCreate.createNewFile();
			Files.write(toWrite, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
		} catch(IOException e) {
			System.out.println("IO exception on log write");
			e.printStackTrace();
		}
	}
}