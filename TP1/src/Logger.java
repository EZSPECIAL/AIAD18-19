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
}