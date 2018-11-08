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
	 * @param proposal the parking lot proposal
	 */
	public CarEvaluator(CarAgent agent, ParkingLotProposal proposal) {
		this.agent = agent;
		this.proposal = proposal;
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
}
