package backEnd;

public class DataReporter extends MazeParameters {

	private static final long serialVersionUID = 1L;

	public void printTrialArena(MazeArena arena) {
		int i, j;
		GridCell[][] memoryArena = arena.memoryArena;
		
		System.out.println("TRIAL\n\n");
		System.out.println("Start location: (" + arena.startCell.x + ", " + arena.startCell.y + ")");
	 
		System.out.println("Platform location: (" + arena.platform.x + ", " + arena.platform.y + ")");

		System.out.println("Path:");
		for(GridPoint g: arena.trialPath)
			System.out.print("(" + g.x + ", " + g.y + ")  ");
		System.out.println();

		System.out.println("Heat map of visits:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else
					System.out.print(arena.trialArena[i][j].visits + "\t");
			}
			System.out.println("\n\n");
		}

		System.out.println("Average Direction Vector:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(arena.trialArena[i][j].visits > 0 && arena.trialArena[i][j].angleToNext != -1)
					System.out.print(radToDeg(arena.trialArena[i][j].angleToNext) + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}	
		
		
		System.out.println("Center of Mass: (" + arena.centerOfMass.x + ", " + arena.centerOfMass.y + ")");
		 
		System.out.println("Angle to Center of Mass:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memoryArena[i][j].visits > 0)
					System.out.print(radToDeg(memoryArena[i][j].angleToCom) + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}

		System.out.println("ComWeight:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memoryArena[i][j].visits > 0)
					System.out.printf("%.3f\t", memoryArena[i][j].comWeight);
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
		
		System.out.println("PlatWeight:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memoryArena[i][j].visits > 0)
					System.out.printf("%.3f\t", memoryArena[i][j].platWeight);
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
	 
		System.out.println("Distance of COM to platform: " + (1/arena.inverseDistPlatCom));
		System.out.println("Inverse Distance: " + arena.inverseDistPlatCom);
	}
	
	public void printMemoryArena(MazeArena maze) {
		int i, j, total=0;
		GridCell[][] memoryArena = maze.memoryArena;
		
		System.out.println("MEMORY\n\n");
		
		System.out.println("Visits:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else
					System.out.print(memoryArena[i][j].visits + "\t");
			}
			System.out.println("\n\n");
		}
		
		System.out.println("Trials:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else
					System.out.print(memoryArena[i][j].trials + "\t");
			}
			System.out.println("\n\n");
		}
		
		System.out.println("Successes:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else
					System.out.print(memoryArena[i][j].successes + "\t");
			}
			System.out.println("\n\n");
		}

		System.out.println("Average Direction Vector:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memoryArena[i][j].visits > 0 && memoryArena[i][j].angleToNext != -1)
					System.out.print(radToDeg(memoryArena[i][j].angleToNext) + "\t");
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
		
		System.out.println("Product of comWeight and PlatWeight:");
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				if(centerDist(i,j) > radius)
					System.out.print("\t");
				else if(memoryArena[i][j].visits > 0)
					System.out.printf("%.3f\t", memoryArena[i][j].comWeight * memoryArena[i][j].platWeight);
				else
					System.out.print(-1 + "\t");
			}
			System.out.println("\n\n");
		}
		
		for(i=0; i<DIAMETER; i++) {
			for(j=0; j<DIAMETER; j++) {
				total += memoryArena[i][j].visits;
			}
		}
		System.out.println("Total visits:" + total + "\n\n\n");
	}
	
	public void printVisualCue(VisualCue cue) {

		System.out.println("CUE\n\n");
		System.out.println("Cue Number " + cue.cueNumber);

		System.out.println("Confidence:");
		for(int i=0; i<DIAMETER; i++) {
			for(int j=0; j<DIAMETER; j++) {
				if(centerDist(i, j) > radius) 
					System.out.print("\t");
				else 
					System.out.printf("%.3f\t", cue.confidence[i][j]);
			}
			System.out.println("\n\n");
		}

		System.out.println("Indicated Direction:");
		for(int i=0; i<DIAMETER; i++) {
			for(int j=0; j<DIAMETER; j++) {
				if(centerDist(i, j) > radius)  
					System.out.print("\t");
				else 
					System.out.print(radToDeg(cue.angleToNext[i][j]) + "\t");
			}
			System.out.println("\n\n");
		}
	}
	
	public void printSimulation(WaterMazeSimulation sim) {
		printTrialArena(sim.maze);
		printMemoryArena(sim.maze);
		for(VisualCue c: sim.visCues.cues)
			printVisualCue(c);
		System.out.println("Successes: " + sim.successes);
	}
}
