package backEnd;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

public class ModelStep extends MazeParameters {

	private static final long serialVersionUID = 1L;
	Random rand = new Random(System.currentTimeMillis());
	
	GridPoint nextValidGridPoint(double x, double y, double angle) {   // return a new GridPoint if within bounds, else null
		double nx, ny;
		
		nx = x + STEPSIZE*Math.cos(angle);
		ny = y + STEPSIZE*Math.sin(angle);
		
		if(centerDist(nx, ny) <= radius)    // new location is within bounds
			return new GridPoint((int)nx, (int)ny);
		return null;
	}
	
	double validRandomAngle(double x, double y) {
		double angle, nx, ny;
		
		do {               // get a valid random angle			
			angle = degToRad((int)(Math.random()*360));
			nx = x + STEPSIZE*Math.cos(angle);
			ny = y + STEPSIZE*Math.sin(angle);
		} while(centerDist(nx, ny) > radius);
		
		return angle;
	}
	
	double nextAngleFromMemory(double x, double y, GridCell[][] memoryArena) {
		GridCell current = new GridCell(memoryArena[(int)x][(int)y]);
		double randAngle = validRandomAngle(x, y), angle, score;
		
		if(current.trials > 0 && current.angleToNext != -1) {
			score = current.comWeight * current.platWeight;
			angle = (randAngle * (1-score)) + (current.angleToNext * (score));  
			
			if(nextValidGridPoint(x, y, angle) != null)  // new location is within bounds
				return angle;
		}
		return randAngle;  // no input from memory, just random
	}
	
	ArrayList<Double> nextAngleFromCues(double x, double y, GridCell[][] memoryArena, VisualCue[] cues) {
		ArrayList<Double> angles = new ArrayList<>();
		
		for(int i=0; i<cues.length; i++) {
			if(nextValidGridPoint(x, y, cues[i].angleToNext[(int)x][(int)y]) != null)
				angles.add(cues[i].angleToNext[(int)x][(int)y]);
		}
		
		return angles;
	}
	
	RatPosition bestNextStep(double x, double y, GridCell[][] memoryArena, VisualCue[] cues) {
		
		double memAngle = nextAngleFromMemory(x, y, memoryArena);
		ArrayList<Double> nextAngles = nextAngleFromCues(x, y, memoryArena, cues);
		GridCell nextCell;
		GridPoint nextPoint;
		double score, bestScore = 0, bestAngle = memAngle, nx, ny;
		
		//HashMap<GridPoint, Double> scores = new HashMap<>();
		
		nextAngles.add(memAngle);

		for(double angle: nextAngles) {               // add new or augment the scores for cells indicated
			nextPoint = nextValidGridPoint(x, y, angle);
			nextCell = memoryArena[nextPoint.x][nextPoint.y];
			score = nextCell.comWeight * nextCell.platWeight;
			if(score > bestScore) {
				bestAngle = angle;
				bestScore = score;
			}
			/*
			if(scores.containsKey(nextPoint)) 
				score += scores.get(nextPoint);
			if(score > bestScore) {
				bestScore = score;
				bestAngle = angle;
			}
			scores.put(nextPoint, score);		
			*/
		}
		
		nx = x + STEPSIZE*Math.cos(bestAngle);
		ny = y + STEPSIZE*Math.sin(bestAngle);
		
		return new RatPosition(nx, ny, bestAngle);
	}
}
