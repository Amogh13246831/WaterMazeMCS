package backEnd;
import java.util.Scanner;
import java.io.*;

public class Simulation extends PhysData implements Serializable {
	
	VisualCues visCues;
	ParticleData rat;
	public Arena maze;
	
	private static final long serialVersionUID = 1L;
	boolean isFirstTrial;
	int simId;
	int curX, curY;
	int successes;
	
	public Simulation(int id, int numCues) {
		Scanner in = new Scanner(System.in);
		int x, y;
		
		simId = id;
		successes = 0;
		maze = new Arena();
		rat = new ParticleData();
		PathType[] cueLocs = new PathType[numCues];
		
		if(numCues != 0)
			for(int i=0; i<diameter; i++) {
				for(int j=0; j<diameter; j++)
					if(maze.centerDist(i,j) > radius)
						System.out.print("\t");
					else
						System.out.print("(" + i + ", " + j + ")\t");
				System.out.println("\n");
			}
		
		for(int i=0; i<numCues; i++) {
			System.out.println("Enter coordinates of cue " + i + ": ");
			x = in.nextInt();
			y = in.nextInt();
			cueLocs[i] = new PathType(x, y);
		}
		visCues = new VisualCues(cueLocs, maze.platform.x, maze.platform.y);
		
		in.close();
	}
	
	public Simulation(int id, PathType[] cueLocs) {
		
		simId = id;
		successes = 0;
		maze = new Arena();
		rat = new ParticleData();
		visCues = new VisualCues(cueLocs, maze.platform.x, maze.platform.y);
	}
	
	public Simulation(Simulation s) {
		simId = s.simId;
		successes = s.successes;
		visCues = s.visCues;
		maze = s.maze;
		rat = s.rat;
		isFirstTrial = s.isFirstTrial;
		curX = s.curX;
		curY = s.curY;
	}

	void printInfo() {	  // print all data
		maze.printArena(); 
		maze.printBayesianStored();
		visCues.putCues();
		System.out.println("Total number of successes: " + successes);
	}

	void nextStep() {
		//AdHocStep next = new AdHocStep();
		BayesianStep next = new BayesianStep();
		//Step next = new Step();
		rat = next.bestNextStep(rat.xPos, rat.yPos, maze.memArena, visCues.cues);
		
		if(maze.arena[curX][curX].dirVect == -1) // update maze data
			maze.arena[curX][curY].dirVect = rat.direction;
		else
			maze.arena[curX][curY].dirVect += rat.direction;
		
		curX = (int) rat.xPos;   // update sim data
		curY = (int) rat.yPos;
		maze.arena[curX][curY].visited += 1;  // new cell visited (initially start not counted)
	}
	
	boolean monteCarloSearch() {
		/*
		  take steps and store to path,
		  till either platform is encountered or maximum number of steps are taken
		 */
		 maze.randomizeStart();
		 rat.setLocation(maze.startCell.x,  maze.startCell.y); // set start position
		 curX = maze.startCell.x;
		 curY = maze.startCell.y;

		 for(maze.stepCount=0; maze.stepCount<steps; maze.stepCount++) {
			 nextStep();                  // take a step and update the arena
			 maze.path[maze.stepCount].x = curX;
			 maze.path[maze.stepCount].y = curY;
			 if(curX==maze.platform.x && curY==maze.platform.y)  // platform encountered
				 return true;
		 }
		 return false; // search ends unsuccessfully
		 
	}
	
	public void runSimulation() {
		maze.getNewArena();
		if(monteCarloSearch()) { // perform the search	
			successes++;           // if successful, increment total number of successes
			maze.getTrialOutcome(true);
		}
		else
			maze.getTrialOutcome(false);
		
		maze.updateMemory();         // update trial results to stored arena
		visCues.updateCues(maze.arena);                // update cue results
		//printInfo();
	}
	
	void storeData(String filename) {
		FileOutputStream fOut = null;
		ObjectOutputStream oOut = null;
		try {
			fOut = new FileOutputStream(new File(filename));
			oOut = new ObjectOutputStream(fOut);
			oOut.writeObject(this);
		} catch(IOException e) {
			System.out.println("File error: " + e);
		} finally {
			try {
				fOut.close();
				oOut.close();
			} catch(IOException e) {
				System.out.println("File error: " + e);
			}
		}
		
	}
	
	static Simulation readData(String filename) {
		FileInputStream fIn = null;
		ObjectInputStream oIn = null;
		try {
			fIn = new FileInputStream(new File(filename));
			oIn = new ObjectInputStream(fIn);
			try {
				return (Simulation) (oIn.readObject());		
			} catch(ClassNotFoundException e) {
				System.out.println("File Error " + e);
			}
			
		} catch(IOException e) {
			System.out.println("File error: " + e);
		} finally {
			try {
				fIn.close();
				oIn.close();
			} catch(IOException e) {
				System.out.println("File error: " + e);
			}
		}
		return null;
	}
}
