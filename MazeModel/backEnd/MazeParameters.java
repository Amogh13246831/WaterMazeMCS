package backEnd;
import java.io.Serializable;

public class MazeParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	static final int STEPS = 60;
	static final double STEPSIZE = 1.5;
	public static final int DIAMETER = 15;   // make it an odd number
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
