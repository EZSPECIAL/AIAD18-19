import jade.lang.acl.ACLMessage;

public class LowerCostCarEvaluator extends CarEvaluator {

	/**
	 * Constructs a car evaluator object responsible for assigning a value to
	 * parking lot proposals. This evaluator chooses the lowest hourly cost
	 * available without considering the distance to the parking lot.
	 * 
	 * @param agent the evaluating car agent
	 * @param proposal the parking lot proposal
	 */
	public LowerCostCarEvaluator(CarAgent agent, ParkingLotProposal proposal) {
		super(agent, proposal);
	}
	
	@Override
	protected int evaluateProposal(ACLMessage proposeMsg) {
		
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
		return cost;
	}
}