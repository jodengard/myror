
public class TerrainObject {
    public enum TerrainType { BLANK, GOAL, FOOD, WALL, PHEROMONE}
	
	
	private int x;
	private int y;
	//private TerrainType type;
	private int type;	//0 = blank, 1 = wall, 2 = food, 3 = nest, 4 = pheromone
	
	//types
	public static final int BLANK = 0;
	public static final int WALL = 1;
	public static final int FOOD = 2;
	public static final int NEST = 3;
	public static final int PHEROMONE = 4;
	
	private double foundStrength;	//food found pheromone
	private double exploreStrength;	//exploration pheromone
		
	TerrainObject()
	{
		x = 0;
		y = 0;
		//type = TerrainType.WALL;
		type = 0;
		foundStrength = 0.0;
		exploreStrength = 0.0;
	}

	//TerrainObject(int _x, int _y, TerrainType _type)
	TerrainObject(int _x, int _y, int _type)
	{
		x = _x;
		y = _y;
		type = _type;
	}
	
	
	public void setX(int _x)
	{
		x = _x;
	}

	public void setY(int _y)
	{
		y = _y;
	}	
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public void setFoundStrength(double strength, double strengthThreshold, double maxStrength)
	{
		if(strength < strengthThreshold)
		{
			foundStrength = 0.0;
			if (exploreStrength < strengthThreshold && type == TerrainObject.PHEROMONE)type = TerrainObject.BLANK;
		}
		else if(strength > maxStrength)foundStrength = maxStrength;
		else foundStrength = strength;
	}

	public void setExploreStrength(double strength, double strengthThreshold, double maxStrength)
	{
		if(strength < strengthThreshold)
		{
			exploreStrength = 0.0;
			if (foundStrength < strengthThreshold && type == TerrainObject.PHEROMONE)type = TerrainObject.BLANK;
		}
		else if(strength > maxStrength)exploreStrength = maxStrength;
		else exploreStrength = strength;
	}
	
	/*public void setFoundStrength(double strength)
	{
		foundStrength = strength;
	}*/
	
	/*public void setExploreStrength(double strength)
	{
		exploreStrength = strength;
	}*/
	
	public double getFoundStrength()
	{
		return foundStrength;
	}	
	
	public double getExploreStrength()
	{
		return exploreStrength;
	}	
}