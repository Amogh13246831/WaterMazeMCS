package backEnd;
import java.io.Serializable;

public class MazeParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/*
	 * Morris Water Maze parameters: 150 cm diameter, 10 cm platform length 
	 * https://en.wikipedia.org/wiki/Morris_water_navigation_task
	 * 
	 * Rough average swimming speed of a rat in the Morris Water Maze test: 15 cm/s
	 * https://www.researchgate.net/figure/The-average-swim-speed-of-rats-in-Morris-water-maze-test-No-significant-difference-was_fig6_309667194
	 * 
	 * Guesstimate of distance per swim stroke of a rat: 3 cm
	 * 
	 * Grid cell dimension: 1 cm
	 * 
	 * Trial time: 60 seconds
	 *  
	 */
	static final int STEPS = 300;
	static final double STEPSIZE = 5;
	public static final int DIAMETER = 151;   // make it an odd number
	public static int radius = DIAMETER/2;
	static int maxCues = 10;
	static int maxSubjects = 10;

	GridPoint center = new GridPoint(radius, radius);
	
	public double centerDist(double x, double y) { // distance of (x,y) from center of the arena
		return Math.sqrt(Math.pow(center.x-x,2) + Math.pow(center.y-y,2)); 
	}
	
	double degToRad(int angle) { // convert degree to radian	
		return angle*Math.PI/180;
	}

	int radToDeg(double angle) {	
		return (int)(angle*180/Math.PI);
	}		
	
}
