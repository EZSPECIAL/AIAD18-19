import jade.lang.acl.ACLMessage;

public class FlexibleCarEvaluator extends CarEvaluator {

	private static final float margin = 0.3f;
	
	/**
	 * Constructs a car evaluator object responsible for assigning a value to
	 * parking lot proposals. This evaluator chooses the lowest distance and hourly
	 * cost possible by checking the combined value of each proposal. Additionally
	 * this evaluator has a margin up to which it doesn't reject proposals.
	 * 
	 * @param agent the evaluating car agent
	 */
	public FlexibleCarEvaluator(CarAgent agent) {
		super(agent, 3);
	}
	
	@Override
	protected int evaluateProposal(ACLMessage proposeMsg) {
		
		// Check distance to parking lot
		int dist = distanceToParking(agent.getCoords(), proposal.getCoords());
		int agentDist = agent.getMaxDistance();
		float overhead = 0;
		
		if(dist > agentDist) {
			overhead = (float) dist / (float) agentDist - 1;
		}
		
		// Check if distance overhead went above margin
		if(overhead > margin) {
			Logger.getInstance().logPrint("Rejecting proposal of " + proposeMsg.getSender().getLocalName() + " because of distance overhead, margin was " + margin + " but overhead was " + overhead);
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
		
		int agentCost = agent.getMaxHourlyCost();
		if(cost > agentCost) {
			overhead += (float) cost / (float) agentCost - 1;
		}
		
		// Check if distance + cost overhead went above margin
		if(overhead > margin) {
			Logger.getInstance().logPrint("Rejecting proposal of " + proposeMsg.getSender().getLocalName() + " because of distance + cost overhead, margin was " + margin + " but overhead was " + overhead);
			return 0;
		}
		
		return dist + cost;
	}
}
