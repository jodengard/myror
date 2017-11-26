
public class DoubleAnt {

	private double x;
	private double y;
	private double angle;
	//private double visibilityRange = 4;	//1 --> only sees itself
	private double visibilityRange = 6;	//1 --> only sees itself
	
	private double visibilityAngle = Math.PI/3;
	private double backupVisibilityAngle = visibilityAngle;
	private double maxDeltaAngle = Math.PI/3;

	private double homeAngleWeight = 0.5;
	private double homeAngleWeightPheromone = 0.5;
	
	
	private double speed = 1.0;	//i.e. speed, cm / s
	
	//private double ignoreProbability = 0.01;
	private double ignoreProbability = 0.0;
	
	private boolean carriesFood;
	private boolean dropsPheromone;
	
	private double pheromoneWeight = 100.0;

	public long carryTime;
	
	public long dropsPheromoneDuration = 200;
	private long lastFoundTime;
	
	private double foodFoundQuality = 0;
	
	private long foodFirstFoundTime = 0;
	
	int hitWall = 0;	
	
	//divide into recruiters and recruits
	public boolean recruiter;
	public boolean active;	
	
	//to make it closer to the paper	
	private double invFrequency = 10000;		//drops per passage between nest and food
	private long lastDroppedTime = 0;
	//private boolean waitTilNest = false;
		
	public long nestDelay = 250;
	public long feedDelay = 50;
	//private int status = 0;
	private long arrivedTime = 0;
	
	private int passages = 0;	
	
	public DoubleAnt(double x, double y, double seed, double seed2, boolean recruiter, double deltaT)
	{
		this.x = x;
		this.y = y;
		
		carriesFood = false;
		dropsPheromone = false;
		angle = 2.0 * Math.PI * (seed - 0.5);
		
		//added
		this.recruiter = recruiter;
		if(recruiter)this.active = true;
		else this.active = false;
		
		arrivedTime = (int)(60 * deltaT * 5);	//release all ants after 5 minutes
		
		//should be larger than 0 with ~ 30% probability
		if(seed2 < 0.3)
		{	
			invFrequency = 60 / speed / deltaT / 2.5;		//4 on average, take distribution into account and spread it out
			if(!recruiter)invFrequency *= 2.5;
			if(seed2 < 0.3 && seed2 > 0.3 - 0.3*6/7)invFrequency /= 0.65/0.03;
			if(seed2 < 0.3 - 0.3*6/7 && seed2 > 0.3 - 0.3*5/7)invFrequency /= 0.2/0.03;			
			//recruiters mark 3 times more
		}
	}
	
	public void decide(Terrain terrain, double seed, double seed2)
	{	
		if((terrain.getState((int)x, (int)y) == TerrainObject.NEST) && 
				(terrain.iteration - arrivedTime) > nestDelay)active = true;
		if((terrain.getState((int)x, (int)y) == TerrainObject.FOOD && 
				(terrain.iteration - arrivedTime) > feedDelay))active = true;
		
		if(active)
		{
			//check if it's ON food or nest
			//pheromoneWeight = terrain.maxStrength;
			
			/*if(dropsPheromone)
				if(terrain.iteration - lastFoundTime > dropsPheromoneDuration)
					dropsPheromone = false;*/
			
			switch(terrain.getState((int)x, (int)y))
			{
				case TerrainObject.WALL:		//move to upper right corner if stuck in obstacle
					//x = terrain.terrainSize/2;
					//y = terrain.terrainSize/2;
					x = 0;
					y = 0;				
					break;
				case TerrainObject.FOOD:	//food
					//if(lastFoundTime - terrain.iteration > 5)dropsPheromone = false;
					if(!carriesFood){
						carriesFood = true;
						if(passages < 6)dropsPheromone = true;					
						passages++;
						
						carryTime = terrain.iteration + feedDelay;
						setAngle(Math.PI);		
						foodFoundQuality = terrain.getExploreStrength((int)x, (int)y);
						
						arrivedTime = terrain.iteration;
						active = false;
						
						if(foodFirstFoundTime == 0)foodFirstFoundTime = terrain.iteration;
					}
					lastFoundTime = terrain.iteration;
					break;
				case TerrainObject.NEST:	//nest
					//visibilityAngle = Math.PI;	//look around
					if(carriesFood)
					{
						//only drops if it carried food back home
						if(passages > 6)dropsPheromone = false;
						passages++;
						
						//statistics
						carryTime = terrain.iteration - carryTime;
						//if(carryTime < 60)System.out.println(carryTime + " " + terrain.iteration);
						//if(carryTime < 60)carryTime = 60;						
						terrain.foodUnitsCollected++;
						terrain.foodQualityCollected += foodFoundQuality;
						terrain.avgPathLength += carryTime;
						
						setAngle(Math.PI);

						arrivedTime = terrain.iteration;
						active = false;
						
						//recruit ants
						if(recruiter)terrain.recruitAnts((int)foodFoundQuality, angle);						
					}				
					carriesFood = false;
					lastFoundTime = terrain.iteration;				
					break;
				default:
					if(dropsPheromone && ((terrain.iteration - lastDroppedTime) > invFrequency))
					{
						terrain.setState((int)x, (int)y, TerrainObject.PHEROMONE);
						terrain.setFoundStrength((int)x, (int)y, terrain.getFoundStrength((int)x, (int)y) +
								foodFoundQuality / terrain.maxFoodQuality * terrain.pheromoneStrength);
						
						lastDroppedTime = terrain.iteration;
						
						//statistics
						if(carriesFood)terrain.marksPer10minutesToNest++;
						else 
						{
							terrain.marksPer10minutesFromNest++;
							//System.out.println("waha");
						}
						
						terrain.updateMarkStatistics(x,y);
						
						//if(carriesFood)terrain.setFoundStrength((int)x, (int)y, terrain.getFoundStrength((int)x, (int)y) + terrain.pheromoneStrength);
						//else terrain.setExploreStrength((int)x, (int)y, terrain.getExploreStrength((int)x, (int)y) + terrain.pheromoneStrength);
						//System.out.println(foodFoundQuality);
					}
			}
			
			if(active)
			{
				//update angle				
				double dAngle = 0.0;
				double tX = 0;
				double tY = 0;
				boolean found = false;
				boolean foundTarget = false;
				//System.out.println("angle=" + angle);
				
				//calculate angle to the strongest pheromone concentration within the visibility cone (angle and distance)
				for(int i = - (int)visibilityRange; i <= (int)visibilityRange; i++)
				{
					for(int j = - (int)visibilityRange; j <= (int)visibilityRange; j++)
					{
						if(foundTarget)break;
						if(j != 0 && i != 0)
						{
							if(foundTarget)break;
							if(terrain.getState((int)x + i, (int)y + j) == TerrainObject.FOOD && !carriesFood)
							{
								tX = 1.0 * i;
								tY = 1.0 * j;
								found = true;
								foundTarget = true;
								break;
							}
							if(terrain.getState((int)x + i, (int)y + j) == TerrainObject.NEST && carriesFood)
							{
								tX = 1.0 * i;
								tY = 1.0 * j;
								found = true;
								foundTarget = true;							
								break;
							}
							if(terrain.getState((int)x + i, (int)y + j) == TerrainObject.PHEROMONE)
							{
								if(Math.sqrt(i*i + j*j) < visibilityRange /*&& 2.0 < Math.sqrt(i*i + j*j)*/)
								//if(true)
								{
								double tempDANGLE = fixAngle(getAngle(0,0, i, j) - angle);
								//System.out.println("tempDANGLE=" + tempDANGLE);
									if(tempDANGLE < visibilityAngle && tempDANGLE > -visibilityAngle)
									{
										//dAngle += tempDANGLE * terrain.getFoundStrength((int)x + i, (int)y + j) / terrain.maxStrength * pheromoneWeight;
										//dAngle += tempDANGLE;
										tX += 1.0 * i * terrain.getFoundStrength((int)x + i, (int)y + j) / terrain.maxStrength * pheromoneWeight;
										tY += 1.0 * j * terrain.getFoundStrength((int)x + i, (int)y + j) / terrain.maxStrength * pheromoneWeight;
										if(!found && terrain.getFoundStrength((int)x + i, (int)y + j) > 0)found = true; 									
									}		
								}
							}
						}
					}
				}
				if(terrain.getState((int)x, (int)y) == TerrainObject.NEST)visibilityAngle = backupVisibilityAngle;		
				
				dAngle = fixAngle(getAngle(0, 0, tX, tY) - angle);
				//if(dAngle > maxDeltaAngle)dAngle = Math.abs(dAngle)/dAngle;
				
				//System.out.println(tX);
				//System.out.println(tY);		
				//System.out.println(found);		
				//if(dAngle == 0 || seed2 < ignoreProbability)dAngle = 2.0 * maxDeltaAngle * (seed - 0.5);
				
				//if no pheromones have been found
				if((!found || seed2 < ignoreProbability) && !foundTarget)
				//if(true)
				{
					if(carriesFood)
					{
						//head home randomly
						dAngle = homeAngleWeight*fixAngle(getAngle(x, y, terrain.nestCoordinateX, terrain.nestCoordinateY) - angle) + 2.0*maxDeltaAngle * (seed - 0.5);
						//System.out.println(dAngle);
					}
					else
					{
						//wander randomly
						dAngle = 2.0 * maxDeltaAngle * (seed - 0.5);
					}
				}
				else
				{
					if(carriesFood)dAngle = homeAngleWeightPheromone * fixAngle(getAngle(x, y, terrain.nestCoordinateX, terrain.nestCoordinateY) - angle - dAngle) + dAngle;			
				}
		
				//update position and check collision with wall -> try new angle		
				setAngle(dAngle);
				//if(hitWall == 0)setAngle(dAngle);
				//else hitWall--;
				
				double dX = Math.cos(angle);
				double dY = Math.sin(angle);
				
		
				dX = speed * terrain.deltaT * Math.cos(angle);
				dY = speed * terrain.deltaT * Math.sin(angle);
				
				
				int i = 0;
				int sign = 1;
				if(dAngle < 0)sign = -1;
				//System.out.println(sign);
				while(terrain.getState((int)(x + dX), (int)(y + dY)) == 1 && i < 16)
				{
					//if(hitWall == 0)hitWall = 5;
					//if(i > 1)dropsPheromone = false;
					setAngle(sign * Math.PI/8);
					//setAngle(Math.PI/16);			
					dX = speed * Math.cos(angle);
					dY = speed * Math.sin(angle);
					i++;
				}
				x += dX;
				y += dY;
						
				
				//make better
				/*
				if(terrain.getState((int)(x + dX), (int)(y + dY)) != 1)
				{
					x += dX;
					y += dY;
				}
				else
				{
					x -= dX;
					y -= dY;
					setAngle(Math.PI);
					//dropsPheromone = false;
				}
				*/
				//System.out.println(angle);		
				//System.out.println(dAngle);
				//System.out.println(dX);		
				//System.out.println(dY);
			}
		}
	}
	
	
	public static double getAngle(double x1, double y1, double x2, double y2)
	{
		if(y2 == y1 && x2 == x1)return 0;
		if(y2 == y1 && x2 < x1)return Math.PI;
		double tAngle = Math.atan((y2-y1)/(x2-x1));
		//check which quadrant the angle belongs to		
		//2nd
		if((y2-y1) > 0.0 && (x2 - x1) < 0.0)tAngle += Math.PI;
		//3rd
		if((y2-y1) < 0.0 && (x2 - x1) < 0.0)tAngle += Math.PI;
		
		return fixAngle(tAngle);
	}	

	public static double fixAngle(double argAngle)
	{	
		//rotate so that it's within - PI and PI
		while(argAngle >= Math.PI)argAngle -= Math.PI*2;
		while(argAngle < -Math.PI)argAngle += Math.PI*2;	
		return argAngle;
	}
	
	public void setAngleOutside(double angle)
	{
		this.angle = angle;
	}
	
	private void setAngle(double dAngle)
	{
		angle += dAngle;
		
		//rotate so that it's within - PI and PI
		while(angle >= Math.PI)angle -= Math.PI*2;
		while(angle < -Math.PI)angle += Math.PI*2;	
	}
	
	public int getX()
	{
		return (int)x;		
	}

	public int getY()
	{
		return (int)y;		
	}
	
	public boolean carriesFood()
	{
		return carriesFood;
	}
}
