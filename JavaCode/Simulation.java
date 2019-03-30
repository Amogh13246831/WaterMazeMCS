package backEnd;
import java.util.Scanner;
import java.io.*;

public class Simulation extends PhysData implements Serializable {
	
	
	CueData visCues[];
	ParticleData rat;
	public Arena maze;
	
	private static final long serialVersionUID = 1L;
	boolean isFirstTrial;
	int simId;
	int curX, curY;
	int numCues;
	int successes;
	
	public Simulation(int id, int cues) {
		Scanner in = new Scanner(System.in);
		int x, y;
		
		simId = id;
		successes = 0;
		maze = new Arena();
		rat = new ParticleData();
		numCues = cues;
		visCues = new CueData[numCues]; 
		
		if(numCues != 0)
			for(int i=0; i<diameter; i++)
				for(int j=0; j<diameter; j++)
					if(maze.centerDist(i,j) > radius)
						System.out.print("\t");
					else
						System.out.print("(" + i + ", " + j + ")\t");
				System.out.println("\n");
		
		for(int i=0; i<numCues; i++) {
			System.out.println("Enter coordinates of cue " + i + ": ");
			x = in.nextInt();
			y = in.nextInt();
			visCues[i] = new CueData(i, x, y);
			visCues[i].setDetails(maze.platform.x, maze.platform.y);
		}
		
		in.close();
	}
	
	public Simulation(Simulation s) {
		simId = s.simId;
		numCues = s.numCues;
		successes = s.successes;
		visCues = s.visCues;
		maze = s.maze;
		rat = s.rat;
		isFirstTrial = s.isFirstTrial;
		curX = s.curX;
		curY = s.curY;
	}
	
	public void setCues(PathType[] locs) {
		for(int i=0; i<numCues; i++) {
			visCues[i] = new CueData(i, locs[i].x, locs[i].y);
			visCues[i].setDetails(maze.platform.x, maze.platform.y);
		}
	}
	
	void putCues() {  // print all cues	
		for(int i=0; i<numCues; i++)
			visCues[i].printCue();
	}

	void printInfo() {	  // print all data
		maze.printArena(); 
		maze.printStored();
		putCues();
		System.out.println("Total number of successes: " + successes);
	}
	
	void nextStep() {
		double x, y, score = 0, tempScore, tempAngle;
		int i, best = -1, nx, ny;
		
		class Pos {
			double x;
			double y;
		}
		Pos cueStep[] = new Pos[numCues];

		if(maze.memArena[curX][curY].visited > 0) { // get angle based on memory calculations	
			tempScore = maze.memArena[curX][curY].comWeight * maze.memArena[curX][curY].platWeight;
			tempAngle = degToRad((int) Math.floor(Math.random()*360))*(1-tempScore) 
					+ maze.memArena[curX][curY].dirVect*tempScore;
		}
		else {
			tempAngle = degToRad((int) Math.floor(Math.random()*360));
		}
		x = rat.xPos + stepSize*Math.cos(tempAngle);
		y = rat.yPos + stepSize*Math.sin(tempAngle);
		while(maze.centerDist(x, y) > radius) {               // get a valid random angle			
			tempAngle = degToRad((int) Math.floor(Math.random()*360));
			x = rat.xPos + stepSize*Math.cos(tempAngle);
			y = rat.yPos + stepSize*Math.sin(tempAngle);
		} 

		for(i=0; i<numCues; i++) {                     // get next cells that cues indicate	
			cueStep[i] = new Pos();
			cueStep[i].x = rat.xPos + stepSize*Math.cos(visCues[i].randVect[curY][curY]);
			cueStep[i].y = rat.yPos + stepSize*Math.cos(visCues[i].randVect[curX][curY]);
		}
		
		for(i=0; i<numCues; i++) {      // choose index of cue indicating best step (-1 if none)	
			nx = (int) cueStep[i].x;
			ny = (int) cueStep[i].y;
			if(maze.centerDist(nx, ny) <= radius) {
				tempScore = maze.memArena[nx][ny].comWeight * maze.memArena[nx][ny].platWeight;
				tempScore *= visCues[i].confidence[nx][ny]; 
				if(score < tempScore) {
					score = tempScore;
					best = i;
				}
			}
		}
		
		if(best != -1) {    // compare best visual cue angle with angle from memory, select the best one
			tempScore = maze.memArena[(int) x][(int) y].comWeight * maze.memArena[(int) x][(int) y].platWeight;
			tempScore /= numCues; 
			if(score > tempScore) {
				tempAngle = visCues[best].randVect[curX][curY];
				x = cueStep[best].x;
				y = cueStep[best].y;
			}
		}
		
		rat.setDirection(tempAngle);  // update rat data
		rat.setLocation(x, y);
		
		if(maze.arena[curX][curX].dirVect == -1) // update maze data
			maze.arena[curX][curY].dirVect = tempAngle;
		else
			maze.arena[curX][curY].dirVect += tempAngle;
		
		curX = (int) x;   // update sim data
		curY = (int) y;
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
	
	void updateCues() {
		/*
	  		modify the direction pointed to by and confidence in a cue, cell-by-cell,
	  		by computing weight similar to comwt for confidence offset dCi,
	  		and computing Ci = (Ci + dCi)/(summation Ci+dCi) to normalise,
	  		and modifying direction Di as DiCi + Tavg(1-Ci), 
	  		updating Ci and Di independently for each cue i, in each vell visited in the trial
		*/
		int i, j, k, offset;
		double totalconf;
		CueData oldcues[] = new CueData[maxCues];

		for(i=0; i<numCues; i++)
			oldcues[i] = new CueData(visCues[i]);       // store old values to use in modifying

		// update confidence based on offset between average direction and cue vector
		for(i=0; i<diameter; i++)
			for(j=0; j<diameter; j++)
				if(maze.arena[i][j].visited > 0 && maze.arena[i][j].dirVect != -1) { // valid cell visited	
					for(k=0; k<numCues; k++) { // for each cue k, compute new confidence Ck+dCk				 
						offset = radToDeg(oldcues[k].randVect[i][j] - maze.arena[i][j].dirVect);
						if(offset < 0) 
							offset = -offset; // modulus of angle distance
						if(offset > 360-offset)
							offset = 360 - offset;  // the smaller distance is considered 
						visCues[k].confidence[i][j] += 1 - (offset/18)*0.1;
					}
				}
		// divide confidence of each cue by total confidence of all cues, for a cell
		for(i=0; i<diameter; i++)
			for(j=0; j<diameter; j++)
				if(maze.arena[i][j].visited > 0 && maze.arena[i][j].dirVect != -1) { // for each valid cell			 
					totalconf = 0;
					for(k=0; k<numCues; k++)
						totalconf += visCues[k].confidence[i][j]; // find summation Ck+dCk
					if(totalconf > 0)
						for(k=0; k<numCues; k++)
							visCues[k].confidence[i][j] /= totalconf; // divide each Ck+dCk by summation 
				}
		// modify direction pointed to based on offset from average, and confidence
		for(i=0; i<diameter; i++)
			for(j=0; j<diameter; j++)
				if(maze.arena[i][j].visited > 0 && maze.arena[i][j].dirVect != -1) { // valid cell visited				
					for(k=0; k<numCues; k++) { // for each cue, compute new direction 					
						totalconf = oldcues[k].confidence[i][j];
						visCues[k].randVect[i][j] *= totalconf; 
						visCues[k].randVect[i][j] += maze.arena[i][j].dirVect*(1-totalconf);
					}
				}
	}
	
	public void runSimulation() {
		maze.getNewArena();
		if(monteCarloSearch()) { // perform the search	
			successes++;           // if successful, increment total number of successes
		}
		maze.findAverageDirection();;     // average out all direction vectors
		maze.findCenterMass();        // compute CoM
		maze.findCenterAngle();       // compute angle of cell to CoM
		maze.computeComWeight();             // compute weight to store
		maze.computePlatWeight();      // compute inverse distance of CoM from platform     
		maze.updateMemory();         // update trial results to stored arena
		updateCues();                // update cue results
		printInfo();
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