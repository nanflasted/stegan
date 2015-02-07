import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//Intro window
public class Stegan extends JFrame {

	private Stegan thisWindow; //used to close window and activate Encode/Decode window

	//initialize intro window
	public Stegan() {
		thisWindow = this;

		// Set window options (visible later)
		setSize(300,120);
		setTitle("Steganographer");
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int)size.getWidth();
		int h = (int)size.getHeight();
		setLocation(w/2 - 150, h/2 - 60);
		setResizable(false);

		//Build panel to hold the text
		JPanel jp = new JPanel();
		JLabel jl = new JLabel("Would you like to encode or decode an image?");
		jp.add(jl);

		//Build other panels to hold buttons
		JPanel p0 = new JPanel(),
			   p1 = new JPanel(),
			   p2 = new JPanel(),
			   p3 = new JPanel(),
			   outer = new JPanel();
		JButton enc = new JButton("Encode"),
				dec = new JButton("Decode"),
				ccl = new JButton("Cancel");

		//CodeListener starts either Encode or Decode window; CloseListener exits program
		enc.addActionListener(new CodeListener());
		dec.addActionListener(new CodeListener());
		ccl.addActionListener(new CloseListener());

		//Add buttons to panels, add panels p1-p3 to one big panel
		p1.add(enc);
		p2.add(dec);
		p3.add(ccl);
		outer.add(p1);
		outer.add(p2);
		outer.add(p3);

		//Add panels to make the main window
		add(p0, BorderLayout.NORTH);
		add(jp, BorderLayout.CENTER);
		add(outer, BorderLayout.SOUTH);
		addWindowListener(new CloseListener());

		setVisible(true);
	}

	//Opens Encode or Decode window, hides this window
	public class CodeListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton)e.getSource();
			thisWindow.setVisible(false);
			if (b.getText().equals("Encode"))	;
				//init Encode window
			else								;
				//init Decode Window
		}
	}

	//Closes this window
	public class CloseListener extends WindowAdapter implements ActionListener {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	//Starts this window
	public static void main(String[] args) {
		Stegan s = new Stegan();
	}
}