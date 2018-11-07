import java.awt.Point;
import java.io.Serializable;

public class CarAgentProposal implements Serializable {

	private static final long serialVersionUID = -1978746667247381288L;
	
	Point coords;
	int maxHourlyCost;
	int maxDistance;
	int hoursNeeded;
	ParkingLotAgent.SpotType[] desiredSpots;

	/**
	 * Encapsulates all the information needed for a car agent to send a cfp message to the parking lot agents.
	 * 
	 * @param coords the car agent coordinates
	 * @param maxHourlyCost the max hourly cost tolerated by the car agent
	 * @param maxDistance the max distance tolerated by the car agent
	 * @param hoursNeeded the hours needed by the car agent
	 * @param desiredSpots the spot types desired by the car agent
	 */
	public CarAgentProposal(Point coords, int maxHourlyCost, int maxDistance, int hoursNeeded, ParkingLotAgent.SpotType[] desiredSpots) {
		this.coords = coords;
		this.maxHourlyCost = maxHourlyCost;
		this.maxDistance = maxDistance;
		this.hoursNeeded = hoursNeeded;
		this.desiredSpots = desiredSpots;
	}

	/**
	 * @return the car coords
	 */
	public Point getCoords() {
		return coords;
	}

	/**
	 * @return the max hourly cost tolerated by the car agent
	 */
	public int getMaxHourlyCost() {
		return maxHourlyCost;
	}

	/**
	 * @return the max distance tolerated by the car agent
	 */
	public int getMaxDistance() {
		return maxDistance;
	}

	/**
	 * @return the hours needed by the car agent
	 */
	public int getHoursNeeded() {
		return hoursNeeded;
	}

	/**
	 * @return the spot types desired by the car agent
	 */
	public ParkingLotAgent.SpotType[] getDesiredSpots() {
		return desiredSpots;
	}
}