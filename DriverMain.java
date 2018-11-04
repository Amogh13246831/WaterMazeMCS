package watermazeData;

public class DriverMain {

	public static void main(String[] args) {
		
		Simulation s = new Simulation(1);
		s.runSimulation();
		s.storeData("testsim.txt");
		Simulation t = Simulation.readData("testsim.txt");
		t.runSimulation();
	}

}
