import jade.core.Runtime;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class RunAgents {

	private static final String CONFIG_NAME = "config.csv";
	private static final ArrayList<Point> parkingLotCoords = new ArrayList<Point>();
	private static final ArrayList<Point> carCoords = new ArrayList<Point>();

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
	private static void createAgents() {
		
		Runtime rt = Runtime.instance();
		Profile mainProfile = new ProfileImpl();
		Profile agentProfile = new ProfileImpl();
		
		ContainerController mainContainer = rt.createMainContainer(mainProfile);
		
		// Connects non main container to main container at port 1099
		ContainerController container = rt.createAgentContainer(agentProfile);
		
		// TODO
		int numCarAgents = ConfigParser.getInstance().numCarAgents;
		int numParkingLots = ConfigParser.getInstance().numParkingLots;
		
		// Generate car agent arguments
		ArrayList<Integer> carArgs = generateCarAgent();
		
		Object[] carArgsObj = new Object[carArgs.size()];
		for(int i = 0; i < carArgs.size(); i++) {
			carArgsObj[i] = carArgs.get(i);
		}
		
		// Generate parking lot agent arguments
		ArrayList<Integer> parkingLotArgs = generateParkingLotAgent();
		
		Object[] parkingLotArgsObj = new Object[parkingLotArgs.size()];
		for(int i = 0; i < parkingLotArgs.size(); i++) {
			parkingLotArgsObj[i] = parkingLotArgs.get(i);
		}
		
		try {
			AgentController parkAgent = container.createNewAgent("ParkingLot", "ParkingLotAgent", parkingLotArgsObj);
			AgentController carAgent = container.createNewAgent("Car", "CarAgent", carArgsObj);
			parkAgent.start();
			carAgent.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.err.println("Exception creating agent!");
			System.exit(1);
		}
	}
	
	/**
	 * Generates arguments for a car agent based on the ranges read from a config file.
	 * World coordinate populating is done by checking whether the coordinates collide, no attempt
	 * at giving reasonable space between agent locations is done.
	 * 
	 * @return ArrayList of generated values
	 */
	private static ArrayList<Integer> generateCarAgent() {
		
		ArrayList<Integer> args = new ArrayList<Integer>();
		Random r = new Random();
		ConfigParser config = ConfigParser.getInstance();
		
		// Select random world coordinates within bounds
		Point coords = new Point();
		int lBound, hBound;
		do {
			lBound = 0;
			hBound = config.worldSize[0];
			coords.x = generateBetweenBounds(r, lBound, hBound);
			
			lBound = 0;
			hBound = config.worldSize[1];
			coords.y = generateBetweenBounds(r, lBound, hBound);
		} while(carCoords.contains(coords) || parkingLotCoords.contains(coords));
		
		args.add(coords.x);
		args.add(coords.y);
		carCoords.add(coords);
		
		// Select max hourly cost tolerated by car agents
		lBound = config.carMaxHourlyCostLowerBound;
		hBound = config.carMaxHourlyCostUpperBound + 1;
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
	
	/**
	 * Generates arguments for a parking lot agent based on the ranges read from a config file.
	 * World coordinate populating is done by checking whether the coordinates collide, no attempt
	 * at giving reasonable space between agent locations is done.
	 * 
	 * @return ArrayList of generated values
	 */
	private static ArrayList<Integer> generateParkingLotAgent() {
	
		ArrayList<Integer> args = new ArrayList<Integer>();
		Random r = new Random();
		ConfigParser config = ConfigParser.getInstance();
		
		// Select random world coordinates within bounds
		Point coords = new Point();
		int lBound, hBound;
		do {
			lBound = 0;
			hBound = config.worldSize[0];
			coords.x = generateBetweenBounds(r, lBound, hBound);
			
			lBound = 0;
			hBound = config.worldSize[1];
			coords.y = generateBetweenBounds(r, lBound, hBound);
		} while(carCoords.contains(coords) || parkingLotCoords.contains(coords));
		
		args.add(coords.x);
		args.add(coords.y);
		parkingLotCoords.add(coords);
		
		// Select number of available parking spots
		lBound = config.lotSpotsLowerBound;
		hBound = config.lotSpotsUpperBound + 1;
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
	
	/**
	 * Generates a pseudorandom integer between the provided bounds.
	 * Lower bound inclusive, upper bound exclusive.
	 * 
	 * @param r pseudorandom number generator
	 * @param lBound lower bound to use
	 * @param hBound upper bound to use
	 * @return a random integer value between the specified bounds
	 */
	private static int generateBetweenBounds(Random r, int lBound, int hBound) {
		return r.nextInt(hBound - lBound) + lBound;
	}
}
