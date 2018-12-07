import java.awt.Point;
import java.io.Serializable;

public class ParkingLotProposal implements Serializable {

	private static final long serialVersionUID = 4944409599949683673L;
	
	Point coords;
	int hourlyCost;
	int luxuryCostPercent;
	boolean hasRegular;
	boolean hasLuxury;
	boolean hasHandicap;
	
	/**
	 * Encapsulates all the information needed for a parking lot agent to send a proposal message to a car agent.
	 * 
	 * @param coords the parking lot agent coordinates
	 * @param hourlyCost the hourly cost of a spot
	 * @param luxuryCostPercent the cost modifier for luxury spots
	 * @param hasRegular whether the parking lot has regular spots
	 * @param hasLuxury whether the parking lot has luxury spots
	 * @param hasHandicap whether the parking lot has handicap spots
	 */
	public ParkingLotProposal(Point coords, int hourlyCost, int luxuryCostPercent, boolean hasRegular, boolean hasLuxury, boolean hasHandicap) {
		this.coords = coords;
		this.hourlyCost = hourlyCost;
		this.luxuryCostPercent = luxuryCostPercent;
		this.hasRegular = hasRegular;
		this.hasLuxury = hasLuxury;
		this.hasHandicap = hasHandicap;
	}

	/**
	 * @return the parking lot coords
	 */
	public Point getCoords() {
		return coords;
	}

	/**
	 * @return the hourly cost of a spot
	 */
	public int getHourlyCost() {
		return hourlyCost;
	}

	/**
	 * @return the cost modifier for luxury spots
	 */
	public int getLuxuryCostPercent() {
		return luxuryCostPercent;
	}

	/**
	 * @return whether the parking lot has regular spots
	 */
	public boolean isHasRegular() {
		return hasRegular;
	}

	/**
	 * @return whether the parking lot has luxury spots
	 */
	public boolean isHasLuxury() {
		return hasLuxury;
	}

	/**
	 * @return whether the parking lot has handicap spots
	 */
	public boolean isHasHandicap() {
		return hasHandicap;
	}
	
	
}
