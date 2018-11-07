import java.io.IOException;
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
	
	private static final int simulatedHours = 5;
	
	ParkingLotAgent agent;
	
	// TODO comment
	public ParkingLotBehavior(Agent a, MessageTemplate mt) {
		super(a, mt);
		this.agent = (ParkingLotAgent) a;
	}

	/**
	 * Checks if at least one of the desired spot types has vacancy in the parking lot.
	 * 
	 * @param proposal the car agent proposal object
	 * @return whether the parking lot as vacancy for the desired spot type
	 */
	private boolean checkVacancy(CarAgentProposal proposal) {
		
		boolean isVacant = false;
		
		for(ParkingLotAgent.SpotType spot : proposal.getDesiredSpots()) {
			
			switch(spot) {
			case REGULAR:
				if(agent.getRegularSpots() > 0) {
					isVacant = true;
				}
				break;
			case LUXURY:
				if(agent.getLuxurySpots() > 0) {
					isVacant = true;
				}
				break;
			case HANDICAP:
				if(agent.getHandicapSpots() > 0) {
					isVacant = true;
				}
				break;
			}
		}
		
		return isVacant;
	}

	/**
	 * Builds a reply to a cfp message by sending a content object with all the info needed for negotiation.
	 * 
	 * @param cfp the call for proposals received by this agent
	 * @return the ACLMessage to reply with
	 */
	private ACLMessage buildProposal(ACLMessage cfp) {
		
		ACLMessage propose = cfp.createReply();
		propose.setPerformative(ACLMessage.PROPOSE);
		
		// Build proposal object
		boolean hasRegular = agent.getRegularSpots() > 0 ? true : false;
		boolean hasLuxury = agent.getLuxurySpots() > 0 ? true : false;
		boolean hasHandicap = agent.getHandicapSpots() > 0 ? true : false;
		ParkingLotProposal proposalTerms = new ParkingLotProposal(agent.getCoords(), agent.getHourlyCost(), agent.getLuxuryCostPercent(), hasRegular, hasLuxury, hasHandicap);
		
		try {
			propose.setContentObject(proposalTerms);
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println("I/O Exception in parking lot agent!");
			System.exit(1);
		}
		
		return propose;
	}
	
	/**
	 * Extracts a car agent proposal object from an ACLMessage sent as cfp.
	 * 
	 * @param msg the ACLMessage to get the object from
	 * @return the car agent proposal
	 */
	private CarAgentProposal getCarAgentProposal(ACLMessage msg) {
		
		CarAgentProposal carAgentProposal = null;
		try {
			carAgentProposal = (CarAgentProposal) msg.getContentObject();
		} catch(UnreadableException e) {
			e.printStackTrace();
			System.err.println("Error occured during the decoding of the content of the ACLMessage!");
			System.exit(1);
		}
		
		return carAgentProposal;
	}
	
	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

		// Get the car agent proposal sent with the cfp
		CarAgentProposal carProposal = getCarAgentProposal(cfp);

		// Log the requested spot types
		String spotTypes = "";
		for(ParkingLotAgent.SpotType spot : carProposal.getDesiredSpots()) {
			spotTypes += spot + " ";
		}
		
		Logger.getInstance().logPrint("Agent " + cfp.getSender().getLocalName() + " requests spot types: " + spotTypes);
		
		// Check whether the parking lot has a spot of one of the types requested
		if(checkVacancy(carProposal)) {
			Logger.getInstance().logPrint("Proposing to " + cfp.getSender().getLocalName());
			return buildProposal(cfp);
		} else {
			Logger.getInstance().logPrint("Refusing to propose to " + cfp.getSender().getLocalName() + " because no parking spots of the requested type are left");
			throw new RefuseException("No empty spots left");
		}
	}

	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		
		String proposer = accept.getSender().getLocalName();
		ParkingLotAgent.SpotType desiredSpot = ParkingLotAgent.SpotType.valueOf(accept.getContent());
		
		Logger.getInstance().logPrint(accept.getSender().getLocalName() + " accepted proposal and selected spot type: " + desiredSpot);
		
		// Update the occupied spots of the parking lot
		switch(desiredSpot) {
		case REGULAR:
			agent.getOccupiedSpots().put(proposer, ParkingLotAgent.SpotType.REGULAR);
			agent.setRegularSpots(agent.getRegularSpots() - 1);
			break;
		case LUXURY:
			agent.getOccupiedSpots().put(proposer, ParkingLotAgent.SpotType.LUXURY);
			agent.setLuxurySpots(agent.getLuxurySpots() - 1);
			break;
		case HANDICAP:
			agent.getOccupiedSpots().put(proposer, ParkingLotAgent.SpotType.HANDICAP);
			agent.setHandicapSpots(agent.getHandicapSpots() - 1);
			break;
		}
		
		// Get the hours needed by the car agent
		CarAgentProposal carProposal = getCarAgentProposal(cfp);
		
		// Schedule a timer to restore the occupied spot
		ParkingLotAgent.getExecutor().schedule(new ParkingLotTimer((ParkingLotAgent) this.getAgent(), agent.getOccupiedSpots(), proposer), carProposal.getHoursNeeded() * simulatedHours, TimeUnit.SECONDS);
		
		ACLMessage inform = accept.createReply();
		inform.setPerformative(ACLMessage.INFORM);
		return inform;
	}

	@Override
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		Logger.getInstance().logPrint(reject.getSender().getLocalName() + " rejected proposal");
	}
}
