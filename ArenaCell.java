package watermazeData;
import java.io.Serializable;

public class ArenaCell implements Serializable {
	
	private static final long serialVersionUID = 1L;
	int visited = 0;
	double dirVect = -1;
	double centerAngle = -1;
	double comWeight = 0;
	double platWeight = 0;
}

