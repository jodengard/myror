import java.awt.*;
import java.awt.event.*;
import java.awt.color.*;
import java.math.*;
import java.util.Random;

public class TerrainView extends Canvas{
	
	private Terrain terrain;
	
	private int objectSize;	
	private int antSize;
	
	//experimental
	/** The offscreen buffer */
	private Graphics offscreenGraphics;

	/** Image representing the offscreen graphics */
	private Image offscreenImage;
	
	
	public TerrainView(int terrainSize, int numberOfAnts, int objectSize)
	{
		//terrain = new Terrain(terrainSize, numberOfAnts);
		this.objectSize = objectSize;
		this.antSize = objectSize;
		
		setSize(terrainSize*objectSize,terrainSize*objectSize);
		
		this.setBackground(Color.white);
	}
	
	public void setTerrain(Terrain terrain)
	{
		this.terrain = terrain;
	}

	public void setTerrainState(int x, int y)
	{
		terrain.setState(x,y,1);
	}	
	
	public void update(Graphics g)
	{
		// Create an offscreen buffer (if we don't have one)
		if (offscreenImage == null) {
			Dimension size = getSize();

			offscreenImage = createImage(size.width, size.height);
			offscreenGraphics = offscreenImage.getGraphics();
		}

		// This will invoke painting correctly on the offscreen buffer
		super.update(offscreenGraphics);
		
		// Draw the contents of the offscreen buffer to screen.
		g.drawImage(offscreenImage, 0, 0, this);
	}
	
	public void paint(Graphics g)
	{
		if (terrain != null) {
			//int exploreStrength = 0;
			int foundStrength = 0;
			
			// Draw all tiles by going over them x-wise and y-wise.
			for(int i = 0; i < terrain.getTerrainSize(); i++) {
				for(int j = 0; j < terrain.getTerrainSize(); j++) {
					Color tColor = Color.WHITE;
					
					int tState = terrain.getState(i, j);
					
					//int tState = (int)(Math.random()*2.0);
					//Random r = new Random();
					//int tState = (int)(r.nextDouble()*2.0);
					
					//0 = blank, 1 = wall, 2 = food, 3 = nest, 4 = pheromone
					switch(tState)
					{
						case 1: tColor = Color.BLACK; break;
						case 2: 
							//tColor = Color.YELLOW; 
							foundStrength = (int)(255 * terrain.getExploreStrength(i, j)/terrain.maxFoodQuality);							
							tColor = new Color(255-foundStrength, 255, 255-foundStrength);
							break;
						case 3: tColor = Color.GRAY; break;
						case 4: 
							//exploreStrength = (int)(255 * terrain.getExploreStrength(i, j)/terrain.maxStrength);
							//if(exploreStrength > 255)exploreStrength = 255;
							foundStrength = (int)(255 * terrain.getFoundStrength(i, j)/terrain.maxStrength) + 30;
							if(foundStrength > 255)foundStrength = 255;				            
							tColor = new Color(255-foundStrength, 255/*-exploreStrength*/, 255); break;											
						default: break;
					}
					g.setColor(tColor);
					g.fillRect(i*objectSize , j*objectSize, objectSize, objectSize);
		            //System.out.println(exploreStrength);						
		            //System.out.println(foundStrength);						
					
				}
			}
			
			for(int i = 0; i < terrain.numberOfAnts; i++)
			{
				DoubleAnt tAnt = terrain.getAnt(i);
				//Ant tAnt = terrain.getAnt(i);
				
				if(tAnt.carriesFood())g.setColor(Color.ORANGE);
				else g.setColor(Color.RED);
				g.fillRect(tAnt.getX()*objectSize , tAnt.getY()*objectSize, antSize, antSize);
			}
		}	
	}
}

