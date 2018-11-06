import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
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
	
	// TODO comment
	private void contractNetInitiate() throws StaleProxyException, InterruptedException {
		
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
		msg.setContent("hello parking");

		// TODO can divide?
		addBehaviour(new ContractNetInitiator(this, msg) {
			
			private static final long serialVersionUID = 8330790807988522110L;

			protected void handlePropose(ACLMessage propose, Vector v) {
				System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Agent "+refuse.getSender().getName()+" refused");
			}
			
			protected void handleFailure(ACLMessage failure) {
				if(failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Responder does not exist");
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed");
				}
				// Immediate failure --> we will not receive a response from this agent
			}
			
			protected void handleAllResponses(Vector responses, Vector acceptances) {

				// Evaluate proposals.
				int bestProposal = -1;
				AID bestProposer = null;
				ACLMessage accept = null;
				Enumeration e = responses.elements();
				while (e.hasMoreElements()) {
					ACLMessage msg = (ACLMessage) e.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						acceptances.addElement(reply);
						int proposal = Integer.parseInt(msg.getContent());
						if (proposal > bestProposal) {
							bestProposal = proposal;
							bestProposer = msg.getSender();
							accept = reply;
						}
					}
				}
				// Accept the proposal of the best proposer
				if(accept != null) {
					System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				}						
			}
			
			protected void handleInform(ACLMessage inform) {
				System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
				
				LinkedBlockingQueue<String> queue = RunAgents.getWaitingCars();
				
				// Remove head from queue since it was the agent that just negotiated
				try {
					queue.take();
				} catch(InterruptedException e) {
					e.printStackTrace();
					System.err.println("Thread interrupted!");
					System.exit(1);
				}
				
				// Notify all threads waiting for queue
				synchronized(queue) {
					queue.notifyAll();
				}
			}
		});
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
}
