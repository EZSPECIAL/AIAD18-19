import jade.core.Runtime;

import java.io.IOException;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class RunAgents {

	private static final String CONFIG_NAME = "config.csv";
	
	public static void main(String[] args) throws IOException {
		
		// Init logging
		Logger.getInstance().initLog(Logger.LogMethod.BOTH);
		
		// Parse config file
		ConfigParser.readConfig("./" + CONFIG_NAME);
		ConfigParser.printConfig();
		
		// Create agents with randomized parameters
		createAgents();
	}

	// TODO comment
	// TODO generate agents from config parameters
	private static void createAgents() {
		
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
			AgentController parkAgent = container.createNewAgent("ParkingLot", "ParkingLotAgent", agentArgs);
			AgentController carAgent = container.createNewAgent("Car", "CarAgent", agentArgs);
			parkAgent.start();
			carAgent.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.err.println("Exception creating agent!");
			System.exit(1);
		}
	}
}
