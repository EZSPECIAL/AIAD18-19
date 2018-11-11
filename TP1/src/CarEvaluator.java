import java.awt.Point;

import jade.lang.acl.ACLMessage;

public abstract class CarEvaluator {

	CarAgent agent;
	ParkingLotProposal proposal;
	
	/**
	 * Constructs a car evaluator object responsible for assigning a value to
	 * parking lot proposals.
	 * 
	 * @param agent the evaluating car agent
	 */
	public CarEvaluator(CarAgent agent) {
		this.agent = agent;
	}

	/**
	 * Returns an integer value representing how good a parking lot agent
	 * proposal. 0 is used as an automatic rejection.
	 * 
	 * @param proposeMsg the parking lot agent proposal
	 * @return the value associated with the proposal
	 */
	protected abstract int evaluateProposal(ACLMessage proposeMsg);
	
	/**
	 * Returns the rounded euclidean distance between 2 points.
	 * 
	 * @param carCoords the car agent coordinates
	 * @param parkingLotCoords the parking lot agent coordinates
	 * @return the rounded euclidean distance between the 2 agents
	 */
	protected int distanceToParking(Point carCoords, Point parkingLotCoords) {
		return (int) carCoords.distance(parkingLotCoords);
	}

	/**
	 * @param proposal the parking lot proposal to set
	 */
	public void setProposal(ParkingLotProposal proposal) {
		this.proposal = proposal;
	}
}
