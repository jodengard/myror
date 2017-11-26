import java.math.*;
import java.util.LinkedList;
import java.util.Random;

public class Terrain 
{
	//2x2 array with terrainObjects
	private TerrainObject terrainObjects[][];
	//private Ant ants[];
	private DoubleAnt ants[];
	
	public final int terrainSize;	//in cm
	public final int numberOfAnts;
	public final double pheromoneStrength = 100.0;
	public double maxStrength;	
	public double linearDecaySpeed;
	public double exponentialDecaySpeed = 0.9;	
	public final double strengthThreshold = 0.1;	
	
	public final double maxFoodQuality = 4.0;
		
	double deltaT = 1.0;	//seconds / iteration
	
	public double nestCoordinateX;
	public double nestCoordinateY;
	
	//recruitment
	private int recruitedAnts;
	
	//statistics
	public long totalFoodCollected = 0;		
	
	public long iteration = 0;
	public long foodUnitsCollected = 0;
	public long foodQualityCollected = 0;
	public long avgPathLength = 0;
	
	public long pollInterval = (long)(60*10/deltaT);	//every 10 minutes...  huuhh..
	//public long pollInterval = (long)(1*10/deltaT);	//every 10 minutes...  huuhh..

	public long pollInterval2 = 50;	//every 100 iterations
	
	private LinkedList<Long> statList;
	
	//from paper
	public int marksPer10minutesToNest = 0;
	public int marksPer10minutesFromNest = 0;
	
	public int closeMarks = 0;
	public int farMarks = 0;
	
	public int marksArray[];
	private LinkedList<Integer> marksList;
	
	public LinkedList<Double> foodDistances;
	public LinkedList<Double> foodQualities;
	public LinkedList<Long> foodAppearanceIterations;
	
	//end statistics
	
	public Terrain(int terrainSize, int numberOfAnts, Random r)
	{
		statList = new LinkedList<Long>();
		marksList = new LinkedList<Integer>();
		marksArray = new int[2];

		foodDistances = new LinkedList<Double>();
		foodQualities = new LinkedList<Double>();
		foodAppearanceIterations = new LinkedList<Long>();
		
		//maxStrength = numberOfAnts*pheromoneStrength;
		maxStrength = numberOfAnts*pheromoneStrength;		
		//maxStrength = pheromoneStrength;		
		
		//linearDecaySpeed = maxStrength / numberOfAnts / 100;
		linearDecaySpeed = maxStrength * numberOfAnts / 10000;
		exponentialDecaySpeed = Math.exp(Math.log((strengthThreshold/pheromoneStrength))/(1000/deltaT));
		System.out.println(exponentialDecaySpeed);
		
		
		//declaration
		this.terrainSize = terrainSize;
		this.numberOfAnts = numberOfAnts;
		
		recruitedAnts = numberOfAnts / 4;
		//recruitedAnts = numberOfAnts;
		
		terrainObjects = new TerrainObject[terrainSize][terrainSize];

		//initialization
		for(int i = 0; i < terrainSize; i++) {
			for(int j = 0; j < terrainSize; j++) {
				terrainObjects[i][j] = new TerrainObject(i,j,0);
			}
		}
		
		//this.numberOfAnts = numberOfAnts;
		//ants = new Ant[numberOfAnts];
		ants = new DoubleAnt[numberOfAnts];
		
		nestCoordinateX = 1.0*terrainSize/2+0.5+20;
		nestCoordinateY = 1.0*terrainSize/2+0.5;		
		
		for(int i = 0; i < numberOfAnts; i++)
		{
			//ants[i] = new Ant(terrainSize/2,terrainSize/2);
			
			//divide into scouts and recruits
			if(i < recruitedAnts)ants[i] = 
				new DoubleAnt((int)nestCoordinateX,(int)nestCoordinateY,r.nextDouble(),r.nextDouble(),true,deltaT);
			else ants[i] = 
				new DoubleAnt((int)nestCoordinateX,(int)nestCoordinateY,r.nextDouble(),r.nextDouble(),false,deltaT);
		}
		
		//0 = blank, 1 = wall, 2 = food, 3 = nest, 4 = food pheromone, 5 = exploring pheromone
				
		//set nest
		/*this.setNest(terrainSize/2, terrainSize/2);		
		this.setNest(terrainSize/2+1, terrainSize/2);
		this.setNest(terrainSize/2, terrainSize/2+1);
		this.setNest(terrainSize/2+1, terrainSize/2+1);*/	
		
		this.setNest((int)nestCoordinateX, (int)nestCoordinateY);		
		this.setNest((int)nestCoordinateX+1, (int)nestCoordinateY);		
		this.setNest((int)nestCoordinateX, (int)nestCoordinateY+1);		
		this.setNest((int)nestCoordinateX+1, (int)nestCoordinateY+1);		
		
		//nestCoordinateX = 1.0*terrainSize/2+0.5;
		//nestCoordinateY = 1.0*terrainSize/2+0.5;
						
		/*		
		//set food
		
		int foodDistanceX = 20;
		int foodDistanceY = 20;
		*/
		//loadScenario1();
	}
	
	public void loadScenario1()
	{
		int foodDistanceX = -60;
		int foodDistanceY = 0;
		
		if(iteration == 0)setFoodSquare((int)nestCoordinateX+foodDistanceX,(int)nestCoordinateY+foodDistanceY,4);
		/*setFoodSquare((int)nestCoordinateX-foodDistanceX,(int)nestCoordinateY+foodDistanceY,4);
		setFoodSquare((int)nestCoordinateX+foodDistanceX,(int)nestCoordinateY-foodDistanceY,4);
		setFoodSquare((int)nestCoordinateX-foodDistanceX,(int)nestCoordinateY-foodDistanceY,4);*/
		
		if(iteration == 0)setFoodSquare((int)nestCoordinateX-40,(int)nestCoordinateY-20,4);

		if(iteration == pollInterval)setFoodSquare((int)nestCoordinateX+15,(int)nestCoordinateY,4);
	}
	
	public void recruitAnts(int recruits, double angle)
	{
		if(recruitedAnts < numberOfAnts)
		{
			//System.out.println(recruitedAnts);
			int oldRecruitedAnts = recruitedAnts;
			recruitedAnts += recruits;
			if(recruitedAnts > numberOfAnts)recruitedAnts = numberOfAnts;
			
			for(int i = oldRecruitedAnts; i < recruitedAnts; i++)
			{
				ants[i].active = true;
				ants[i].setAngleOutside(angle);
			}
		}
	}
	
	public int getTerrainSize()
	{
		return terrainSize;
	}
	
	public int getState(int i, int j)
	{
		if(i < 0 || i > terrainSize - 1)return 1;	//wall -> out of range
		if(j < 0 || j > terrainSize - 1)return 1;	//wall -> out of range
			
		return terrainObjects[i][j].getType();
	}
	
	public void setState(int i, int j, int type)
	{
		if(i < 0 || i > terrainSize - 1)return;
		terrainObjects[i][j].setType(type);
	}
	
	public double getFoundStrength(int i, int j)
	{
		if(i < 0 || i > terrainSize - 1)return 0.0;	//wall -> out of range
		if(j < 0 || j > terrainSize - 1)return 0.0;	//wall -> out of range
			
		return terrainObjects[i][j].getFoundStrength();
	}	

	public double getExploreStrength(int i, int j)
	{
		if(i < 0 || i > terrainSize - 1)return 0.0;	//wall -> out of range
		if(j < 0 || j > terrainSize - 1)return 0.0;	//wall -> out of range
			
		return terrainObjects[i][j].getExploreStrength();
	}	
	
	public void setFoundStrength(int i, int j, double strength)
	{
		if(i < 0 || i > terrainSize - 1)return;
		terrainObjects[i][j].setFoundStrength(strength, strengthThreshold, maxStrength);
	}	

	public void setExploreStrength(int i, int j, double strength)
	{
		if(i < 0 || i > terrainSize - 1)return;
		terrainObjects[i][j].setExploreStrength(strength, strengthThreshold, maxStrength);
	}	

	public void setFood(int i, int j, double quality)
	{
		if(i < 0 || i > terrainSize - 1)return;
		this.setState(i, j, TerrainObject.FOOD);
		terrainObjects[i][j].setFoundStrength(0, 0, 0);
		terrainObjects[i][j].setExploreStrength(quality, 0, maxStrength);		
	}	

	public void setFoodSquare(int i, int j, double quality)
	{
		setFood(i,j,quality);
		setFood(i+1,j,quality);
		setFood(i,j+1,quality);
		setFood(i+1,j+1,quality);		
		
		foodDistances.add(Math.sqrt( 1.0*(i + 0.5 - nestCoordinateX)*(i + 0.5 - nestCoordinateX) + 
				(j + 0.5 - nestCoordinateY)*(j + 0.5 - nestCoordinateY)));
		foodQualities.add(quality);
		foodAppearanceIterations.add(iteration);		
	}	
	
	public void setFood(int i, int j)
	{
		if(i < 0 || i > terrainSize - 1)return;
		this.setState(i, j, TerrainObject.FOOD);
		//terrainObjects[i][j].setFoundStrength(maxStrength*100, 0, maxStrength*100);		
		//terrainObjects[i][j].setExploreStrength(0, 0, maxStrength);				
	}		
	
	public void setNest(int i, int j)
	{
		if(i < 0 || i > terrainSize - 1)return;
		this.setState(i, j, TerrainObject.NEST);
		//terrainObjects[i][j].setFoundStrength(maxStrength*100, 0, maxStrength*100);
		//terrainObjects[i][j].setExploreStrength(0, 0, maxStrength);					
	}
	
	public void setWall(int i, int j)
	{
		if(i < 0 || i > terrainSize - 1)return;
		terrainObjects[i][j].setFoundStrength(0, 0, maxStrength);		
		terrainObjects[i][j].setExploreStrength(0, 0, maxStrength);				

		//terrainObjects[i][j].setFoundStrength(-maxStrength);		
		//terrainObjects[i][j].setExploreStrength(-maxStrength);				
		
		this.setState(i, j, TerrainObject.WALL);		
	}

	public void setBlank(int i, int j)
	{
		if(i < 0 || i > terrainSize - 1)return;
		terrainObjects[i][j].setFoundStrength(0, 0, maxStrength);		
		terrainObjects[i][j].setExploreStrength(0, 0, maxStrength);				
		this.setState(i, j, TerrainObject.BLANK);		
	}
	
	
	public void update(Random r)
	{
		totalFoodCollected -= foodUnitsCollected;
		
		loadScenario1();
		iteration++;
		//update ants
		for(int i = 0; i < numberOfAnts; i++)
		{		
			ants[i].decide(this, r.nextDouble(), r.nextDouble());	//double ants			
		}
		
		//update pheromone strength
		for(int i = 0; i < terrainSize; i++) {
			for(int j = 0; j < terrainSize; j++) {
				if(terrainObjects[i][j].getType() == 4)
				{
					//linear
					//terrainObjects[i][j].setFoundStrength(terrainObjects[i][j].getFoundStrength() - linearDecaySpeed, strengthThreshold, maxStrength);
					//terrainObjects[i][j].setExploreStrength(terrainObjects[i][j].getExploreStrength() - linearDecaySpeed, strengthThreshold, maxStrength);					
					//exponential
					terrainObjects[i][j].setFoundStrength(terrainObjects[i][j].getFoundStrength() * exponentialDecaySpeed, strengthThreshold, maxStrength);
					terrainObjects[i][j].setExploreStrength(terrainObjects[i][j].getExploreStrength() * exponentialDecaySpeed, strengthThreshold, maxStrength);					
					
				}
			}
		}
		
		totalFoodCollected += foodUnitsCollected;
		//statistics
		if((iteration % pollInterval) == 0)pollStatistics();
		if((iteration % pollInterval2) == 0)pollStatistics2();
		
	}
	
	//public Ant getAnt(int index)
	public DoubleAnt getAnt(int index)	
	{
		return(ants[index]);
	}
	
	private void pollStatistics2()
	{
		statList.add(foodUnitsCollected);
		statList.add(avgPathLength);
		avgPathLength = 0;
		foodUnitsCollected = 0;	
		//System.out.println("statistics polled at iteration " + iteration);
	}
	
	private void pollStatistics()
	{	
		//statList.add(foodUnitsCollected);
		//statList.add(avgPathLength);
		
		marksList.add(marksPer10minutesToNest);
		marksList.add(marksPer10minutesFromNest);
		
		marksPer10minutesToNest = 0;
		marksPer10minutesFromNest = 0;
		
		marksList.add(closeMarks);
		closeMarks = 0;
		marksList.add(farMarks);
		farMarks = 0;
		
		//avgPathLength = 0;
	}
	
	public void updateMarkStatistics(double x, double y)
	{
		double nestDistance = Math.sqrt((x - nestCoordinateX)*(x - nestCoordinateX) + (y - nestCoordinateY)*(y - nestCoordinateY));
		if(nestDistance < 20)closeMarks++;
		else if(nestDistance > 40)farMarks++;
	}
	
	public LinkedList<Long> getStatistics()
	{
		return statList;	//sends reference?
	}
	
	public LinkedList<Integer> getmarksList()
	{
		return marksList;	//sends reference?
	}
	
}