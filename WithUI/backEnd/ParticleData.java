package backEnd;
import java.io.Serializable;

public class ParticleData implements Serializable {

	private static final long serialVersionUID = 1L;
	double xPos;
	double yPos;
	double direction;
	
	public ParticleData() {
		
	}
	
	public ParticleData(double x, double y, double angle) {
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