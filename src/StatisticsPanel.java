import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.math.*;
import java.io.*;

public class StatisticsPanel extends Panel{

	private Label foodCollectedLabel;
	private Label iterationLabel;
	
	public StatisticsPanel()
	{
		this.setLayout(new GridLayout(0,1));
		foodCollectedLabel = new Label("Food Collected: 0    ");
		iterationLabel = new Label("Iteration: 0           ");
		this.add(foodCollectedLabel);
		this.add(iterationLabel);
	}
	
	public void updateFoodLabel(long value)
	{
		foodCollectedLabel.setText("Food Collected: " + value + "    ");
	}	

	public void updateIterationLabel(long value)
	{
		iterationLabel.setText("Iteration: " + value + "    ");
	}	
	
}