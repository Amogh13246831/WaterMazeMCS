package backEnd;

public class DriverMain {

	public static void main(String[] args) {
		
		Simulation s = new Simulation(1, new PathType[] {new PathType(13, 3)});//, new PathType(0, 6), new PathType(6, 0), new PathType(14,4)});
		//Simulation t = null;
		int maxiters = 10000;
		for(int i=0; i<maxiters; i++) {
			s.runSimulation();
			//s.storeData("testsim.txt");
			//t = Simulation.readData("testsim.txt");
			//t.runSimulation();
		}
		s.printInfo();
		System.out.println("\nScore: " + s.successes + "/" + maxiters);
		
	}
}
