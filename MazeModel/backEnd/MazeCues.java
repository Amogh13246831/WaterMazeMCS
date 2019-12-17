package backEnd;

import java.io.Serializable;
import java.util.HashSet;

public class MazeCues extends MazeParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public VisualCue[] cues;
	int numCues;
	
	public MazeCues(GridPoint[] locs, GridPoint platform) {
		numCues = locs.length;
		cues = new VisualCue[numCues]; 
		
		for(int i=0; i<numCues; i++) 
			cues[i] = new VisualCue(i, locs[i].x, locs[i].y, platform);		
	}

	
	void updateCues(GridCell[][] arena, HashSet<GridPoint> visitedCells) {
		double totalNewConfidence = 0, offset;
		double newConfidence[] = new double[cues.length];
		
		for(GridPoint g: visitedCells) {
			int x = g.x, y = g.y;
			totalNewConfidence = 0;
			
			for(int i=0; i<cues.length; i++) {                // get new confidences
				offset = Math.abs(cues[i].angleToNext[x][y] - arena[x][y].angleToNext);  // angle difference of cue and trial
				newConfidence[i] = cues[i].confidence[x][y] + 1 - (radToDeg(offset)/18)*0.1;    // Ci = Ci + dA
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
