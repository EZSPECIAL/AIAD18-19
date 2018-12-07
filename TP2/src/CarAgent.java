import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class CarAgent extends Agent {
	
	private static final long serialVersionUID = -3463154810903197092L;
	
	// Car agent argument indices
	private static final int configTypeI = 0;
	private static final int coordsXI = 1;
	private static final int coordsYI = 2;
	private static final int maxHourlyCostI = 3;
	private static final int maxDistanceI = 4;
	private static final int hoursNeededI = 5;
	private static final int regularSpotI = 6;
	private static final int luxurySpotI = 7;
	private static final int handicapSpotI = 8;
	private static final int evaluatorI = 9;
	
	// Car constants
	private static final int randomConfig = 0;
	
	// Car agent parameters
	private Point coords;
	private int maxHourlyCost;
	private int maxDistance;
	private int hoursNeeded;
	private boolean regularSpot;
	private boolean luxurySpot;
	private boolean handicapSpot;
	
	private String localName;
	private CarEvaluator eval;
	
	public void setup() {
		
		// Check type of config file
		this.initArgs(((int) this.getArguments()[configTypeI] == randomConfig) ? true : false);

		this.logCarAgent();
		
		try {
			this.contractNetInitiate();
		} catch(StaleProxyException e) {
			e.printStackTrace();
			System.err.println("Exception initiating contract net!");
			System.exit(1);
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.err.println("Thread interrupted!");
			System.exit(1);
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println("I/O Exception in car agent!");
			System.exit(1);
		}
	}

	/**
	 * Checks whether the current agent is at the head of the queue. If not, the
	 * agent waits until its turn. Implemented using wait() and notifyAll() methods.
	 */
	private void checkQueue() throws InterruptedException {
		
		LinkedBlockingQueue<String> queue = RunAgents.getWaitingCars();

		// Block in queue if current head isn't the same ID as this agent
		if(!queue.peek().equals(localName)) {

			Logger.getInstance().logPrint("waiting in queue");
			synchronized(queue) {
				while(!queue.peek().equals(localName)) {
					queue.wait();
				}
			}
		}

		Logger.getInstance().logPrint("my turn");
	}

	/**
	 * Initiates a ContractNetInitiator to start negotiation with the parking lots.
	 */
	private void contractNetInitiate() throws StaleProxyException, InterruptedException, IOException {
		
		// Wait for turn
		checkQueue();
		
		// Call for proposals
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		
		// Add all parking lots as receivers
		ArrayList<AgentController> parkingLots = RunAgents.getParkingLotAgents();
		for(AgentController agent : parkingLots) {
			msg.addReceiver(new AID(agent.getName(), AID.ISGUID));
		}
		
		// Message protocol and content
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		
		// Build desired spot types array
		ArrayList<ParkingLotAgent.SpotType> desiredSpotsList = new ArrayList<ParkingLotAgent.SpotType>();
		if(regularSpot) {
			desiredSpotsList.add(ParkingLotAgent.SpotType.REGULAR);
		}
		if(luxurySpot) {
			desiredSpotsList.add(ParkingLotAgent.SpotType.LUXURY);
		}
		if(handicapSpot) {
			desiredSpotsList.add(ParkingLotAgent.SpotType.HANDICAP);
		}
		
		ParkingLotAgent.SpotType[] desiredSpots = desiredSpotsList.toArray(new ParkingLotAgent.SpotType[0]);
		
		// Set proposal parameters
		msg.setContentObject(new CarAgentProposal(coords, maxHourlyCost, maxDistance, hoursNeeded, desiredSpots));

		addBehaviour(new CarBehavior(this, msg));
	}
	
	/**
	 * Initialises car agent. May randomise parameters or use the provided values
	 * if using fixed config parameters.
	 * 
	 * @param isRandom whether the agent generation is randomised or not
	 */
	private void initArgs(boolean isRandom) {
		
		localName = this.getAID().getLocalName();
		
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
		
		// Select car evaluator
		eval = selectCarEvaluator(isRandom);
	}

	/**
	 * Selects a car evaluator to use in the negotiation phase. May randomise
	 * the choice or use a provided value if using fixed config parameters.
	 * 
	 * @param isRandom whether the agent generation is randomised or not
	 * @return the evaluator object to use
	 */
	private CarEvaluator selectCarEvaluator(boolean isRandom) {
		
		Random r = new Random();
		int evalIndex = isRandom ? r.nextInt(4) : (int) this.getArguments()[evaluatorI]; // 0 to 3
		
		String currName = Thread.currentThread().getName();
		switch(evalIndex) {
		case 0:
			Thread.currentThread().setName("Strict" + currName);
			return new StrictCarEvaluator(this);
		case 1:
			Thread.currentThread().setName("LowerCost" + currName);
			return new LowerCostCarEvaluator(this);
		case 2:
			Thread.currentThread().setName("LowerDistance" + currName);
			return new LowerDistanceCarEvaluator(this);
		case 3:
			Thread.currentThread().setName("Flexible" + currName);
			return new FlexibleCarEvaluator(this);
		default:
			return new StrictCarEvaluator(this);
		}
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

	/**
	 * @return the car coords
	 */
	public Point getCoords() {
		return coords;
	}

	/**
	 * @return the max hourly cost tolerated by the car agent
	 */
	public int getMaxHourlyCost() {
		return maxHourlyCost;
	}

	/**
	 * @return the max distance tolerated by the car agent
	 */
	public int getMaxDistance() {
		return maxDistance;
	}

	/**
	 * @return the hours needed by the car agent
	 */
	public int getHoursNeeded() {
		return hoursNeeded;
	}

	/**
	 * @return whether the car agent desires a regular spot
	 */
	public boolean isRegularSpot() {
		return regularSpot;
	}

	/**
	 * @return whether the car agent desires a luxury spot
	 */
	public boolean isLuxurySpot() {
		return luxurySpot;
	}

	/**
	 * @return whether the car agent desires an handicap spot
	 */
	public boolean isHandicapSpot() {
		return handicapSpot;
	}

	/**
	 * @return the evaluator object used to evaluate proposals
	 */
	public CarEvaluator getEval() {
		return eval;
	}
}
