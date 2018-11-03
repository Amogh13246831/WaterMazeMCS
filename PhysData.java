package watermazeData;

public class PhysData {
	
	static int steps = 15;
	static double stepSize = 1.5;
	static int diameter = 15;
	static int radius = diameter/2;
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
}

