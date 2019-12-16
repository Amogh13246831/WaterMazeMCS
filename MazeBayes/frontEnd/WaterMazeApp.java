package frontEnd;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import backEnd.GridPoint;
import backEnd.WaterMazeSimulation;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;

public class WaterMazeApp {

	private JFrame frmWaterMazeSimulator;
	private JTextField cueEntry;
	private JLabel status;
	private JTextField iterGet;
	
	int iterations  = 1;
	int numCues = 0;
	boolean cueSet = false;
	MazeDrawing mazeDrawing;
	private JLabel lblCueDisplay;
	private JLabel lblCuesSetTo;
	private JTextField tfStoreFile;
	private JTextField tfLoadFile;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WaterMazeApp window = new WaterMazeApp();
					window.frmWaterMazeSimulator.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WaterMazeApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWaterMazeSimulator = new JFrame();
		frmWaterMazeSimulator.setTitle("Water Maze Simulator: Bayesian Model");
		frmWaterMazeSimulator.setBounds(100, 100, 700, 450);
		frmWaterMazeSimulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmWaterMazeSimulator.getContentPane().setLayout(null);
		
		JButton runIt = new JButton("Run");
		runIt.setFont(new Font("Tahoma", Font.PLAIN, 18));
		runIt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!cueSet) {
					showStatus("Enter the number of cues before running");
					return;
				}
				
				new Thread() {
					public void run() {
						for(int i=0; i<iterations; i++) {
							mazeDrawing.runSim();
							mazeDrawing.repaint();
							try {
								Thread.sleep(50);
							} catch(InterruptedException ie) {
								showStatus("Sleep Interrupted!");
							}
						}
					}
				}.start();
				
			}
		});
		runIt.setBounds(30, 11, 175, 45);
		frmWaterMazeSimulator.getContentPane().add(runIt);
		
		JLabel cueLabel = new JLabel("CUES");
		cueLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		cueLabel.setBounds(30, 77, 50, 14);
		frmWaterMazeSimulator.getContentPane().add(cueLabel);
		
		cueEntry = new JTextField();
		cueEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!cueSet) {
					try {
						numCues = Integer.parseInt(cueEntry.getText());
						if(numCues > 4)
							numCues = 4;
						mazeDrawing.initialize(1, numCues);
						cueSet = true;
						showStatus("Simulation initialised. Number of cues: " + numCues);
						
						if(numCues == 0) {
							lblCuesSetTo.setText("");
							lblCueDisplay.setText("");
						}
						else {
							lblCuesSetTo.setText("Cue Locations");
							String cuePos = "";
							GridPoint[] cueLocs = new GridPoint[] {new GridPoint(0, 80), new GridPoint(70, 0), new GridPoint(150, 75), new GridPoint(50, 145)};
							for(int i=0; i<numCues; i++) {
								cuePos += "(" + cueLocs[i].x + ", " + cueLocs[i].y + ") ";
							}
							lblCueDisplay.setText(cuePos);						
						}
					} catch(NumberFormatException ne) {
						showStatus("Please enter the number of cues in the textbox");
					}
				}
				else 
					showStatus("Number of cues already set to " + numCues);				
			}
		});
		cueEntry.setBounds(30, 92, 61, 20);
		frmWaterMazeSimulator.getContentPane().add(cueEntry);
		cueEntry.setColumns(10);
		
		mazeDrawing = new MazeDrawing();
		mazeDrawing.setBounds(250, 11, 424, 390);
		frmWaterMazeSimulator.getContentPane().add(mazeDrawing);
		
		status = new JLabel();
		status.setBounds(10, 387, 230, 14);
		frmWaterMazeSimulator.getContentPane().add(status);
		
		JLabel lblIterations = new JLabel("ITERATIONS");
		lblIterations.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIterations.setBounds(119, 77, 86, 14);
		frmWaterMazeSimulator.getContentPane().add(lblIterations);
		
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
		iterGet.setBounds(119, 92, 86, 20);
		frmWaterMazeSimulator.getContentPane().add(iterGet);
		iterGet.setColumns(10);
		
		JButton btnNewSimulation = new JButton("New Simulation");
		btnNewSimulation.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnNewSimulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cueSet = false;            //  cueSet determines the number of cues, hence a simulation epoch
				showStatus("New Simulation: Enter the number of cues to begin");
			}
		});
		btnNewSimulation.setBounds(30, 331, 175, 45);
		frmWaterMazeSimulator.getContentPane().add(btnNewSimulation);
		
		lblCuesSetTo = new JLabel("");
		lblCuesSetTo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblCuesSetTo.setBounds(30, 138, 175, 20);
		frmWaterMazeSimulator.getContentPane().add(lblCuesSetTo);
		
		lblCueDisplay = new JLabel("");
		lblCueDisplay.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblCueDisplay.setBounds(30, 158, 175, 20);
		frmWaterMazeSimulator.getContentPane().add(lblCueDisplay);
		
		JLabel lblNewLabel = new JLabel("RESULTS");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblNewLabel.setBounds(30, 189, 175, 23);
		frmWaterMazeSimulator.getContentPane().add(lblNewLabel);
		
		JButton btnTrial = new JButton("Trials");
		btnTrial.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnTrial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnTrial.setBounds(30, 210, 76, 23);
		frmWaterMazeSimulator.getContentPane().add(btnTrial);
		
		JButton btnSuccess = new JButton("Successes");
		btnSuccess.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnSuccess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnSuccess.setBounds(116, 210, 89, 23);
		frmWaterMazeSimulator.getContentPane().add(btnSuccess);

		tfStoreFile = new JTextField();
		tfStoreFile.setBounds(119, 258, 86, 20);
		frmWaterMazeSimulator.getContentPane().add(tfStoreFile);
		tfStoreFile.setColumns(10);
		
		tfLoadFile = new JTextField();
		tfLoadFile.setColumns(10);
		tfLoadFile.setBounds(119, 292, 86, 20);
		frmWaterMazeSimulator.getContentPane().add(tfLoadFile);
		
		JButton btnStore = new JButton("STORE");
		btnStore.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnStore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String fileName = tfStoreFile.getText();
				if(fileName.equals(""))
					showStatus("Enter STORE filename");
				else {
					mazeDrawing.sim.storeData(fileName);
					showStatus("Data stored!");
				}
			}
		});
		btnStore.setBounds(30, 255, 76, 23);
		frmWaterMazeSimulator.getContentPane().add(btnStore);
		
		JButton btnLoad = new JButton("LOAD");
		btnLoad.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String fileName = tfLoadFile.getText();
				if(fileName.equals(""))
					showStatus("Enter LOAD filename");
				else {
					mazeDrawing.sim = WaterMazeSimulation.readData(fileName);
					cueSet = true;
					showStatus("Data loaded, cues set!");
				}
			}
		});
		btnLoad.setBounds(30, 289, 76, 23);
		frmWaterMazeSimulator.getContentPane().add(btnLoad);
		
	}
	
	private void showStatus(String message) {
		status.setText(message);
	}
}
