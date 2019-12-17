package backEnd;
import java.io.Serializable;

public class GridCell implements Serializable {
	
	private static final long serialVersionUID = 1L;
	int visits, trials, successes;
	double angleToNext, angleToCom;
	double comWeight, platWeight;
	
	public GridCell() {
		visits = trials = successes = 0;
		angleToNext = angleToCom = -1;
		comWeight = platWeight = 0;
	}
	
	public GridCell(GridCell g) {
		visits = g.visits;
		trials = g.trials;
		successes = g.successes;
		angleToNext = g.angleToNext;
		angleToCom = g.angleToCom;
		comWeight = g.comWeight;
		platWeight = g.platWeight;
	}
	
}
