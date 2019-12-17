package backEnd;

public class DriverMain {

	static WaterMazeSimulation siminit() {
		WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[] {new GridPoint(13, 3), new GridPoint(0, 6), new GridPoint(8, 1), new GridPoint(15, 7)});
		//WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[] {new GridPoint(13, 3), new GridPoint(0, 6), new GridPoint(8, 1)});
		//WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[] {new GridPoint(13, 3), new GridPoint(0, 6)});
		//WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[] {new GridPoint(13, 3)});
		//WaterMazeSimulation s = new WaterMazeSimulation(1, new GridPoint[0]);
		return s;
	}
	
	public static void main(String[] args) {
		WaterMazeSimulation s = null;
		DataReporter d = new DataReporter();
		//d.printVisualCue(s.visCues.cues[0]);
		/*
		for(int i=0; i<maxiters; i++) {
			s.runSimulation();
			//d.printSimulation(s);
			//s.storeData("testsim.txt");
			//t = WaterMazeSimulation.readData("testsim.txt");
			//t.runSimulation();
			//d.printSimulation(t);
			
			System.out.println("\nScore: " + s.successes + "/" + i);
		}
		d.printSimulation(s);
		System.out.println("\nScore: " + s.successes + "/" + maxiters);
		*/
		int maxIters = 10000, maxTrials = 1;
		int scores[] = new int[maxTrials];
		float totalScore = 0;
		
		for(int tr=0; tr<maxTrials; tr++) {
			s = siminit();
			for(int i=0; i<maxIters; i++) {
				s.runSimulation();
			}
			scores[tr] = s.successes;
			totalScore += s.successes;
			//System.out.println("\nScore: " + s.successes + "/" + maxIters);
		}

		for(int i=0; i<scores.length; i++)
			System.out.println(scores[i]);	
		
		d.printSimulation(s);
		System.out.println("Total Score: " + totalScore);
		System.out.println("Success Rate: " + totalScore/(maxIters*maxTrials));
	}
}
