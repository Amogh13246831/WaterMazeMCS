package backEnd;
import java.util.Scanner;	
import java.io.*;

public class WaterMazeSimulation extends MazeParameters implements Serializable {
	
	public MazeCues visCues;
	RatPosition rat;
	public MazeArena maze;
	
	private static final long serialVersionUID = 1L;
	boolean isSuccess;
	int simId;
	GridPoint current;
	public int successes;
	public int totalTrials;
	
	void setCurrent(int x, int y) {
		current = new GridPoint(x, y);
	}
	
	public WaterMazeSimulation(int id, GridPoint[] cueLocs) {
		
		simId = id;
		successes = 0;
		totalTrials = 0;
		maze = new MazeArena();
		rat = new RatPosition();
		visCues = new MazeCues(cueLocs, maze.platformTopLeft.x + maze.platformLength/2, maze.platformTopLeft.y + maze.platformLength/2);
	}
	
	public WaterMazeSimulation(WaterMazeSimulation s) {
		simId = s.simId;
		successes = s.successes;
		totalTrials = s.totalTrials;
		visCues = s.visCues;
		maze = s.maze;
		rat = s.rat;
		isSuccess = s.isSuccess;
		current = new GridPoint(s.current.x, s.current.y);
	}
	
	void nextStep() {
		BayesianStep next = new BayesianStep();
		rat = next.bestNextStep(rat.xPos, rat.yPos, maze.memoryArena, visCues.cues, rat.direction, 0.1); // changed for momentum calc
		
		if(maze.trialArena[current.x][current.y].angleToNext == -1)      // update step data
			maze.trialArena[current.x][current.y].angleToNext = rat.direction;
		else
			maze.trialArena[current.x][current.y].angleToNext += rat.direction;
		
		setCurrent((int)rat.xPos, (int)rat.yPos);   // update location data
		maze.trialArena[current.x][current.y].visits += 1;  // new cell visited (initially start not counted)
	}
	
	boolean monteCarloSearch() {
		 maze.randomizeStart();
		 rat.setLocation(maze.startCell.x,  maze.startCell.y); // set start position
		 setCurrent(maze.startCell.x, maze.startCell.y);
		 
		 for(maze.stepCount=0; maze.stepCount<STEPS; maze.stepCount++) {
			 nextStep();                  // take a step and update the arena
			 
			 maze.trialPath.add(new GridPoint(current.x, current.y));
			 
			 if((current.x >= maze.platformTopLeft.x && current.x <= maze.platformBottomRight.x) && 
					 (current.y >= maze.platformTopLeft.y && current.y <= maze.platformBottomRight.y))  // platform encountered
				 return true;
		 }
		 return false; // search ends unsuccessfully	 
	}
	
	public void runSimulation() {
		maze.getNewTrialArena();
		totalTrials++;
		if((isSuccess=monteCarloSearch())) { // perform the search	
			successes++;           // if successful, increment total number of successes
			maze.calculateTrialOutcome(true);
		}
		else
			maze.calculateTrialOutcome(false);
		
		visCues.updateCues(maze.trialArena, maze.visitedCells);                // update cue results
	}
	
	public void storeData(String filename) {
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
	
	public static WaterMazeSimulation readData(String filename) {
		FileInputStream fIn = null;
		ObjectInputStream oIn = null;
		try {
			fIn = new FileInputStream(new File(filename));
			oIn = new ObjectInputStream(fIn);
			try {
				return (WaterMazeSimulation) (oIn.readObject());		
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