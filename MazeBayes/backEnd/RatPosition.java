package backEnd;
import java.io.Serializable;

public class RatPosition implements Serializable {

	private static final long serialVersionUID = 1L;
	double xPos;
	double yPos;
	double direction;
	
	public RatPosition() {
		
	}
	
	public RatPosition(double x, double y, double angle) {
		xPos = x;
		yPos = y;
		direction = angle;
	}
	
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
