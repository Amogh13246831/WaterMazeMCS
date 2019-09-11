package frontEnd;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;
import backEnd.*;

public class MazeDrawing extends JPanel {

	private static final long serialVersionUID = 1L;
	int size = 400;
	int baseX = 50, baseY = 10;
	WaterMazeSimulation sim;
	DataReporter reporter;
	ArrayList<GridPoint> path;
	boolean displayLocs = false;
	JLabel lblSuccesses = new JLabel("Initial");
	JLabel lblResult = new JLabel("");
	
	public MazeDrawing() {
		setLayout(null);
		lblSuccesses.setFont(new Font("Tahoma", Font.PLAIN, 16));
		
		lblSuccesses.setBounds(10, 11, 78, 23);
		add(lblSuccesses);
		
		lblResult.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblResult.setBounds(10, 39, 78, 23);
		add(lblResult);
	}
	
	public void initialize(int fileNo, int numCues) {
		GridPoint[] cueLocs = new GridPoint[] {new GridPoint(13, 3), new GridPoint(0, 6), new GridPoint(8, 0), new GridPoint(12, 12)};
		sim = new WaterMazeSimulation(1, Arrays.copyOfRange(cueLocs, 0, numCues));
		reporter = new DataReporter();
	}
	
	public void runSim() {
		sim.runSimulation();
		reporter.printSimulation(sim);
		path = sim.maze.trialPath;
	}
	
	int scale(int x) {
		return x * (size/MazeParameters.DIAMETER);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.WHITE);
		g.drawOval(baseX, baseY, scale(14), scale(14));
		
		if(sim != null) {
			g.setColor(Color.RED);
			g.fillOval(baseX + scale(sim.maze.platform.x)-5, baseY+scale(sim.maze.platform.y)-5,
					10, 10);
			
			g.setColor(Color.GREEN);
			for(VisualCue vc: sim.visCues.cues)
				g.fillOval(baseX + scale(vc.position.x)-5, baseY+scale(vc.position.y)-5,
						10, 10);
			
			if(path != null) {
				g.setColor(Color.BLUE);
			
				g.drawLine(baseX + scale(sim.maze.startCell.x), baseY + scale(sim.maze.startCell.y), 
						baseX + scale(path.get(0).x), baseY + scale(path.get(0).y));
			
				for(int i=0; i<path.size()-1; i++) {
					g.drawLine(baseX+scale(path.get(i).x), baseY+scale(path.get(i).y), 
							baseX+scale(path.get(i+1).x), baseY+scale(path.get(i+1).y));
				}
			}			
			lblSuccesses.setText("Successes");
			lblResult.setText(sim.successes + "/" + sim.totalTrials);	
		}
	}
}
