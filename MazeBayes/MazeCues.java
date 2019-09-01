package backEnd;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MazeCues extends MazeParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	VisualCue[] cues;
	int numCues;
	
	public MazeCues(GridPoint[] locs, int platX, int platY) {
		numCues = locs.length;
		cues = new VisualCue[numCues]; 
		Random rand = new Random(System.currentTimeMillis());
		
		for(int i=0; i<numCues; i++) {
			cues[i] = new VisualCue(i, locs[i].x, locs[i].y);
			for(int j=0; j<DIAMETER; j++) 
				for(int k=0; k<DIAMETER; k++) 
					if(centerDist(j, k) <= radius) {
						cues[i].angleToNext[j][k] = rand.nextDouble() * 2 * Math.PI;    // set a random angle
						cues[i].confidence[j][k] = 1 / numCues;
					}
					else {
						cues[i].angleToNext[j][k] = -1;
						cues[i].confidence[j][k] = 0;
					}
		}
	}

	
	void updateCues(GridCell[][] arena, HashSet<GridPoint> visitedCells) {
		double totalNewConfidence = 0, offset;
		double newConfidence[] = new double[cues.length];
		
		for(GridPoint g: visitedCells) {
			int x = g.x, y = g.y;
			totalNewConfidence = 0;
			
			for(int i=0; i<cues.length; i++) {                // get new confidences
				offset = Math.abs(cues[i].angleToNext[x][y] - arena[x][y].angleToNext);  // angle difference of cue and trial
				newConfidence[i] = cues[i].confidence[x][y] + 1 - (offset/(2*Math.PI));    // Ci = Ci + dA
				totalNewConfidence += newConfidence[i];
			}
			
			for(VisualCue c: cues) {          // get new indicated angles
				c.angleToNext[x][y] *= c.confidence[x][y];     
				c.angleToNext[x][y] += arena[x][y].angleToNext * (1-c.confidence[x][y]); // Ai = AiCi + Atr(1-Ci)
			}
			
			for(int i=0; i<cues.length; i++)   // store normalized new confidences
				cues[i].confidence[x][y] = newConfidence[i] / totalNewConfidence;
		}
	}

}
