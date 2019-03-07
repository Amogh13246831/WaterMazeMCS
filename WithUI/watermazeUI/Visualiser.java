package watermazeUI;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import backEnd.*;
import backEnd.PhysData.PathType;

public class Visualiser extends Applet {

	private static final long serialVersionUID = 1L;
	int size = 500;
	int baseX = 50, baseY = 50;
	Simulation s;
	int numCues = 0;
	boolean cueSet = false;
	PathType[] path;

	public void init() {
		this.resize(1000, 800);
		
		Button b = new Button("Run");
		add(b);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(s == null) {
					showStatus("Enter the number of cues before running");
					return;
				}
				s.runSimulation();
				path = s.maze.path;
				repaint();
			}
		});
		
		TextField cue = new TextField(10);
		add(cue);
		cue.addTextListener(new TextListener() {
			public void textValueChanged(TextEvent te) {
				if(!cueSet) {
					try {
						numCues = Integer.parseInt(cue.getText());
						s = new Simulation(1, numCues);
						cueSet = true;
						showStatus("Simulation initialised. Number of cues: " + numCues);
					} catch(NumberFormatException e) {
						showStatus("Please enter the number of cues in the textbox");
					}
				}
				else 
					showStatus("Number of cues has already been set to " + numCues);				
			}
		});
	}
	
	int scale(int x) {
		return x * (size/PhysData.diameter);
	}
	
	public void paint(Graphics g) {
		if(s != null) {
			//g.drawLine(baseX + scale(7), baseY + scale(0), baseX + scale(14), baseY + scale(7));
			//g.drawLine(baseX + scale(7), baseY + scale(0), baseX + scale(0), baseY + scale(7));
			//g.drawLine(baseX + scale(7), baseY + scale(14), baseX + scale(14), baseY + scale(7));
			//g.drawLine(baseX + scale(7), baseY + scale(14), baseX + scale(0), baseY + scale(7));
			
			g.drawRect(baseX, baseY, scale(14), scale(14));
			g.drawOval(baseX, baseY, scale(14), scale(14));
			
			//g.drawRect(baseX, baseY, size, size);
			//g.drawOval(baseX, baseY, size, size);
			g.setColor(Color.red);
			g.fillOval(baseX + scale(s.maze.platform[0])-5, baseX+scale(s.maze.platform[1])-5,
					10, 10);
			if(path != null) {
				g.setColor(Color.blue);
			
				g.drawLine(baseX + scale(s.maze.startCell[0]), baseY + scale(s.maze.startCell[1]), 
						baseX + scale(path[0].x), baseY + scale(path[0].y));
			
				for(int i=0; i<path.length-1; i++) {
					if(path[i+1].x == 0)
						break;
					System.out.println(path[i].x + "\t" + path[i].y);
					g.drawLine(baseX+scale(path[i].x), baseY+scale(path[i].y), 
							baseX+scale(path[i+1].x), baseY+scale(path[i+1].y));
				
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
