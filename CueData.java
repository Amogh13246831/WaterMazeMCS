package watermazeData;

public class CueData extends PhysData{
	
	int cueNumber;  
	int xPos;
	int yPos;
	double platVect;
	double platDist;
	double randVect[][];
	double confidence[][];
	
	public CueData(int number, int x, int y)
	{
		cueNumber = number;
		xPos = x;
		yPos = y;	
		randVect = new double[diameter][diameter];
		confidence = new double[diameter][diameter];
	}
	
	void setDetails(int px, int py)
	{
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
			 for(int j=0; j<diameter; j++)
			 {
				 celldist = Math.sqrt(Math.pow(i-xPos,2) + Math.pow(j-yPos,2));
				 confidence[i][j] = 1/(platDist*celldist);
				 randVect[i][j] = degToRad((int) (Math.random()%360));
			 }
	}
	
	void printCue()
	{
		System.out.println("Cue Number " + cueNumber);
		double centdist; 

		 // print the confidence and random vector arrays
		System.out.println("Confidence:");
		for(int i=0; i<diameter; i++)
		{
			for(int j=0; j<diameter; j++)
			{
				centdist = Math.sqrt(Math.pow(radius-i,2) + Math.pow(radius-j,2)); // distance from C = (r,r)
				if(centdist > radius) 
					System.out.print("\t");
				else 
					System.out.print(confidence[i][j] + "\t");
			}
			System.out.println("\n\n");
		}

		System.out.println("Indicated Direction:");
		for(int i=0; i<diameter; i++)
		{
			for(int j=0; j<diameter; j++)
			{
				centdist = Math.sqrt(Math.pow(radius-i,2) + Math.pow(radius-j,2)); // distance from C = (r,r)
				if(centdist > radius) 
					System.out.print("\t");
				else 
					System.out.print(randVect[i][j] + "\t");
			}
			System.out.println("\n\n");
		}
	}
}
