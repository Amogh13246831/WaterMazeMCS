package backEnd;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class MazeArena extends MazeParameters implements Serializable {
	
	Random random = new Random(System.currentTimeMillis());
	private static final long serialVersionUID = 1L;
	GridCell trialArena[][];
	public GridCell memoryArena[][];

	boolean isFirstTrial;
	int stepCount = 0;	

	
	public GridPoint startCell;
	public GridPoint platformTopLeft, platformBottomRight;
	public int platformLength = 10;
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
		platformTopLeft = new GridPoint();
		platformBottomRight = new GridPoint();
		
		double pRad = 0.75 * radius;
		double pAngle = degToRad((platformQuadrant.y - platformQuadrant.x) / 2); 
		platformTopLeft.x = (int) (center.x + pRad*Math.cos(pAngle)) - platformLength/2;
		platformTopLeft.y = (int) (center.y + pRad*Math.sin(pAngle)) - platformLength/2;
		platformBottomRight.x = (int) (center.x + pRad*Math.cos(pAngle)) + platformLength/2;
		platformBottomRight.y = (int) (center.y + pRad*Math.sin(pAngle)) + platformLength/2;
		
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
	
	void updateMemory() {
		double memA, memV, trA, trV;
		
		for(GridPoint g: visitedCells) {
			int x = g.x, y = g.y;
		
			if(memoryArena[x][y].visits == 0) 
				memoryArena[x][y] = new GridCell(trialArena[x][y]);    // first ever visit to that cell

			else {
				if(trialArena[x][y].angleToNext != -1) {             // not the last visited cell with no next angle
					if(memoryArena[x][y].angleToNext == -1)
						memoryArena[x][y].angleToNext = trialArena[x][y].angleToNext;
					else {
						memA = memoryArena[x][y].angleToNext; trA = trialArena[x][y].angleToNext;
						memV = memoryArena[x][y].visits; trV = trialArena[x][y].visits;
						
						memoryArena[x][y].angleToNext = ((memA*memV) + (trA*trV)) / (memV + trV); // visit-weighted avg of angles in memory and trial
					}
				}
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
		updateMemory();
	}
	
}
	
