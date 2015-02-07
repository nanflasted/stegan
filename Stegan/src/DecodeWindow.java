import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

//Decode window. Pops up to let the user decode a mask.
public class DecodeWindow extends JFrame {

	private JTextField path;  //path of mask
	private JButton okButton; //used to control enabling/disabling of button

	//Initializes new decoding window
	public DecodeWindow() {

		//Set window options
		setSize(400,122);
		setTitle("Decoder");
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int)size.getWidth();
		int h = (int)size.getHeight();
		setLocation(w/2 - 200, h/2 - 61);
		setResizable(false);

		//Build label panel
		JPanel jp = new JPanel();
		JLabel jl = new JLabel("Choose the image you want to decode:");
		jp.add(jl);

		//Build file chooser panel
		JPanel dp = new JPanel();
		JTextField tf = new JTextField(25);
		path = tf;
		tf.setEditable(false);
		JButton jb = new JButton("Browse...");
		jb.addActionListener(new BrowseListener());
		dp.add(tf);
		dp.add(jb);

		//Build buttons panel
		JPanel bp = new JPanel();
		JButton ok = new JButton("OK"),
				ccl = new JButton("Cancel");
		okButton = ok;
		ok.addActionListener(new OKListener());
		ok.setEnabled(false);
		ccl.addActionListener(new CclListener());
		bp.add(ok);
		bp.add(ccl);

		//Build main window
		add(jp, BorderLayout.NORTH);
		add(dp, BorderLayout.CENTER);
		add(bp, BorderLayout.SOUTH);
		addWindowListener(new CclListener());

		setVisible(true);
	}

	//Listens for clicks to OK button.
	public class OKListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//Find parent directory
			String s = path.getText();
			int i = s.length()-1;
			while (s.charAt(i) != "\\".charAt(0))
				i--;
			i++;
			String dir = s.substring(0, i);

			//Get PW (if any)
			String pw = JOptionPane.showInputDialog("Enter a password (optional):");
			if (pw.equals(""))
				pw = null;

			//Call decoder
			File f = new File(s);
			try {
				Decoder.decoder(f,dir, pw);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(DecodeWindow.this, x.getMessage());
				return;
			}

			//After decoding, tell the user it has finished decoding. (Build a new window)
			DecodeWindow.this.setVisible(false);

			JDialog d = new JDialog(DecodeWindow.this, "Decoding Complete");
			d.setSize(300,120);
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			int w = (int)size.getWidth();
			int h = (int)size.getHeight();
			d.setLocation(w/2 - 150, h/2 - 60);

			//Build panels
			JPanel top = new JPanel(),
				   jpCtr = new JPanel(),
				   btm = new JPanel(),
				   total = new JPanel();
			JLabel jl = new JLabel("Done! Output saved to same directory as input.");
			jpCtr.add(jl);
			JButton ok = new JButton("OK");
			ok.addActionListener(new CclListener());
			btm.add(ok);

			//Build window
			total.setLayout(new BorderLayout());
			total.add(top, BorderLayout.NORTH);
			total.add(jpCtr, BorderLayout.CENTER);
			total.add(btm, BorderLayout.SOUTH);
			d.setContentPane(total);

			d.setVisible(true);
		}
	}

	//Creates a file chooser to choose the file. Enables OK button if the file is a BMP.
	public class BrowseListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int i = fc.showOpenDialog(DecodeWindow.this);
			File f;
			if (i == JFileChooser.APPROVE_OPTION) {
				f = fc.getSelectedFile();
				String filepath = f.getPath();
				DecodeWindow.this.path.setText(filepath);

				String ext = filepath.substring(filepath.length()-3, filepath.length());
				if (ext.equals("bmp"))
					okButton.setEnabled(true);
				else
					okButton.setEnabled(false);
			}
		}
	}

	//Closes program on Cancel or red X click.
	public class CclListener extends WindowAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}

	//Test method
	public static void main(String[] args) {
		new DecodeWindow();
	}
}