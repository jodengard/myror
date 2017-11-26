import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.math.*;
import java.io.*;

public class AntGUI extends Panel {
	private Button startButton;
	private Button stopButton;
	private Button pauseButton;
	
	private Scrollbar fpsScrollbar;
	private Scrollbar decayScrollbar;
	
	private Choice terrainObjectChoice;
	
	private Label fpsLabel;	
	private Label decayLabel;	
	private Label spacerLabel;
	private Label choiceLabel;
	
	private TerrainView terrainView;
	private Panel panel;
	private AntThread thread;
	//private Terrain terrain;
	//private Terrain terrainView;
	public StatisticsPanel statisticsPanel;

	private final int terrainSize = 100;
	//private final int numberOfAnts = 100;
	private final int numberOfAnts = 1000;
	
	private final int objectSize = 5;

	private int activeTerrainObjectType = 0;
	
	public AntGUI()
	{
		panel = new Panel();
		statisticsPanel = new StatisticsPanel();
		startButton = new Button("Start");
		stopButton = new Button("Stop");
		pauseButton = new Button("Pause");
		
		fpsScrollbar = new Scrollbar (Scrollbar.HORIZONTAL);
		//fpsScrollbar.setSize(20);
		fpsScrollbar.setMaximum (40);
		fpsScrollbar.setMinimum(0);
		
		decayScrollbar = new Scrollbar (Scrollbar.HORIZONTAL);
		decayScrollbar.setMaximum (110);
		decayScrollbar.setMinimum(0);
		
		terrainObjectChoice = new Choice();
		terrainObjectChoice.addItem("BLANK");
		terrainObjectChoice.addItem("WALL");
		terrainObjectChoice.addItem("FOOD");
		
		//fpsScrollbar.setMinimumSize(new Dimension(1000,10));
		//fpsScrollbar.setUnitIncrement(5);
		spacerLabel = new Label("*************************");
		fpsLabel = new Label("FPS limit");
		decayLabel = new Label("Pheromone decay");		
		choiceLabel = new Label("Object type");
		
		panel.setLayout(new GridLayout(0,1));
		
		panel.add(startButton);
		panel.add(pauseButton);		
		panel.add(stopButton);
		panel.add(spacerLabel);
		panel.add(fpsLabel);		
		panel.add(fpsScrollbar);
		panel.add(decayLabel);
		panel.add(decayScrollbar);
		panel.add(choiceLabel);
		panel.add(terrainObjectChoice);
		
		startButton.addActionListener(new GUIListener());
		pauseButton.addActionListener(new GUIListener());		
		stopButton.addActionListener(new GUIListener());
		
		fpsScrollbar.addAdjustmentListener(new ScrollbarListener());
		decayScrollbar.addAdjustmentListener(new ScrollbarListener());
		terrainObjectChoice.addItemListener(new choiceListener());
		
		//panel.setVisible(true);	
		terrainView = new TerrainView(terrainSize, numberOfAnts, objectSize);
		terrainView.addMouseListener(new TerrainListener());

		//terrain = new Terrain(100);
		//terrainView.setTerrain(terrain);
		
		this.setLayout(new FlowLayout());
		panel.add(statisticsPanel);		

		add(panel);		
		add(terrainView);
		//thread = new Thread();
	}
	
	private class GUIListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			//if(e.getSource() == startButton)terrainView.repaint();
			//else if(e.getSource() == stopButton)terrainView.repaint();
			
			if(e.getSource() == startButton)
			{
				System.out.println("Startbutton pressed");
				if(thread == null){
					thread = new AntThread(terrainView, statisticsPanel, terrainSize, numberOfAnts);				
					thread.start();
				}
				else
				{
					if(thread.allDone)
					{
						thread = new AntThread(terrainView, statisticsPanel, terrainSize, numberOfAnts);				
						thread.start();
						
						//update labels
						fpsLabel.setText("FPS limit");
						fpsLabel.repaint();
					
						decayLabel.setText("Pheromone decay");
						decayLabel.repaint();						
					}
						
				}
				thread.allDone = false;
				
			}
			else if(e.getSource() == stopButton)
			{
				System.out.println("Stopbutton pressed");
				if(thread != null)
				{
					thread.allDone = true;
				}				
			}
			else if(e.getSource() == pauseButton)
			{
				System.out.println("Pausebutton pressed");
				if(thread != null)
				{
					if(!thread.paused)thread.paused = true;
					else thread.paused = false;
				}				
			}
			
		}
	}
	
	private class TerrainListener implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent e) {
			System.out.println("Mousebutton pressed");
			int x = (int)(1.0*e.getX()/(objectSize));
			int y = (int)(1.0*e.getY()/(objectSize));

			if(thread != null)
			{
				thread.setTerrainPos(x,y,activeTerrainObjectType);
			}
			System.out.println(activeTerrainObjectType);
			//terrain.setState(3,3,1);
			//terrainView.setTerrain(terrain);
			//terrainView.repaint();
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub			
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	 private class ScrollbarListener implements AdjustmentListener
	 {

		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			if(e.getSource() == fpsScrollbar)
			{
				if(thread != null)
				{
					thread.FPS = 5 + 10 * fpsScrollbar.getValue();
					fpsLabel.setText("FPS limit: " + (10 * fpsScrollbar.getValue() + 5));
					fpsLabel.repaint();
				}
			}
			else if(e.getSource() == decayScrollbar)
			{
				if(thread != null)
				{
					thread.setDecaySpeed(1.0*decayScrollbar.getValue()/100);
					decayLabel.setText("Pheromone decay: " + 1.0*decayScrollbar.getValue()/100);
					decayLabel.repaint();
				}
			}
			
		}
		 
	 }
	 
	 private class choiceListener implements ItemListener
	 {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getSource() == terrainObjectChoice)
			{
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String name = (String) e.getItem();
					System.out.println(name);
					if(name == "BLANK")activeTerrainObjectType = TerrainObject.BLANK;
					if(name == "WALL")activeTerrainObjectType = TerrainObject.WALL;
					if(name == "FOOD")activeTerrainObjectType = TerrainObject.FOOD;
					System.out.println(activeTerrainObjectType);				}
				
			}
		}
		 
	 }
	 
}
