package watermazeUI;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class builtWin {

	private JFrame frame;
	private JTextField cueEntry;
	private JLabel status;
	private JTextField iterGet;
	
	int iterations  = 1;
	int numCues = 0;
	boolean cueSet = false;
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
				if(!cueSet) {
					showStatus("Enter the number of cues before running");
					return;
				}
				
				new Thread() {
					public void run() {
						for(int i=0; i<iterations; i++) {
							mazeDisplay.runSim();
							mazeDisplay.repaint();
							try {
								Thread.sleep(500);
							} catch(InterruptedException ie) {
								showStatus("Sleep Interrupted!");
							}
						}
					}
				}.start();
				
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
						mazeDisplay.initialize(1, numCues);
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
		
		JLabel lblIterations = new JLabel("Iterations:");
		lblIterations.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIterations.setBounds(30, 200, 86, 14);
		frame.getContentPane().add(lblIterations);
		
		iterGet = new JTextField();
		iterGet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					iterations = Integer.parseInt(iterGet.getText());
					if(iterations < 1)
						throw new NumberFormatException();
					showStatus("Iterations set to " + iterations);
				} catch(NumberFormatException ne) {
					showStatus("Enter a valid number of iterations");
				}
			}
		});
		iterGet.setBounds(30, 215, 86, 20);
		frame.getContentPane().add(iterGet);
		iterGet.setColumns(10);
		
	}
	
	private void showStatus(String message) {
		status.setText(message);
	}
}
