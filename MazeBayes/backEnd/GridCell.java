package backEnd;
import java.io.Serializable;

public class GridCell implements Serializable {
	
	private static final long serialVersionUID = 1L;
	int visits;
	public int trials;
	public int successes;
	double angleToNext;
	
	public GridCell() {
		visits = trials = successes = 0;
		angleToNext = -1;
	}
	
	public GridCell(GridCell g) {
		visits = g.visits;
		trials = g.trials;
		successes = g.successes;
		angleToNext = g.angleToNext;
	}
	
}
