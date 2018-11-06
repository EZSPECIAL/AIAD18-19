import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

public class StrictCarBehavior extends ContractNetInitiator {

	private static final long serialVersionUID = -7251449437912201891L;

	// TODO comment
	public StrictCarBehavior(Agent a, ACLMessage cfp) {
		super(a, cfp);
	}

	@Override
	protected void handlePropose(ACLMessage propose, Vector v) {
		// TODO change propose message
		System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
	}
	
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		// TODO change refuse message
		// TODO remove from queue
		System.out.println("Agent " + refuse.getSender().getName() + " refused");
	}
	
	@Override
	protected void handleFailure(ACLMessage failure) {
		
		// TODO change failure message
		// TODO remove from queue
		
		if(failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			System.out.println("Responder does not exist");
		} else {
			System.out.println("Agent "+failure.getSender().getName()+" failed");
		}
		// Immediate failure --> we will not receive a response from this agent
	}
	
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {

		// TODO evaluate
		
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
	
	@Override
	protected void handleInform(ACLMessage inform) {
		
		// TODO change inform message
		
		System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
		
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
}
