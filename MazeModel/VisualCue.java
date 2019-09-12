package backEnd;
import java.io.Serializable;
import java.util.Random;

public class VisualCue extends MazeParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	int cueNumber;  
	public GridPoint position;
	
	GridPoint[][] nextCell;
	double[][] angleToNext;
	double[][] confidence;
	double platformDistance;
	double platformDirection;
	
	public VisualCue(int number, int x, int y, GridPoint platform) {
		Random rand = new Random(System.currentTimeMillis());
		double cellDistance = 0;
		
		cueNumber = number;
		position = new GridPoint(x, y);	
		angleToNext = new double[DIAMETER][DIAMETER];
		confidence = new double[DIAMETER][DIAMETER];
		
		platformDirection = Math.atan2(platform.y - y, platform.x - x);
		platformDistance = Math.sqrt(Math.pow(platform.x - x, 2) + Math.pow(platform.y - y, 2));
		
		for(int i=0; i<DIAMETER; i++) 
			for(int j=0; j<DIAMETER; j++) 
				if(centerDist(i, j) <= radius) {
					cellDistance = Math.sqrt(Math.pow(i-x, 2) + Math.pow(j-y, 2));
					confidence[i][j] = 1 / (platformDistance * cellDistance);
					angleToNext[i][j] =  rand.nextDouble() * 2 * Math.PI;    // set a random angle
				}
				else {
					angleToNext[i][j] = -1;
					confidence[i][j] = 0;
				}
	}

}
