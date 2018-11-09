import jade.core.Runtime;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class RunAgents {

	private static final String RANDOM_CONFIG = "random.csv";
	private static final String CAR_CONFIG = "cars.csv";
	private static final String LOT_CONFIG = "lots.csv";
	private static final boolean isRandom = true;
	
	private static final int regularSpotI = 5;
	private static final int luxurySpotI = 6;
	private static final int handicapSpotI = 7;
	
	// Car agent / Parking lot agent world information
	private static ArrayList<Point> parkingLotCoords = new ArrayList<Point>();
	private static ArrayList<Point> carCoords = new ArrayList<Point>();
	private static ArrayList<AgentController> parkingLotAgents = new ArrayList<AgentController>();
	private static ArrayList<AgentController> carAgents = new ArrayList<AgentController>();
	private static ArrayList<Object[]> carConfigArgs;
	private static ArrayList<Object[]> lotConfigArgs;
	
	// Queue of cars awaiting negotiation
	private static LinkedBlockingQueue<String> waitingCars = new LinkedBlockingQueue<String>();
	
	public static void main(String[] args) throws IOException {
		
		// Init logging
		Logger.getInstance().initLog(Logger.LogMethod.BOTH);
		
		// Parse config file according to selected method
		if(isRandom) {
			RandomConfigParser.getInstance().readConfig("./" + RANDOM_CONFIG);
			RandomConfigParser.getInstance().printConfig();
		} else {
			carConfigArgs = FixedConfigParser.getInstance().readCarConfig("./" + CAR_CONFIG);
			lotConfigArgs = FixedConfigParser.getInstance().readLotConfig("./" + LOT_CONFIG);
		}
		
		// Create agents with randomised parameters
		createAgents();
	}

	/**
	 * Creates and starts all the agents of the program. Generates car and parking lot agents
	 * with randomised values before starting all of them.
	 */
	private static void createAgents() {
		
		Runtime rt = Runtime.instance();
		Profile mainProfile = new ProfileImpl();
		Profile agentProfile = new ProfileImpl();
		
		rt.createMainContainer(mainProfile);
		
		// Connects non main container to main container at port 1099
		ContainerController container = rt.createAgentContainer(agentProfile);
		
		try {
			createCarAgents(container);
			createParkingLotAgents(container);
			
			for(AgentController agent : parkingLotAgents) {
				agent.start();
			}
			
			for(AgentController agent : carAgents) {
				agent.start();
			}
			
		} catch(StaleProxyException e) {
			e.printStackTrace();
			System.err.println("Exception creating agent!");
			System.exit(1);
		}
	}
	
	/**
	 * Creates car agents using randomised parameters or fixed parameters read from config file
	 * according to the command line options. Adds cars to the negotiation queue.
	 * 
	 * @param container agent container to create agent in
	 */
	private static void createCarAgents(ContainerController container) throws StaleProxyException {
		
		// Generate car agents with randomised parameters
		if(isRandom) {
			for(int i = 0; i < RandomConfigParser.getInstance().numCarAgents; i++) {

				// Generate car agent arguments
				ArrayList<Integer> carArgs = generateCarAgent();

				Object[] carArgsObj = new Object[carArgs.size()];
				for(int j = 0; j < carArgs.size(); j++) {
					carArgsObj[j] = carArgs.get(j);
				}

				int carID = carAgents.size();
				carAgents.add(container.createNewAgent("Car" + carID, "CarAgent", carArgsObj));
				waitingCars.add("Car" + carID);
			}
		// Use fixed parameters read from config file
		} else {
			
			for(int i = 0; i < carConfigArgs.size(); i++) {
				
				Object[] carArgsObj = carConfigArgs.get(i);
				carCoords.add(new Point((int) carArgsObj[1], (int) carArgsObj[2])); // TODO hardcoded index
				
				int carID = carAgents.size();
				carAgents.add(container.createNewAgent("Car" + carID, "CarAgent", carArgsObj));
				waitingCars.add("Car" + carID);
			}
		}
	}
	
	/**
	 * Creates a parking lot agent with randomised parameters.
	 * 
	 * @param container agent container to create agent in
	 */
	private static void createParkingLotAgents(ContainerController container) throws StaleProxyException {
		
		for(int i = 0; i < RandomConfigParser.getInstance().numParkingLots; i++) {
			
			// Generate car agent arguments
			ArrayList<Integer> parkingLotArgs = generateParkingLotAgent();
			
			Object[] parkingLotArgsObj = new Object[parkingLotArgs.size()];
			for(int j = 0; j < parkingLotArgs.size(); j++) {
				parkingLotArgsObj[j] = parkingLotArgs.get(j);
			}

			parkingLotAgents.add(container.createNewAgent("ParkingLot" + parkingLotAgents.size(), "ParkingLotAgent", parkingLotArgsObj));
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
		RandomConfigParser config = RandomConfigParser.getInstance();
		
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
		int spotTypesSelected = 0;
		if(config.regularSpot) {
			int val = generateBetweenBounds(r, 0, 2);
			spotTypesSelected += val;
			args.add(val);
		} else args.add(0);
		
		if(config.luxurySpot) {
			int val = generateBetweenBounds(r, 0, 2);
			spotTypesSelected += val;
			args.add(val);
		} else args.add(0);
		
		if(config.handicapSpot) {
			int val = generateBetweenBounds(r, 0, 2);
			spotTypesSelected += val;
			args.add(val);
		} else args.add(0);

		// Force spot type if randomiser selected none
		if(spotTypesSelected == 0) {
			if(config.regularSpot) {
				args.set(regularSpotI, 1);
			} else if(config.luxurySpot) {
				args.set(luxurySpotI, 1);
			} else if(config.handicapSpot) {
				args.set(handicapSpotI, 1);
			}
		}
		
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
		RandomConfigParser config = RandomConfigParser.getInstance();
		
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
		args.add(config.regularSpot ? 1 : 0);
		args.add(config.luxurySpot ? 1 : 0);
		args.add(config.handicapSpot ? 1 : 0);
		
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

	/**
	 * @return the parking lot agents
	 */
	public static synchronized ArrayList<AgentController> getParkingLotAgents() {
		return parkingLotAgents;
	}
	/**
	 * @return the car agents
	 */
	public static synchronized ArrayList<AgentController> getCarAgents() {
		return carAgents;
	}

	/**
	 * @return the cars queueing up for a parking spot
	 */
	public static LinkedBlockingQueue<String> getWaitingCars() {
		return waitingCars;
	}
}