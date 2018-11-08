import jade.lang.acl.ACLMessage;

public class LowerDistanceCarEvaluator extends CarEvaluator {

	/**
	 * Constructs a car evaluator object responsible for assigning a value to
	 * parking lot proposals. This evaluator chooses the lowest distance
	 * available without considering the hourly cost of the parking lot.
	 * 
	 * @param agent the evaluating car agent
	 * @param proposal the parking lot proposal
	 */
	public LowerDistanceCarEvaluator(CarAgent agent, ParkingLotProposal proposal) {
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
		
		return dist;
	}
}