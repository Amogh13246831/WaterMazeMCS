package backEnd;

import java.io.Serializable;

public class VisualCues extends PhysData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	CueData[] cues;
	int numCues;
	
	public VisualCues(PathType[] locs, int platX, int platY) {
		numCues = locs.length;
		cues = new CueData[numCues]; 
		
		for(int i=0; i<numCues; i++) {
			cues[i] = new CueData(i, locs[i].x, locs[i].y);
			cues[i].setDetails(platX, platY);
		}
	}
	
	public void setCues(PathType[] locs, int platX, int platY) {
		for(int i=0; i<numCues; i++) {
			cues[i] = new CueData(i, locs[i].x, locs[i].y);
			cues[i].setDetails(platX, platY);
		}
	}
	
	void putCues() {  // print all cues	
		for(int i=0; i<numCues; i++)
			cues[i].printCue();
	}
	
	void updateCues(ArenaCell[][] arena) {
		/*
	  		modify the direction pointed to by and confidence in a cue, cell-by-cell,
	  		by computing weight similar to comwt for confidence offset dCi,
	  		and computing Ci = (Ci + dCi)/(summation Ci+dCi) to normalise,
	  		and modifying direction Di as DiCi + Tavg(1-Ci), 
	  		updating Ci and Di independently for each cue i, in each vell visited in the trial
		*/
		int i, j, k, offset;
		double totalconf;
		CueData oldcues[] = new CueData[maxCues];

		for(i=0; i<numCues; i++)
			oldcues[i] = new CueData(cues[i]);       // store old values to use in modifying

		// update confidence based on offset between average direction and cue vector
		for(i=0; i<diameter; i++)
			for(j=0; j<diameter; j++)
				if(arena[i][j].visited > 0 && arena[i][j].dirVect != -1) { // valid cell visited	
					for(k=0; k<numCues; k++) { // for each cue k, compute new confidence Ck+dCk				 
						offset = radToDeg(oldcues[k].randVect[i][j] - arena[i][j].dirVect);
						if(offset < 0) 
							offset = -offset; // modulus of angle distance
						if(offset > 360-offset)
							offset = 360 - offset;  // the smaller distance is considered 
						cues[k].confidence[i][j] += 1 - (offset/18)*0.1;
					}
				}
		// divide confidence of each cue by total confidence of all cues, for a cell
		for(i=0; i<diameter; i++)
			for(j=0; j<diameter; j++)
				if(arena[i][j].visited > 0 && arena[i][j].dirVect != -1) { // for each valid cell			 
					totalconf = 0;
					for(k=0; k<numCues; k++)
						totalconf += cues[k].confidence[i][j]; // find summation Ck+dCk
					if(totalconf > 0)
						for(k=0; k<numCues; k++)
							cues[k].confidence[i][j] /= totalconf; // divide each Ck+dCk by summation 
				}
		// modify direction pointed to based on offset from average, and confidence
		for(i=0; i<diameter; i++)
			for(j=0; j<diameter; j++)
				if(arena[i][j].visited > 0 && arena[i][j].dirVect != -1) { // valid cell visited				
					for(k=0; k<numCues; k++) { // for each cue, compute new direction 					
						totalconf = oldcues[k].confidence[i][j];
						cues[k].randVect[i][j] *= totalconf; 
						cues[k].randVect[i][j] += arena[i][j].dirVect*(1-totalconf);
					}
				}
	}
}
