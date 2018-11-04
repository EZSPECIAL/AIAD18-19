import jade.core.Runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class RunAgents {

	private static final String CONFIG_NAME = "config.csv";
	
	public static void main(String[] args) throws IOException {
		
		// Init logging
		Logger.getInstance().initLog(Logger.LogMethod.BOTH);
		
		// Parse config file
		ConfigParser.getInstance().readConfig("./" + CONFIG_NAME);
		ConfigParser.getInstance().printConfig();
		
		// Create agents with randomised parameters
		createAgents();
	}

	// TODO comment
	// TODO generate agents from config parameters
	private static void createAgents() {
		
		Runtime rt = Runtime.instance();
		Profile mainProfile = new ProfileImpl();
		Profile agentProfile = new ProfileImpl();
		
		ContainerController mainContainer = rt.createMainContainer(mainProfile);
		
		// Connects non main container to main container at port 1099
		ContainerController container = rt.createAgentContainer(agentProfile);
		
		// Create agent and pass it reference to Object
		Object reference = new Object();
		Object agentArgs[] = new Object[1];
		agentArgs[0] = reference;
		
		// Generate agent arguments
		// TODO implement in agent constructor
		ArrayList<Integer> carArgs = generateCarAgent();
		ArrayList<Integer> parkingLotArgs = generateParkingLotAgent();
		logCarAgent(carArgs);
		logParkingLotAgent(parkingLotArgs);
		
		// TODO loop
		int numCarAgents = ConfigParser.getInstance().numCarAgents;
		int numParkingLots = ConfigParser.getInstance().numParkingLots;
		
		try {
			AgentController parkAgent = container.createNewAgent("ParkingLot", "ParkingLotAgent", agentArgs);
			AgentController carAgent = container.createNewAgent("Car", "CarAgent", agentArgs);
			parkAgent.start();
			carAgent.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.err.println("Exception creating agent!");
			System.exit(1);
		}
	}
	
	// TODO comment
	// TODO world coords
	private static ArrayList<Integer> generateCarAgent() {
		
		ArrayList<Integer> args = new ArrayList<Integer>();
		Random r = new Random();
		ConfigParser config = ConfigParser.getInstance();
		
		// Select max hourly cost tolerated by car agents
		int lBound = config.carMaxHourlyCostLowerBound;
		int hBound = config.carMaxHourlyCostUpperBound + 1;
		args.add(generateBetweenBounds(r, lBound, hBound));
		
		// Select max distance tolerated by car agents
		lBound = config.carMaxDistanceLowerBound;
		hBound = config.carMaxDistanceUpperBound + 1;
		args.add(generateBetweenBounds(r, lBound, hBound));
		
		// Select hours needed by car agents
		lBound = config.carHoursNeededLowerBound;
		hBound = config.carHoursNeededUpperBound + 1;
		args.add(generateBetweenBounds(r, lBound, hBound));
		
		// Select spot types car agent desires
		if(config.regularSpot) {
			args.add(generateBetweenBounds(r, 0, 2));
		} else args.add(0);
		
		if(config.luxurySpot) {
			args.add(generateBetweenBounds(r, 0, 2));
		} else args.add(0);
		
		if(config.handicapSpot) {
			args.add(generateBetweenBounds(r, 0, 2));
		} else args.add(0);
		
		return args;
	}
	
	// TODO comment
	// TODO world coords
	private static ArrayList<Integer> generateParkingLotAgent() {
	
		ArrayList<Integer> args = new ArrayList<Integer>();
		Random r = new Random();
		ConfigParser config = ConfigParser.getInstance();
		
		// Select number of available parking spots
		int lBound = config.lotSpotsLowerBound;
		int hBound = config.lotSpotsUpperBound + 1;
		args.add(generateBetweenBounds(r, lBound, hBound));
		
		// Add spot distribution
		args.add(config.lotRegularSpotPercent);
		args.add(config.lotLuxurySpotPercent);
		args.add(config.lotHandicapSpotPercent);
		
		// Select hourly cost
		lBound = config.lotHourlyCostLowerBound;
		hBound = config.lotHourlyCostUpperBound + 1;
		args.add(generateBetweenBounds(r, lBound, hBound));
		
		// Add luxury spot cost modifier
		args.add(config.lotLuxurySpotCostPercent);
		
		// Select available spot types
		args.add(config.regularSpot ? 0 : 1);
		args.add(config.luxurySpot ? 0 : 1);
		args.add(config.handicapSpot ? 0 : 1);
		
		return args;
	}
	
	// TODO comment
	private static void logCarAgent(ArrayList<Integer> args) {
		
		Logger logger = Logger.getInstance();
		
		logger.logPrint("CAR AGENT ARGS START");
		logger.logPrint("Max hourly cost: " + args.get(0));
		logger.logPrint("Max distance: " + args.get(1));
		logger.logPrint("Hours needed: " + args.get(2));
		logger.logPrint("Regular: " + ((args.get(3) != 0) ? false : true) + " Luxury: " + ((args.get(4) != 0) ? false : true) + " Handicap: " + ((args.get(5) != 0) ? false : true));
		logger.logPrint("CAR AGENT ARGS END");
	}
	
	// TODO comment
	private static void logParkingLotAgent(ArrayList<Integer> args) {
		
		Logger logger = Logger.getInstance();
		
		
	}
	
	// TODO comment
	private static int generateBetweenBounds(Random r, int lBound, int hBound) {
		return r.nextInt(hBound - lBound) + lBound;
	}
}
