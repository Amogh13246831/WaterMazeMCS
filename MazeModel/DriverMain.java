package backEnd;

public class DriverMain {

	public static void main(String[] args) {
	
		//WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[] {new GridPoint(13, 3), new GridPoint(0, 6), new GridPoint(8, 1)});
		WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[] {new GridPoint(13, 3), new GridPoint(0, 6)}), t = null;
		//WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[] {new GridPoint(13, 3)});
		//WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[0]);
		DataReporter d = new DataReporter();
		//d.printVisualCue(s.visCues.cues[0]);
		int maxiters = 1000;
		for(int i=0; i<maxiters; i++) {
			s.runSimulation();
			//d.printSimulation(s);
			//s.storeData("testsim.txt");
			//t = WaterMazeSimulation.readData("testsim.txt");
			//t.runSimulation();
			//d.printSimulation(t);
			
			System.out.println("\nScore: " + s.successes + "/" + i);
		}
		
		System.out.println("\nScore: " + s.successes + "/" + maxiters);
		
	}
}