import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

public class StrictCarBehavior extends ContractNetInitiator {

	private static final long serialVersionUID = -7251449437912201891L;

	// TODO comment
	public StrictCarBehavior(Agent a, ACLMessage cfp) {
		super(a, cfp);
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

		// TODO evaluate
		
		// Evaluate proposals.
		int bestProposal = -1;
		AID bestProposer = null;
		ACLMessage accept = null;
		Enumeration enumerator = responses.elements();
		while (enumerator.hasMoreElements()) {
			ACLMessage msg = (ACLMessage) enumerator.nextElement();
			if (msg.getPerformative() == ACLMessage.PROPOSE) {
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reply);
				
				ParkingLotProposal parkingLotProposal = getParkingLotProposal(msg);
				
//				int proposal = Integer.parseInt(msg.getContent());
				int proposal = 3;
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
	
	@Override
	protected void handleInform(ACLMessage inform) {
		
		// TODO change inform message
		
		System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
		
		removeFromQueue();
	}
}
