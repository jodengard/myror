import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class AntThread extends Thread{
	
	public boolean allDone = false;
	public boolean paused = false;
	public int FPS = 1;
	public int currentFrame = 0;
	
	private TerrainView terrainView;
	private StatisticsPanel statisticsPanel;
	private Terrain terrain;	
	private Random r;
	
	//for multiple runs
	private int runs = 0;
	private int numberOfExperiments = 10;	
		
	LinkedList<Long> stats;	
	LinkedList<Integer> stats2;
	//waha
	
	public AntThread(TerrainView terrainView, StatisticsPanel statisticsPanel, int terrainSize, int numberOfAnts)
	{
		
		//Initialize random
		r = new Random();	
		
		//super();
		this.terrainView = terrainView;
		this.statisticsPanel = statisticsPanel;
		terrain = new Terrain(terrainSize, numberOfAnts, r);

		//Initialize Ants		
	}

	public void setTerrainFood(int x, int y, int quality)
	{
		terrain.setFood(x,y,quality);
		terrain.setFood(x+1,y,quality);
		terrain.setFood(x,y+1,quality);
		terrain.setFood(x+1,y+1,quality);
	}
	
	public void setTerrainPos(int x, int y, int type)
	{	
		switch(type)
		{
		case TerrainObject.BLANK:
			terrain.setBlank(x, y);
			terrain.setBlank(x+1, y);
			terrain.setBlank(x, y+1);
			terrain.setBlank(x+1, y+1);			
			break;
		
		case TerrainObject.WALL:
			terrain.setWall(x, y);
			terrain.setWall(x+1, y);
			terrain.setWall(x, y+1);
			terrain.setWall(x+1, y+1);			
			break;
		
		case TerrainObject.FOOD:
			terrain.setFood(x,y,terrain.maxFoodQuality);
			terrain.setFood(x+1,y,terrain.maxFoodQuality);
			terrain.setFood(x,y+1,terrain.maxFoodQuality);
			terrain.setFood(x+1,y+1,terrain.maxFoodQuality);
			break;
			
		}
		
	}

	public void setDecaySpeed(double x)
	{
		// /100 works
		//terrain.linearDecaySpeed = 1.0 * terrain.maxStrength / terrain.numberOfAnts / 100 /100 * x;
		terrain.exponentialDecaySpeed = x;
	}
	
	public double getDecaySpeed()
	{
		return terrain.exponentialDecaySpeed;
	}
	
    public void run() {
        while (true) {
			if (allDone) {
                return;
            }
        	//System.out.println("waha");
        	//update ants			
        	if(!paused)
        	{
				terrain.update(r);
				//if(terrain.iteration % (3*terrain.pollInterval) == 0)writeAnts();
				
				//new stuff
				if(terrain.iteration % (3*terrain.pollInterval) == 0)
				{
					//start over
					saveAnts();
					runs++;
					System.out.println("run #" + runs + " completed with last iteration " + terrain.iteration);
					if(runs > numberOfExperiments-1)
					{
						writeMultipleAnts();
						allDone = true;
						break;
					}
					int numberOfAnts = terrain.numberOfAnts;
					int terrainSize = terrain.terrainSize;
					terrain = new Terrain(terrainSize, numberOfAnts, r);					
				}
				
        	}
        	currentFrame++;
			if(currentFrame > FPS / 40)
			{
				currentFrame = 0;
				statisticsPanel.updateFoodLabel(terrain.totalFoodCollected);
				statisticsPanel.updateIterationLabel(terrain.iteration);
	            terrainView.setTerrain(terrain);				
				terrainView.repaint();
			}
			try {
				this.sleep(1000/FPS);
			}
			catch (InterruptedException e) {
				allDone = true;
			}        	
        }
    }
    //add file writing here
    public void writeAnts()
    {
    	/*
		try{
		    // Create file 
			FileWriter fstream = new FileWriter("values.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			LinkedList<Long> tStats = terrain.getStatistics();
			for(int i = 0; i < tStats.size()-1; i++)
			{
				out.write(Long.toString(tStats.get(i)) + " ");
			}
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}*/
        //use buffering
    		
    	Writer output;
    	Writer output2;
    	Writer output3;
    	
		try {			
			output = new BufferedWriter(new FileWriter("values.txt"));
			output2 = new BufferedWriter(new FileWriter("settings.txt"));
			output3 = new BufferedWriter(new FileWriter("papervalues.txt"));
			
	        try {
	        	
	            //FileWriter always assumes default encoding is OK!
	  			LinkedList<Long> tStats = terrain.getStatistics();
	  			for(int i = 0; i < tStats.size(); i++)
	  			{
	  				output.write(Long.toString(tStats.get(i)) + " ");
	  			}
	  			
	  			LinkedList<Integer> tStats2 = terrain.getmarksList();
	  			for(int i = 0; i < tStats2.size(); i++)
	  			{
	  				output3.write(Integer.toString(tStats2.get(i)) + " ");
	  			}
	  			
	  			//write settings
	  			output2.write(Integer.toString(terrain.numberOfAnts) + " ");
	  			output2.write(Double.toString(terrain.exponentialDecaySpeed) + " ");
	  			//output2.write(Double.toString(terrain.exponentialDecaySpeed) + " ");	  			
	          }
	          finally {
	            output.close();
	            output2.close();
	            output3.close();	            
	          }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("File written");
    	
    }
    
    public void saveAnts()
    {
    	if(runs == 0)
    	{
			stats = terrain.getStatistics();
			stats2 = terrain.getmarksList();    		
    	}
    	else
    	{
			LinkedList<Long> tStats = terrain.getStatistics();
			for(int i = 0; i < tStats.size(); i++)
			{
				long temp = 0;
				temp = stats.get(i);
				if(runs == 1)temp /= numberOfExperiments;				
				stats.set(i, temp + tStats.get(i)/numberOfExperiments);
			}
			
			LinkedList<Integer> tStats2 = terrain.getmarksList();
			for(int i = 0; i < tStats2.size(); i++)
			{
				int temp = 0;
				temp = stats2.get(i);
				if(runs == 1)temp /= numberOfExperiments;
				stats2.set(i, temp + tStats2.get(i)/numberOfExperiments);
			}
    	}   	
    }
    
    public void writeMultipleAnts()
    {
    	Writer output;
    	Writer output2;
    	Writer output3;
    	
		try {			
			output = new BufferedWriter(new FileWriter("values.txt"));
			output2 = new BufferedWriter(new FileWriter("settings.txt"));
			output3 = new BufferedWriter(new FileWriter("papervalues.txt"));
			
	        try {
	        	
	            //FileWriter always assumes default encoding is OK!
	  			for(int i = 0; i < stats.size(); i++)
	  			{
	  				output.write(Long.toString(stats.get(i)) + " ");
	  			}
	  			
	  			for(int i = 0; i < stats2.size(); i++)
	  			{
	  				output3.write(Integer.toString(stats2.get(i)) + " ");
	  			}
	  			
	  			//write settings and scenario
	  			output2.write(Integer.toString(terrain.numberOfAnts) + " ");
	  			output2.write(Double.toString(terrain.exponentialDecaySpeed) + " ");
	  			output2.write(Integer.toString(numberOfExperiments) + " ");	  			
	  			output2.write(Double.toString(terrain.deltaT) + " ");	  			
	  			output2.write(Long.toString(terrain.pollInterval) + " ");	  			
	  			output2.write(Long.toString(terrain.pollInterval2) + " ");	  			

	  			for(int i = 0; i < terrain.foodDistances.size(); i++)
	  			{
	  				output2.write(Double.toString(terrain.foodDistances.get(i)) + " ");
	  				output2.write(Double.toString(terrain.foodQualities.get(i)) + " ");
	  				output2.write(Long.toString(terrain.foodAppearanceIterations.get(i)) + " ");
	  			}	  			
	  			
	  			//output2.write(Double.toString(terrain.exponentialDecaySpeed) + " ");	  			
	          }
	          finally {
	            output.close();
	            output2.close();
	            output3.close();	            
	          }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("File written");    	
    }
}
