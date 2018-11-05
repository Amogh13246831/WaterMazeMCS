package watermazeData;
import java.io.Serializable;

public class Arena extends PhysData implements Serializable {
	

	private static final long serialVersionUID = 1L;
	ArenaCell arena[][];
	ArenaCell memArena[][];

	boolean isFirstTrial;
	double invDist;
	int stepCount = 0;
	
	int center[];
	int startCell[];
	int platform[];
	int platQuad[];
	int centerMass[];
	
	class PathType implements Serializable {
		private static final long serialVersionUID = 1L;
		int x = 0;
		int y = 0;
	}
	PathType path[];
	
	public Arena()
	{
		arena = new ArenaCell[diameter][diameter];
		memArena = new ArenaCell[diameter][diameter];
		for(int i=0; i< diameter; i++)
			for(int j=0; j<diameter; j++)
			{
				arena[i][j] = new ArenaCell();
				memArena[i][j] = new ArenaCell();
			}
		
		center = new int[] {radius, radius};
		platQuad = new int[] {0, 90};
		startCell = new int[2];
		centerMass = new int[2];
		platform = new int[2];
		
		double pRad = 0.75 * radius;
		double pAngle = degToRad((platQuad[1]-platQuad[0])/2); 
		platform[0] = (int) (center[0] + pRad*Math.cos(pAngle));
		platform[1] = (int) (center[1] + pRad*Math.sin(pAngle));
		
		memArena[platform[0]][platform[1]].comWeight = 1;
		
		path = new PathType[steps];
		for(int i=0; i<steps; i++)
			path[i] = new PathType();
	}

	double centerDist(double x, double y) // distance of (x,y) from center of the arena
	{
		return Math.sqrt(Math.pow(center[0]-x,2) + Math.pow(center[1]-y,2)); 
	}
	
	void getNewArena()     
	{
		// initialise the maze and path storage to be used in the current trial
		arena = new ArenaCell[diameter][diameter];
		for(int i=0; i< diameter; i++)
			for(int j=0; j<diameter; j++)
			{
				arena[i][j] = new ArenaCell();
			}
		
		path = new PathType[steps];
		for(int i=0; i<steps; i++)
			path[i] = new PathType();
	}
	
	void randomizeStart()
	{
		/*
  		 randomize the angle of the start platform relative to the horizontal,
  		 ensuring that it does not lie between the angles of the platform quadrant
		*/

		double sAngle;
		int temp; 
		do
		{
			temp = (int) (Math.random()%360); // randomize angle
		}
		while(temp>platQuad[0] && temp<platQuad[1]); // until not in platform quadrant
 
		/*
  		 set the coordinates of the start position at the perimeter of the arena,
  		 at a valid (non-platform quadrant) angle
		*/ 
		sAngle = degToRad(temp);
		startCell[0] = (int) (center[0] + radius*Math.cos(sAngle)); 
		startCell[1] = (int) (center[1] + radius*Math.sin(sAngle)); 
	}
	
	void findAverageDirection()
	{
	 /*
	  compute the average direction-of-motion vector for each cell of the search path,
	  by dividing the summed-up vector by the number of visits
	 */
	 for(int i=0; i<diameter; i++)
		 for(int j=0; j<diameter; j++)  //sum of vectors per visit present in each cell
			 if(arena[i][j].visited > 0 && arena[i][j].dirVect != -1) // visited, has vector
				 arena[i][j].dirVect /= arena[i][j].visited; //divide by visits
	}

	void findCenterMass() 
	{
	 /*
	  compute the x-y coordinates of the center of mass relative arena[0][0], 
	  by assuming the map of visits to be a plane lamina of discrete masses,
	  where Xcom = summation MiXi/ summation Mi, Ycom = summation MiYi/ summation Yi
	 */

	 int i, j;
	 double xcom, ycom, totalweight;
	 xcom = ycom = totalweight = 0;
	 for(i=0; i<diameter; i++)
		 for(j=0; j<diameter; j++)
			 if(centerDist(i,j) <= radius)
			 {
				 totalweight += arena[i][j].visited; // summation Mi
				 xcom += i*arena[i][j].visited; // summation MiXi
				 ycom += j*arena[i][j].visited;  // summation MiYi
			 }

	 // assign the coordinates to center of mass
	 centerMass[0] = (int) (xcom/totalweight);
	 centerMass[1] = (int) (ycom/totalweight);
	}
	
	void findCenterAngle()
	{
	 /*
	  compute the angle to the center of mass from each cell of the search path,
	  where Tcom = arctan((Ycom-y)/(Xcom-x)), 
	  for cell coordinates (x,y) and center of mass coordinates (Xcom, Ycom)
	 */
	 int i, j;
	 for(i=0; i<diameter; i++)
		 for(j=0; j<diameter; j++)
			 if(centerDist(i,j)<=radius && arena[i][j].visited > 0) // valid cell visited
			 {
				 // inverse tangent of (Ycom - Yi)/(Xcom - Xj), -180 to +180 degrees
				 arena[i][j].centerAngle = Math.atan2(centerMass[1]-j, centerMass[0]-i);
				 if(arena[i][j].centerAngle < 0)
					 arena[i][j].centerAngle += degToRad(360); // convert negative angles to 180+
			 } 
	}
	
	void computeComWeight()
	{
	 /*
	  compute Wcom for each cell in the path with a direction vector,
	  where Wcom = 1-0.1*(floor(d/18)), where d is modulus of the deflection,
	  i.e. the difference between angle to the center of mass and average direction, 
	 */
	 int i, j, offset;
	 for(i=0; i<diameter; i++)
		 for(j=0; j<diameter; j++)
			 if(arena[i][j].visited > 0 && arena[i][j].dirVect != -1) // valid cell visited
			 {
				 offset = radToDeg(arena[i][j].centerAngle - arena[i][j].dirVect);
				 if(offset < 0) 
					 offset = -offset; // modulus of angle distance
				 if(offset > 360-offset)
					 offset = 360 - offset;  // the smaller distance is considered 
				 arena[i][j].comWeight = 1 - (offset/18)*0.1;
	   }
	}
	
	void computePlatWeight()
	{
	 /*
	  compute Wplat for the trial,
	  where Wplat = 1/D is the inverse of the cartesian distance,
	  D = sqrt((Xp-Xcom)^2 + (Yp-Ycom)^2), between the platform and center of mass
	 */
	 double dist;
	 dist = Math.sqrt(Math.pow(platform[0]-centerMass[0],2) + Math.pow(platform[1]-centerMass[1],2));
	 if(dist < 1) 
		 dist = 1; // make the inverse valid, and max reinforcement for < 1
	 invDist = 1/dist;
	 
	 // assign the value of Wplat to each cell
	 for(int i=0; i<diameter; i++)
		 for(int j=0; j<diameter; j++)
			 if(centerDist(i,j)<=radius && arena[i][j].visited > 0) 
			 {
				 arena[i][j].platWeight = invDist;
			 }
	}
	
	void updateMemory() 
	{                        
		/*
	  		update the stored data with the results of the current trial, 
	  		by averaging the parameters (average direction vector, Wcom, Wplat) for each cell.
	  		If there is no data in that memory cell, assign the trial data to that cell
		*/
		for(int i=0; i<diameter; i++)
			for(int j=0; j<diameter; j++)
				if(arena[i][j].visited > 0)
				{
					if(memArena[i][j].visited == 0) // first ever visit to the cell
					{
						memArena[i][j] = arena[i][j]; // assign current trial values
						continue;
					}
					/* 
	     				else, cell has been visited, 
	     				and  memarena or arena or both may not have direction vectors in that cell,
	     				if arena has no direction vector (last search step/ platform), no change,
	     				else if memarena has no direction vector, assign arena vector and CoM weight,
	     				else if both have them, then average them out and store to memarena,
	     				else if neither have them, do nothing,
	     				and compute average for platform weight and store to memarena
					*/ 

					if(arena[i][j].dirVect != -1) // cell in current trial has direction vector
					{
						if(memArena[i][j].dirVect == -1) // visited, but no stored vector
						{
							memArena[i][j].dirVect = arena[i][j].dirVect; // assign vector 
							memArena[i][j].comWeight = arena[i][j].comWeight;    // and CoM weight
						}
						else       // visited, and direction vector exists
						{
							// compute the average of average angles and CoM weights
							memArena[i][j].dirVect *= memArena[i][j].visited;
							memArena[i][j].dirVect += arena[i][j].dirVect*arena[i][j].visited; 
	      
							memArena[i][j].comWeight *= memArena[i][j].visited; 
							memArena[i][j].comWeight += arena[i][j].comWeight*arena[i][j].visited;

							memArena[i][j].dirVect /= memArena[i][j].visited + arena[i][j].visited;
							memArena[i][j].comWeight /= memArena[i][j].visited + arena[i][j].visited;      
						}
					}
					// else, no direction vector in this trial, so no update

					// average out platform weights
					memArena[i][j].platWeight *= memArena[i][j].visited;
					memArena[i][j].platWeight += arena[i][j].platWeight*arena[i][j].visited;

					// sum up the visits and divide by the total number of visits for average    
					memArena[i][j].visited += arena[i][j].visited;   
					memArena[i][j].platWeight /= memArena[i][j].visited;
				}    
				// else, cell not visited in this trial, so no memory change
	}

	void printArena()
	{ 
		//print all experiment parameters and current trial results

		int i, j;

		System.out.println("Start location: (" + startCell[0] + ", " + startCell[1] + ")");
	 
		System.out.println("Platform location: (" + platform[0] + ", " + platform[1] + ")");

		System.out.println("Path:");
		for(i=0; i<stepCount; i++)
			System.out.println("(" + path[i].x + ", " + path[i].y + ")");
		System.out.println();

		System.out.println("Heat map of visits:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else
					System.out.print(arena[i][j].visited + "\t");
			}
			System.out.println("\n\n");
		}

		System.out.println("Average Direction Vector:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(arena[i][j].visited > 0 && arena[i][j].dirVect != -1)
					System.out.print(radToDeg(arena[i][j].dirVect) + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}

		System.out.println("Center of Mass: (" + centerMass[0] + ", " + centerMass[1] + ")");
	 
		System.out.println("Angle to Center of Mass:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(arena[i][j].visited > 0)
					System.out.print(radToDeg(arena[i][j].centerAngle) + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}

		System.out.println("ComWeight:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(arena[i][j].visited > 0)
					System.out.print(arena[i][j].comWeight + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
	 
		System.out.println("Distance of COM to platform: " + (1/invDist));
		System.out.println("Inverse Distance: " + invDist);
	}

	void printStored()
	{
		// print the data stored in memory

		int i, j, total = 0;

		System.out.println("Average Direction Vector in Memory:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memArena[i][j].visited > 0 && memArena[i][j].dirVect != -1)
					System.out.print(radToDeg(memArena[i][j].dirVect) + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
	 
		System.out.println("ComWeight in Memory:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memArena[i][j].visited > 0)
					System.out.print(memArena[i][j].comWeight + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
		
		System.out.println("PlatWeight in Memory:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memArena[i][j].visited > 0)
					System.out.print(memArena[i][j].platWeight + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
		
		System.out.println("Product of Weights in Memory:");
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memArena[i][j].visited > 0)
					System.out.print(memArena[i][j].platWeight*memArena[i][j].comWeight + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
	 
		for(i=0; i<diameter; i++)
		{
			for(j=0; j<diameter; j++)
			{
				total += memArena[i][j].visited;
			}
		}
		System.out.println("Total visits:" + total);
	}	
}

