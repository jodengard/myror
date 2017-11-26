import java.awt.*;
import java.awt.event.*;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Frame frame = new Frame();
		
		Panel antGUI = new AntGUI();
		frame.setTitle("Ants");
		
		frame.add(antGUI);
		frame.pack();
		
		frame.addWindowListener(new AntWindowListener());
		frame.setVisible(true);
		frame.requestFocus();
	}

}

class AntWindowListener extends WindowAdapter {
	/**
	 * Called when the user clicks the close button on the window.
	 * (the X-button in the upper right corner in Windows)
	 */
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
}