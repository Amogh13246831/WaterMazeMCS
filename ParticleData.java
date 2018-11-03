package watermazeData;

public class ParticleData {
	
	double xPos;
	double yPos;
	double direction;
	
	void setLocation(double x, double y)
	{
		xPos = x;
		yPos = y;
	}
	
	void setDirection(double theta)
	{
		direction = theta; 
	}
}
