package backEnd;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class MazeArena extends MazeParameters implements Serializable {
	
	Random random = new Random(System.currentTimeMillis());
	private static final long serialVersionUID = 1L;
	GridCell trialArena[][];
	GridCell memoryArena[][];

	boolean isFirstTrial;
	int stepCount = 0;	
	GridPoint centerOfMass;
	double inverseDistPlatCom = 1;
	
	public GridPoint startCell;
	public GridPoint platform;
	GridPoint platformQuadrant;
	public ArrayList<GridPoint> trialPath;
	HashSet<GridPoint> visitedCells;
	
	public MazeArena() {
		trialArena = new GridCell[DIAMETER][DIAMETER];
		memoryArena = new GridCell[DIAMETER][DIAMETER];
		for(int i=0; i< DIAMETER; i++)
			for(int j=0; j<DIAMETER; j++) {
				trialArena[i][j] = new GridCell();
				memoryArena[i][j] = new GridCell();
			}
		
		startCell = new GridPoint();
		platformQuadrant = new GridPoint(0, 90);
		platform = new GridPoint();
		
		double pRad = 0.75 * radius;
		double pAngle = degToRad((platformQuadrant.y - platformQuadrant.x) / 2); 
		platform.x = (int) (center.x + pRad*Math.cos(pAngle));
		platform.y = (int) (center.y + pRad*Math.sin(pAngle));
		
		trialPath = new ArrayList<>();
		visitedCells = new HashSet<>();
	}
	
	void getNewTrialArena() {
		trialArena = new GridCell[DIAMETER][DIAMETER];
		for(int i=0; i< DIAMETER; i++)
			for(int j=0; j<DIAMETER; j++) {
				trialArena[i][j] = new GridCell();
			}
		
		trialPath = new ArrayList<>();
		visitedCells = new HashSet<>();
	}
	
	
	void randomizeStart() {
		double sAngle;	
		int temp; 
		do {
			temp = random.nextInt(360); // randomize angle
		} while(temp > platformQuadrant.x && temp < platformQuadrant.y); // until not in platform quadrant
 
		sAngle = degToRad(temp);
		startCell.x = (int) (center.x + radius*Math.cos(sAngle)); 
		startCell.y = (int) (center.y + radius*Math.sin(sAngle)); 
	}
	
	void getVisitedCells() {
		for(GridPoint g: trialPath)
			visitedCells.add(g);
	}

	void updateTrialSuccess(boolean success) {
		for(GridPoint g: visitedCells) {   // update trials and successes after results of completed trial
			trialArena[g.x][g.y].trials = 1;
			if(success)
				trialArena[g.x][g.y].successes = 1;
		}
	}
	
	void updateTrialAngleToNext() {
		 for(GridPoint g: visitedCells) 
			 if(trialArena[g.x][g.y].angleToNext != -1)
				 trialArena[g.x][g.y].angleToNext /= trialArena[g.x][g.y].visits;	 
	}
	
	void findTrialCenterOfMass() {
		double xCom = 0, yCom = 0, totalWeight = 1;
		int x, y;
		for(GridPoint g: visitedCells) {
			x = g.x; y = g.y;
			totalWeight += trialArena[g.x][g.y].visits; // summation Mi
			xCom += x*trialArena[g.x][g.y].visits; // summation MiXi
			yCom += y*trialArena[g.y][g.x].visits;  // summation MiYi
		}
		
		// assign the coordinates to center of mass
		centerOfMass = new GridPoint((int) (xCom/totalWeight), (int) (yCom/totalWeight));
	}
	
	void findTrialAngleToCom() {
		int x, y;
		for(GridPoint g: visitedCells) {
			x = g.x; y = g.y;
			trialArena[x][y].angleToCom = Math.atan2(centerOfMass.y - y, centerOfMass.x - x);
			if(trialArena[x][y].angleToCom < 0)
				trialArena[x][y].angleToCom += degToRad(360); // convert negative angles to 180+
		}
	}
	
	void computeComWeight() {
		double offset = 0;
		int x, y;
		for(GridPoint g: visitedCells) {
			x = g.x; y = g.y;
			if(trialArena[x][y].angleToNext != -1) {  
				offset = radToDeg(Math.abs(trialArena[x][y].angleToCom - trialArena[x][y].angleToNext)); 
				if(360-offset < offset)     // the smaller angle is considered 
					offset = 360 - offset;
				trialArena[x][y].comWeight = 1 - (offset/18)*0.1;
			}
		}
	}
	
	void computePlatWeight() {
		double dist = Math.sqrt(Math.pow(platform.x-centerOfMass.x,2) + Math.pow(platform.y-centerOfMass.y,2));
		if(dist < 1)
			dist = 1;
		inverseDistPlatCom = 1 / dist;
		
		for(GridPoint g:visitedCells)
			trialArena[g.x][g.y].platWeight = inverseDistPlatCom;
		
	}
	
	void updateMemory() {
		double memA, memV, trA, trV;
		
		for(GridPoint g: visitedCells) {
			int x = g.x, y = g.y;
		
			if(memoryArena[x][y].visits == 0) 
				memoryArena[x][y] = new GridCell(trialArena[x][y]);    // first ever visit to that cell

			else {
				memV = memoryArena[x][y].visits; trV = trialArena[x][y].visits;	
				
				if(trialArena[x][y].angleToNext != -1) {             // not the last visited cell with no next angle
					if(memoryArena[x][y].angleToNext == -1) {
						memoryArena[x][y].angleToNext = trialArena[x][y].angleToNext;
						memoryArena[x][y].comWeight = trialArena[x][y].comWeight;
					}
					else {
						memA = memoryArena[x][y].angleToNext; trA = trialArena[x][y].angleToNext;		
						memoryArena[x][y].angleToNext = ((memA*memV) + (trA*trV)) / (memV + trV); // visit-weighted avg of angles in memory and trial
						
						memA = memoryArena[x][y].comWeight; trA = trialArena[x][y].comWeight;	
						memoryArena[x][y].comWeight = ((memA*memV) + (trA*trV)) / (memV + trV); // visit-weighted avg of comWeight in memory, trial
					}
				}
				memA = memoryArena[x][y].platWeight; trA = trialArena[x][y].platWeight;
				memoryArena[x][y].platWeight = ((memA*memV) + (trA*trV)) / (memV + trV); // visit-weighted avg of platWeight in memory, trial
				
				memoryArena[x][y].visits += trialArena[x][y].visits;
				memoryArena[x][y].trials += trialArena[x][y].trials;
				memoryArena[x][y].successes += trialArena[x][y].successes;
				
			}
		}
	}
	
	void calculateTrialOutcome(boolean success) {
		getVisitedCells();
		updateTrialSuccess(success);
		updateTrialAngleToNext();
		
		findTrialCenterOfMass();
		findTrialAngleToCom();
		computeComWeight();
		computePlatWeight();
		
		updateMemory();
	}
	
}
	
