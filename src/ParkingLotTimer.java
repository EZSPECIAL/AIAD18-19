import java.util.concurrent.ConcurrentHashMap;

public class ParkingLotTimer implements Runnable {

	private ParkingLotAgent agent;
	private ConcurrentHashMap<String, ParkingLotAgent.SpotType> occupiedSpots;
	private String keyToRemove;
	
	/**
	 * Constructs a parking lot timer responsible for vacating a parking lot spot when
	 * the scheduled time has elapsed. Vacating a parking lot spot means updating the number
	 * of spots available on the parking lot used to construct the timer.
	 * 
	 * @param agent the parking lot agent
	 * @param hashMap the occupied spots hash map
	 * @param keyToRemove the key to remove from the hash map
	 */
	public ParkingLotTimer(ParkingLotAgent agent, ConcurrentHashMap<String, ParkingLotAgent.SpotType> hashMap, String keyToRemove) {
		this.agent = agent;
		this.occupiedSpots = hashMap;
		this.keyToRemove = keyToRemove;
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName(agent.getLocalName() + " Timer");
		updateSpots();
	}
	
	/**
	 * Updates the parking lot spots by restoring an empty spot of the same type that was removed. 
	 */
	private void updateSpots() {
		
		// Get spot type to remove and remove it
		ParkingLotAgent.SpotType spotType = occupiedSpots.get(keyToRemove);
		occupiedSpots.remove(keyToRemove);
		Logger.getInstance().logPrint(keyToRemove + " left " + agent.getLocalName());
		
		// Restore spot of the same type
		switch(spotType) {
		case REGULAR:
			agent.setRegularSpots(agent.getRegularSpots() + 1);
			break;
		case LUXURY:
			agent.setLuxurySpots(agent.getLuxurySpots() + 1);
			break;
		case HANDICAP:
			agent.setHandicapSpots(agent.getHandicapSpots() + 1);
			break;
		}
	}
}
