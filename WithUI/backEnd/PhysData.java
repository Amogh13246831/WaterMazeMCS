package backEnd;

import java.io.Serializable;

public class PhysData {
	
	static int steps = 60;
	static double stepSize = 1.5;
	public static int diameter = 15;
	public static int radius = diameter/2;
	static int maxCues = 10;
	static int maxSubjects = 10;
	
	double degToRad(int angle) // convert degree to radian
	{
		return angle*3.14159/180;
	}

	int radToDeg(double angle)
	{
		return (int)(angle*180/3.14159);
	}
		
	public class PathType implements Serializable {
		private static final long serialVersionUID = 1L;
		public int x = 0;
		public int y = 0;
	}
}