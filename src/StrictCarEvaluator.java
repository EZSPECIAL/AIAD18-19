import jade.lang.acl.ACLMessage;

public class StrictCarEvaluator extends CarEvaluator {

	// TODO comment
	public StrictCarEvaluator(CarAgent agent, ParkingLotProposal proposal) {
		super(agent, proposal);
	}
	
	@Override
	protected int evaluateProposal(ACLMessage proposeMsg) {
		
		// Check distance to parking lot
		int dist = distanceToParking(agent.getCoords(), proposal.getCoords());
		if(dist > agent.getMaxDistance()) {
			Logger.getInstance().logPrint("Rejecting proposal of " + proposeMsg.getSender().getLocalName() + " because of distance");
			return 0;
		}
		
		// Verify hourly cost according to spot type desired by car agent, priority is REGULAR -> HANDICAP -> LUXURY
		int cost = 0;
		if(agent.isRegularSpot() && proposal.isHasRegular()) {
			cost = proposal.getHourlyCost();
		} else if(agent.isHandicapSpot() && proposal.isHasHandicap()) {
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
}
