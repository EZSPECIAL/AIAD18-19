import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
	private static final int coordsXI = 0;
	private static final int coordsYI = 1;
	private static final int maxHourlyCostI = 2;
	private static final int maxDistanceI = 3;
	private static final int hoursNeededI = 4;
	private static final int regularSpotI = 5;
	private static final int luxurySpotI = 6;
	private static final int handicapSpotI = 7;
	
	// Car agent paremeters
	private Point coords;
	private int maxHourlyCost;
	private int maxDistance;
	private int hoursNeeded;
	private boolean regularSpot;
	private boolean luxurySpot;
	private boolean handicapSpot;
	
	private String localName;
	
	public void setup() {
		
		this.initArgs();
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
	 * Initialises car agent with generated randomised parameters.
	 */
	private void initArgs() {
		
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
}
