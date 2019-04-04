package backEnd;

public class Step extends PhysData {

	private static final long serialVersionUID = 1L;
	
	class EvalStep {
		double angle; 
		double eval;
		
		EvalStep(double a, double e) {
			angle = a;
			eval = e;
		}
	}
	
	double validRandomAngle(double x, double y) {
		double angle, nx, ny;
		
		do {               // get a valid random angle			
			angle = degToRad((int) Math.floor(Math.random()*360));
			nx = x + stepSize*Math.cos(angle);
			ny = y + stepSize*Math.sin(angle);
		} while(centerDist(nx, ny) > radius);
		
		return angle;
	}
	
	double[] evaluate(double x, double y) {
		double angle = validRandomAngle(x, y),
				nx = x + stepSize*Math.cos(angle),
				ny = y + stepSize*Math.sin(angle);
		
		return new double[] {nx, ny};
	}
}

class AdHocStep extends Step {

	private static final long serialVersionUID = 1L;
	
	EvalStep memoryStep(double x, double y, Arena maze) {
		int cx = (int)x, cy = (int)y;
		double angle, score, nx, ny;
		
		if(maze.memArena[cx][cy].visited > 0) { // get angle based on memory calculations	
			score = maze.memArena[cx][cy].comWeight * maze.memArena[cx][cy].platWeight;
			angle = degToRad((int) (Math.floor(Math.random()*360)*(1-score) + maze.memArena[cx][cy].dirVect*score));
			
			nx = x + stepSize*Math.cos(angle);
			ny = y + stepSize*Math.sin(angle);
			if(centerDist((int)nx, (int)ny) > radius) {
				angle = validRandomAngle(x, y);
			}
		}
		else
			angle = validRandomAngle(x, y);

		nx = x + stepSize*Math.cos(angle);
		ny = y + stepSize*Math.sin(angle);
		score = (maze.memArena[(int) nx][(int) ny].comWeight * maze.memArena[(int) x][(int) y].platWeight);
		
		return new EvalStep(angle, score);
	}
	
	EvalStep cueStep(double x, double y, Arena maze, CueData[] cues) {
		int numCues = cues.length;
		class Pos {
			double x;
			double y;
		}
		Pos cueStep[] = new Pos[numCues];
		double score, topScore = 0, angle;
		int nx, ny, i, cx = (int)x, cy = (int)y, best = -1;
		
		for(i=0; i<numCues; i++) {                     // get next cells that cues indicate	
			cueStep[i] = new Pos();
			cueStep[i].x = x + stepSize*Math.cos(cues[i].randVect[cx][cy]);
			cueStep[i].y = y + stepSize*Math.cos(cues[i].randVect[cx][cy]);
		}
		
		for(i=0; i<numCues; i++) {      // choose index of cue indicating best step (-1 if none)	
			nx = (int) cueStep[i].x;
			ny = (int) cueStep[i].y;
			if(centerDist(nx, ny) <= radius) {
				score = maze.memArena[nx][ny].comWeight * maze.memArena[nx][ny].platWeight * cues[i].confidence[nx][ny]; 
				if(topScore < score) {
					topScore = score;
					best = i;
				}
			}
		}
		
		if(best != -1)     
			angle = cues[best].randVect[cx][cy];    // top score already computed
		else {
			angle = -1;
			topScore = -1;
		}
		return new EvalStep(angle, topScore);
	}
	
	double[] evaluate(double x, double y, Arena maze, CueData[] cues) {
		EvalStep memAngle = memoryStep(x, y, maze),
				cueAngle = cueStep(x, y, maze, cues);

		double nx, ny;
		
		if(cueAngle.angle == -1 || memAngle.eval/cues.length >= cueAngle.eval) {  // normalize memory evaluation with numCues
			nx = x + stepSize*Math.cos(memAngle.angle);
			ny = y + stepSize*Math.sin(memAngle.angle);
		} 
		else {
			nx = x + stepSize*Math.cos(cueAngle.angle);
			ny = y + stepSize*Math.sin(cueAngle.angle);
		}
		
		return new double[] {nx, ny};
	}
}