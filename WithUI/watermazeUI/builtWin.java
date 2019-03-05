package watermazeUI;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import backEnd.Simulation;
import backEnd.PhysData.PathType;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class builtWin {

	private JFrame frame;
	private JTextField cueEntry;
	private JLabel status;
	int size = 500;
	int baseX = 50, baseY = 50;
	Simulation s;
	int numCues = 0;
	boolean cueSet = false;
	PathType[] path;
	mazeDrawing mazeDisplay;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					builtWin window = new builtWin();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public builtWin() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 700, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton runIt = new JButton("Run");
		runIt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(s == null) {
					showStatus("Enter the number of cues before running");
					return;
				}
				s.runSimulation();
				path = s.maze.path;
				mazeDisplay.initialize(s, path);
				mazeDisplay.repaint();
			}
		});
		runIt.setBounds(30, 50, 89, 23);
		frame.getContentPane().add(runIt);
		
		JLabel cueLabel = new JLabel("Cues:");
		cueLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		cueLabel.setBounds(30, 110, 50, 14);
		frame.getContentPane().add(cueLabel);
		
		cueEntry = new JTextField();
		cueEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!cueSet) {
					try {
						numCues = Integer.parseInt(cueEntry.getText());
						s = new Simulation(1, numCues);
						cueSet = true;
						showStatus("Simulation initialised. Number of cues: " + numCues);
					} catch(NumberFormatException ne) {
						showStatus("Please enter the number of cues in the textbox");
					}
				}
				else 
					showStatus("Number of cues already set to " + numCues);				
			}
		});
		cueEntry.setBounds(30, 125, 86, 20);
		frame.getContentPane().add(cueEntry);
		cueEntry.setColumns(10);
		
		mazeDisplay = new mazeDrawing();
		mazeDisplay.setBounds(250, 11, 424, 390);
		frame.getContentPane().add(mazeDisplay);
		
		status = new JLabel();
		status.setBounds(10, 387, 230, 14);
		frame.getContentPane().add(status);
		
	}
	
	private void showStatus(String message) {
		status.setText(message);
	}

}
