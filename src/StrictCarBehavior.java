import java.awt.Point;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

public class StrictCarBehavior extends ContractNetInitiator {

	private static final long serialVersionUID = -7251449437912201891L;

	CarAgent agent;
	
	// TODO comment
	public StrictCarBehavior(Agent a, ACLMessage cfp) {
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
	
	// TODO comment
	private int evaluateProposal(ACLMessage proposeMsg) {
		
		ParkingLotProposal proposal = getParkingLotProposal(proposeMsg);
		
		// Check distance to parking lot
		int dist = distanceToParking(agent.getCoords(), proposal.getCoords());
		if(dist > agent.getMaxDistance()) {
			Logger.getInstance().logPrint("Rejecting proposal of " + proposeMsg.getSender().getLocalName() + " because of distance");
			return 0;
		}
		
		// Verify hourly cost according to spot type desired by car agent, priority is REGULAR -> HANDICAP -> LUXURY
		int cost = 0;
		if((agent.isRegularSpot() && proposal.isHasRegular()) ||
				(agent.isHandicapSpot() && proposal.isHasHandicap())) {
			cost = proposal.getHourlyCost();
		} else if(agent.isLuxurySpot() && proposal.isHasLuxury()) {
			cost = (int) Math.round(proposal.getHourlyCost() * proposal.getLuxuryCostPercent() / 100.0f);
		}
		
		if(cost > agent.getMaxHourlyCost()) {
			Logger.getInstance().logPrint("Rejecting proposal of " + proposeMsg.getSender().getLocalName() + " because of cost");
			return 0;
		}
		return 1;
	}
	
	/**
	 * Returns the rounded euclidean distance between 2 points.
	 * 
	 * @param carCoords the car agent coordinates
	 * @param parkingLotCoords the parking lot agent coordinates
	 * @return the rounded euclidean distance between the 2 agents
	 */
	private int distanceToParking(Point carCoords, Point parkingLotCoords) {
		return (int) carCoords.distance(parkingLotCoords);
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
		
		// TODO can all fail? queue removal won't work
		
		if(failure.getSender().equals(myAgent.getAMS())) Logger.getInstance().logPrint("Responder does not exist!");
		else Logger.getInstance().logPrint(failure.getSender().getLocalName() + " failed!");
	}
	
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {

		// TODO refactor to override evaluation function only
		
		// Evaluate proposals
		int bestProposal = -1;
		AID bestProposer = null;
		ACLMessage accept = null;
		Enumeration enumerator = responses.elements();
		
		while(enumerator.hasMoreElements()) {
			
			ACLMessage msg = (ACLMessage) enumerator.nextElement();
			if(msg.getPerformative() == ACLMessage.PROPOSE) {
				
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reply);

				int proposal = evaluateProposal(msg);
				
				if((proposal != 0) && (proposal > bestProposal)) {
					bestProposal = proposal;
					bestProposer = msg.getSender();
					accept = reply;
				}
			}
		}
		
		// Accept the proposal of the best proposer
		if(accept != null) {
			Logger.getInstance().logPrint("Accepting proposal of " + bestProposer.getLocalName());
			accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
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
