package backEnd;
import java.io.Serializable;

public class VisualCue extends MazeParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	int cueNumber;  
	public GridPoint position;
	
	GridPoint[][] nextCell;
	double[][] angleToNext;
	double[][] confidence;
	
	public VisualCue(int number, int x, int y) {
		cueNumber = number;
		position = new GridPoint(x, y);	
		angleToNext = new double[DIAMETER][DIAMETER];
		confidence = new double[DIAMETER][DIAMETER];
	}

}
