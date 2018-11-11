import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

public class CarBehavior extends ContractNetInitiator {

	private static final long serialVersionUID = -7251449437912201891L;

	CarAgent agent;

	/**
	 * Constructs a car behaviour responsible for handling the ContractNetInitiator role
	 * in a FIPA ContractNet protocol. Sends a cfp message signaling the desired spot types
	 * to which parking lot agents respond with their proposals provided a spot of the desired type
	 * is available. The car agent then evaluates the proposals and chooses to accept 0 or 1 of them.
	 * 
	 * @param a the car agent
	 * @param cfp the call for proposals message associated with this initiator
	 */
	public CarBehavior(Agent a, ACLMessage cfp) {
		super(a, cfp);
		this.agent = (CarAgent) a;
	}

	/**
	 * Removes the head of the queue from the waiting cars queue. It should be the
	 * car that just negotiated. 
	 */
	private void removeFromQueue() {
	
		LinkedBlockingQueue<String> queue = RunAgents.getWaitingCars();
		
		// Remove head from queue since it was the agent that just negotiated
		try {
			queue.take();
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.err.println("Thread interrupted!");
			System.exit(1);
		}
		
		// Check if all negotiations have ended
		if(queue.size() == 0) {
			Logger.getInstance().logPrint("Waiting cars queue emptied, no more negotiations to do!");
		}
		
		// Notify all threads waiting for queue
		synchronized(queue) {
			queue.notifyAll();
		}
	}
	
	/**
	 * Extracts a parking lot proposal object from an ACLMessage sent as proposal message.
	 * 
	 * @param msg the ACLMessage to get the object from
	 * @return the parking lot proposal object
	 */
	private ParkingLotProposal getParkingLotProposal(ACLMessage msg) {
		
		ParkingLotProposal parkingLotProposal = null;
		try {
			parkingLotProposal = (ParkingLotProposal) msg.getContentObject();
		} catch(UnreadableException e) {
			e.printStackTrace();
			System.err.println("Error occured during the decoding of the content of the ACLMessage!");
			System.exit(1);
		}
		
		return parkingLotProposal;
	}

	/**
	 * Selects a spot type by checking which spot types the car agent wants and
	 * the available ones in a specific parking lot agent.
	 * 
	 * @param proposal the parking lot agent proposal
	 * @return the parking lot spot type enumerator the car agent has chosen
	 */
	private ParkingLotAgent.SpotType selectSpot(ParkingLotProposal proposal) {
		
		if(agent.isRegularSpot() && proposal.isHasRegular()) {
			return ParkingLotAgent.SpotType.REGULAR;
		} else if(agent.isHandicapSpot() && proposal.isHasHandicap()) {
			return ParkingLotAgent.SpotType.HANDICAP;
		} else if(agent.isLuxurySpot() && proposal.isHasLuxury()) {
			return ParkingLotAgent.SpotType.LUXURY;
		}
		
		return ParkingLotAgent.SpotType.REGULAR;
	}
	
	@Override
	protected void handlePropose(ACLMessage propose, Vector v) {
		
		ParkingLotProposal parkingLotProposal = getParkingLotProposal(propose);
		
		Logger.getInstance().logPrint(propose.getSender().getLocalName() + " proposed -" +
		" coords: (" + parkingLotProposal.getCoords().x + ", " + parkingLotProposal.getCoords().y + ");"
		+ " hourly cost: " + parkingLotProposal.getHourlyCost() + ";"
		+ " luxury cost modifier: " + parkingLotProposal.getLuxuryCostPercent() + ";"
		+ " hasRegular: " + parkingLotProposal.isHasRegular() + ";"
		+ " hasLuxury: " + parkingLotProposal.isHasLuxury() + ";"
		+ " hasHandicap: " + parkingLotProposal.isHasHandicap());
	}
	
	@Override
	protected void handleRefuse(ACLMessage refuse) {

		Logger.getInstance().logPrint(refuse.getSender().getLocalName() + " refused to propose");
		removeFromQueue();
	}
	
	@Override
	protected void handleFailure(ACLMessage failure) {

		if(failure.getSender().equals(myAgent.getAMS())) Logger.getInstance().logPrint("Responder does not exist!");
		else Logger.getInstance().logPrint(failure.getSender().getLocalName() + " failed!");
	}
	
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {

		int bestProposal = Integer.MAX_VALUE;
		int bestProposerI = 0;
		AID bestProposer = null;
		ACLMessage accept = null;
		
		// Evaluate proposals
		for(int i = 0; i < responses.size(); i++) {

			ACLMessage msg = (ACLMessage) responses.get(i);
			if(msg.getPerformative() == ACLMessage.PROPOSE) {
				
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reply);

				// Evaluate proposal using the agent's evaluator (personality)
				CarEvaluator eval = agent.getEval();
				eval.setProposal(getParkingLotProposal(msg));
				int proposal = eval.evaluateProposal(msg);
				
				// Lower values are better since cost and distance are both best when minimised
				if((proposal != 0) && (proposal < bestProposal)) {
					Logger.getInstance().logPrint("New best proposal by " + msg.getSender().getLocalName() + " evaluated at " + proposal);
					bestProposal = proposal;
					bestProposer = msg.getSender();
					bestProposerI = i;
					accept = reply;
				} else Logger.getInstance().logPrint("Worse proposal by " + msg.getSender().getLocalName() + " evaluated at " + proposal);
			}
		}
		
		// Accept the proposal of the best proposer
		if(accept != null) {
			Logger.getInstance().logPrint("Accepting proposal of " + bestProposer.getLocalName());
			accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			accept.setContent(selectSpot(getParkingLotProposal((ACLMessage) responses.get(bestProposerI))).name());
		// All proposals rejected, exit queue
		} else {
			Logger.getInstance().logPrint("Rejected all proposals");
			removeFromQueue();
		}
	}
	
	@Override
	protected void handleInform(ACLMessage inform) {

		Logger.getInstance().logPrint("negotiation with " + inform.getSender().getLocalName() + " ended");
		removeFromQueue();
	}
}
