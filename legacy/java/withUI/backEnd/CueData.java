package backEnd;
import java.io.Serializable;

public class CueData extends PhysData implements Serializable {

	private static final long serialVersionUID = 1L;
	int cueNumber;  
	int xPos;
	int yPos;
	double platVect;
	double platDist;
	double randVect[][];
	double confidence[][];
	
	public CueData(int number, int x, int y) {
		cueNumber = number;
		xPos = x;
		yPos = y;	
		randVect = new double[diameter][diameter];
		confidence = new double[diameter][diameter];
	}
	
	public CueData(CueData other) {
		cueNumber = other.cueNumber;
		xPos = other.xPos;
		yPos = other.yPos;
		randVect = new double[diameter][diameter];
		confidence = new double[diameter][diameter];
		for(int i=0; i<diameter; i++)
			for(int j=0; j<diameter; j++) {
				randVect[i][j] = other.randVect[i][j];
				confidence[i][j] = other.confidence[i][j];
			}
		platVect = other.platVect;
		platDist = other.platDist;
	}
	
	void setDetails(int px, int py) {
		/* set platform distance and platform angle from the cue
		  platform distance the is cartesian distance between platform and cue
		 */
		 double celldist;
		 platDist = Math.sqrt(Math.pow(px-xPos,2) + Math.pow(py-yPos,2));
		 // inverse tangent of (Yplat - y)/(Xplat - x), -pi to +pi radian
		 platVect = Math.atan2(py-yPos, px-xPos);
		 if(platVect < 0)
			 platVect += degToRad(360); // convert negative angles to pi+

		 // initialise confidence from each cell to be (1/Dplat)*(1/Dcell)
		 for(int i=0; i<diameter; i++)
			 for(int j=0; j<diameter; j++) {
				 celldist = Math.sqrt(Math.pow(i-xPos,2) + Math.pow(j-yPos,2));
				 confidence[i][j] = 1/(platDist*celldist);
				 randVect[i][j] = Math.random()%(2*Math.PI);
			 }
	}
	
	void printCue() {
		System.out.println("Cue Number " + cueNumber);
		double centdist; 

		 // print the confidence and random vector arrays
		System.out.println("Confidence:");
		for(int i=0; i<diameter; i++) {
			for(int j=0; j<diameter; j++) {
				centdist = Math.sqrt(Math.pow(radius-i,2) + Math.pow(radius-j,2)); // distance from C = (r,r)
				if(centdist > radius) 
					System.out.print("\t");
				else 
					System.out.printf("%.3f\t", confidence[i][j]);
			}
			System.out.println("\n\n");
		}

		System.out.println("Indicated Direction:");
		for(int i=0; i<diameter; i++) {
			for(int j=0; j<diameter; j++) {
				centdist = Math.sqrt(Math.pow(radius-i,2) + Math.pow(radius-j,2)); // distance from C = (r,r)
				if(centdist > radius) 
					System.out.print("\t");
				else 
					System.out.print(radToDeg(randVect[i][j]) + "\t");
			}
			System.out.println("\n\n");
		}
	}
}
