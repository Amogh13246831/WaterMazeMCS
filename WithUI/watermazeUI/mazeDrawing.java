package watermazeUI;

import java.awt.*;
import javax.swing.*;
import backEnd.*;
import backEnd.PhysData.PathType;

public class mazeDrawing extends JPanel {

	private static final long serialVersionUID = 1L;
	int size = 400;
	int baseX = 30, baseY = 10;
	Simulation sim;
	PathType[] path;
	boolean displayLocs = false;

	public void initialize(int fileNo, int numCues) {
		if(numCues != 0) {
			displayLocs = true;
			repaint();
			numCues = 0;
		}
		sim = new Simulation(fileNo, numCues);
	}
	
	public void runSim() {
		sim.runSimulation();
		path = sim.maze.path;
	}
	
	int scale(int x) {
		return x * (size/PhysData.diameter);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.WHITE);
		g.drawOval(baseX, baseY, scale(14), scale(14));
		
		if(displayLocs == true) {
			for(int i=0; i<PhysData.diameter; i++)
			{	
				for(int j=0; j<PhysData.diameter; j++)
					if(sim.maze.centerDist(i,j) > PhysData.radius)
						g.drawString("\t", baseX + scale(i), baseY + scale(j));
					else
						g.drawString("(" + i + ", " + j + ")\t", baseX + scale(i), baseY + scale(j));
			}
			displayLocs = false;
		}
		
		if(sim != null) {
			g.setColor(Color.red);
			g.fillOval(baseX + scale(sim.maze.platform[0])-5, baseX+scale(sim.maze.platform[1])-5,
					10, 10);
			if(path != null) {
				g.setColor(Color.blue);
			
				g.drawLine(baseX + scale(sim.maze.startCell[0]), baseY + scale(sim.maze.startCell[1]), 
						baseX + scale(path[0].x), baseY + scale(path[0].y));
			
				for(int i=0; i<path.length-1; i++) {
					if(path[i+1].x == 0)
						break;
					System.out.println(path[i].x + "\t" + path[i].y);
					g.drawLine(baseX+scale(path[i].x), baseY+scale(path[i].y), 
							baseX+scale(path[i+1].x), baseY+scale(path[i+1].y));
				}
			}
		}
	}
	
}
