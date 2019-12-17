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
			angle = degToRad((int)(Math.random()*360));
			nx = x + stepSize*Math.cos(angle);
			ny = y + stepSize*Math.sin(angle);
		} while(centerDist(nx, ny) > radius);
		
		return angle;
	}
	
	ParticleData bestNextStep(double x, double y, ArenaCell[][] memArena, CueData[] cues) {
		double angle = validRandomAngle(x, y),
				nx = x + stepSize*Math.cos(angle),
				ny = y + stepSize*Math.sin(angle);
		
		return new ParticleData(nx, ny, angle);
	}
}

class AdHocStep extends Step {

	private static final long serialVersionUID = 1L;
	
	EvalStep memoryStep(double x, double y, ArenaCell[][] memArena) {
		int cx = (int)x, cy = (int)y;
		double angle, score=0, nx, ny;
		
		if(memArena[cx][cy].visited > 0) {                          // get angle based on memory calculations	
			score = memArena[cx][cy].comWeight * memArena[cx][cy].platWeight;
			angle = degToRad((int) (Math.floor(Math.random()*360)*(1-score) + memArena[cx][cy].dirVect*score));
			
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
		score = (memArena[(int) nx][(int) ny].comWeight * memArena[(int) x][(int) y].platWeight);
		
		return new EvalStep(angle, score);
	}
	
	EvalStep cueStep(double x, double y, ArenaCell[][] memArena, CueData[] cues) {
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
			cueStep[i].y = y + stepSize*Math.sin(cues[i].randVect[cx][cy]);  // BUG FIX on 28/8/19, was Math.cos
		}
		
		for(i=0; i<numCues; i++) {      // choose index of cue indicating best step (-1 if none)	
			nx = (int) cueStep[i].x;
			ny = (int) cueStep[i].y;
			if(centerDist(nx, ny) <= radius) {
				score = memArena[nx][ny].comWeight * memArena[nx][ny].platWeight * cues[i].confidence[nx][ny]; 
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
	
	ParticleData bestNextStep(double x, double y, ArenaCell[][] memArena, CueData[] cues) {
		EvalStep memAngle = memoryStep(x, y, memArena),
				cueAngle = cueStep(x, y, memArena, cues);

		double nx, ny, angle;
		
		if(cueAngle.angle == -1 || memAngle.eval/cues.length >= cueAngle.eval)  // normalize memory evaluation with numCues
			angle = memAngle.angle;
		else 
			angle = cueAngle.angle;

		nx = x + stepSize*Math.cos(angle);
		ny = y + stepSize*Math.sin(angle);
		
		return new ParticleData(nx, ny, angle);
	}
}

class BayesianStep extends Step {

	private static final long serialVersionUID = 1L;
	
	EvalStep memoryStep(double x, double y, ArenaCell[][] memArena) {
		int cx = (int)x, cy = (int)y;
		double angle, score, nx, ny;
		
		if(memArena[cx][cy].trials > 0) { // get angle based on memory calculations	
			score = memArena[cx][cy].successes / memArena[cx][cy].trials;
			angle = degToRad((int) (Math.floor(Math.random()*360)*(1-score) + memArena[cx][cy].dirVect*score));
			
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
		score = 0; //memArena[(int) nx][(int) ny].successes / memArena[(int) x][(int) y].trials;
		
		return new EvalStep(angle, score);
	}
	
	EvalStep cueStep(double x, double y, ArenaCell[][] memArena, CueData[] cues) {
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
			cueStep[i].y = y + stepSize*Math.sin(cues[i].randVect[cx][cy]);  // BUG FIX on 28/8/19, was Math.cos
		}
		
		for(i=0; i<numCues; i++) {      // choose index of cue indicating best step (-1 if none)	
			nx = (int) cueStep[i].x;
			ny = (int) cueStep[i].y;
			if(centerDist(nx, ny) <= radius && memArena[nx][ny].trials > 0) {
				score = memArena[nx][ny].successes / memArena[nx][ny].trials * cues[i].confidence[nx][ny]; 
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
	
	ParticleData bestNextStep(double x, double y, ArenaCell[][] memArena, CueData[] cues) {

		EvalStep memAngle = memoryStep(x, y, memArena),
				cueAngle = cueStep(x, y, memArena, cues);

		double nx, ny, angle;
		
		if(cueAngle.angle == -1 || memAngle.eval/cues.length >= cueAngle.eval)  // normalize memory evaluation with numCues
			angle = memAngle.angle;
		else 
			angle = cueAngle.angle;

		nx = x + stepSize*Math.cos(angle);
		ny = y + stepSize*Math.sin(angle);
		
		return new ParticleData(nx, ny, angle);
	}
}
