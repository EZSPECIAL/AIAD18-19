import java.util.concurrent.TimeUnit;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

public class ParkingLotBehavior extends ContractNetResponder {

	private static final long serialVersionUID = -3463116286662565961L;
	
	ParkingLotAgent agent;
	
	// TODO comment
	public ParkingLotBehavior(Agent a, MessageTemplate mt) {
		super(a, mt);
		this.agent = (ParkingLotAgent) a;
	}

	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		
		// TODO send proposal to car
		// TODO is car request fulfillable?
		
		CarAgentProposal carProposal = null;
		try {
			carProposal = (CarAgentProposal) cfp.getContentObject();
		} catch(UnreadableException e) {
			e.printStackTrace();
			System.err.println("Error occured during the decoding of the content of the ACLMessage!");
			System.exit(1);
		}
		
		System.out.println("Agent " + agent.getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + carProposal.getHoursNeeded());
		//System.out.println("Agent " + agent.getLocalName() + ": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
		int proposal = 3;
		if(proposal > 2) {
			// We provide a proposal
			System.out.println("Agent " + agent.getLocalName() + ": Proposing "+proposal);
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			propose.setContent(String.valueOf(proposal));
			return propose;
		} else {
			// We refuse to provide a proposal
			System.out.println("Agent " + agent.getLocalName() + ": Refuse");
			throw new RefuseException("evaluation-failed");
		}
	}

	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		
		// TODO car accepted
		
		System.out.println("Agent " + agent.getLocalName() + ": Proposal accepted");

		// TODO hardcoded values
		String proposer = cfp.getSender().getLocalName();
		
		agent.getOccupiedSpots().put(proposer, ParkingLotAgent.SpotType.REGULAR);
		agent.setRegularSpots(agent.getRegularSpots() - 1);
		
		String timeValue = proposer.replaceAll("\\D+","");
		ParkingLotAgent.getExecutor().schedule(new ParkingLotTimer((ParkingLotAgent) this.getAgent(), agent.getOccupiedSpots(), proposer), Integer.parseInt(timeValue) + 1, TimeUnit.SECONDS);
		
		ACLMessage inform = accept.createReply();
		inform.setPerformative(ACLMessage.INFORM);
		return inform;
	}

	@Override
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		System.out.println("Agent " + agent.getLocalName() + ": Proposal rejected");
		// TODO car rejected
	}
}
