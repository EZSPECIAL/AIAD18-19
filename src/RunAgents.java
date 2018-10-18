import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class RunAgents {

	public static void main(String[] args) {
		
		Runtime rt = Runtime.instance();
		Profile mainProfile = new ProfileImpl();
		Profile agentProfile = new ProfileImpl();
		
		ContainerController mainContainer = rt.createMainContainer(mainProfile);
		
		// Connects non main container to main container at port 1099
		ContainerController container = rt.createAgentContainer(agentProfile);
		
		// Create agent and pass it reference to Object
		Object reference = new Object();
		Object agentArgs[] = new Object[1];
		agentArgs[0] = reference;
		
		try {
			AgentController agent = container.createNewAgent("ParkingLot", "ParkingLotAgent", agentArgs);
			agent.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Exception creating agent!");
			System.exit(1);
		}
	}

}
