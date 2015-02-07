import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

//Creates a window for the user to specify settings and encode stuff.
public class EncodeWindow extends JFrame {

	// Window parts
	private JSlider slider;
	private JComboBox cb;
	private JCheckBox chk, chk2;
	private JTextField path, path2;
	private JLabel slabel, clabel;
	private JButton jb;
	private JLabel stg, dataSize;
	private int px = 78;

	//Initializes window
	public EncodeWindow() {

		// Set window options (setVisible later)
		setSize(500,300);
		setTitle("Encoder");
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int)size.getWidth();
		int h = (int)size.getHeight();
		setLocation(w/2 - 250, h/2 - 150);
		setResizable(false);
		setLayout(new GridLayout(5,1));

		//Init panels
		JPanel[] jp = new JPanel[5];
		for (int i = 0; i < 5; i++)
			jp[i] = new JPanel();

		/***********\
		Set up panels
		\***********/

		//Panel 1: Slider
		JSlider slider = new JSlider(1,8);
		this.slider = slider;
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(1);
		slider.addChangeListener(new SliderListener());
		JLabel slabel = new JLabel("4");
		this.slabel = slabel;
		JLabel slabel2 = new JLabel("Bits per px");
		jp[0].add(slabel2);
		jp[0].add(slider);
		jp[0].add(slabel);

		//Panel 2: Combo Box and Compression
		String[] s = {"Darken", "Lighten", "Red", "Green", "Blue", "Auto"};
		JComboBox box = new JComboBox(s);
		cb = box;
		JLabel clabel = new JLabel("Encoding");
		this.clabel = clabel;
		JLabel comp = new JLabel("Compress it?");
		JCheckBox check = new JCheckBox();
		JLabel encr = new JLabel("Encrypt it?");
		JCheckBox check2 = new JCheckBox();
		chk = check;
		chk2 = check2;
		jp[1].add(clabel);
		jp[1].add(box);
		jp[1].add(comp);
	    jp[1].add(check);
	    jp[1].add(encr);
	    jp[1].add(check2);

		//Panel 3: File with info
		JTextField tf = new JTextField(25);

		//Check for previous data path
		String ext = null;
		try {
			FileReader fr = new FileReader(System.getProperty("user.dir") + "\\encode.txt");
			StringBuilder sb;
			sb = new StringBuilder();
			int i = fr.read();
			while (i != 13) {
				sb.append((char)i);
				i = fr.read();
			}
			fr.close();
			tf.setText(sb.toString());
			ext = sb.toString().substring(sb.toString().length() - 3, sb.toString().length());
		}catch(Exception x){}
		path = tf;
		tf.setEditable(false);
		JButton jb = new JButton("Browse...");
		jb.addActionListener(new BrowseListener(0));
		JLabel blabel = new JLabel("File location");
		jp[2].add(blabel);
		jp[2].add(tf);
		jp[2].add(jb);

		//Panel 4: Mask loc'n.
		JTextField tf2 = new JTextField(25);
		ext = null;
		try {
			FileReader fr = new FileReader(System.getProperty("user.dir") + "\\encode.txt");
			StringBuilder sb = new StringBuilder();
			int i = fr.read();
			while (i != 13)
				i = fr.read();
			i = fr.read();
			while (i != -1) {
				sb.append((char)i);
				i = fr.read();
			}
			fr.close();
			tf2.setText(sb.toString());
			ext = sb.toString().substring(sb.toString().length() - 3, sb.toString().length());
		}catch(Exception x){}
		path2 = tf2;
		tf2.setEditable(false);
		JButton jb2 = new JButton("Browse...");
		jb2.addActionListener(new BrowseListener(1));
		JLabel mlabel = new JLabel("Mask location");
		jp[3].add(mlabel);
		jp[3].add(tf2);
		jp[3].add(jb2);

		//Panel 5: Encode/Cancel buttons
		JLabel stg = new JLabel("Total storage available: 0 bytes");
		JLabel dataSize = new JLabel("Size of data: 0 bytes");
		this.stg = stg;
		this.dataSize = dataSize;

		//Check previous encode file
		// 1) get filepaths
		String firstPath = null, secondPath = null;
		String exten = null;
		try {
			FileReader fr = new FileReader(System.getProperty("user.dir") + "\\encode.txt");
			StringBuilder sb = new StringBuilder();
			int i = fr.read();
			while (i != 13) {
				sb.append((char)i);
				i = fr.read();
			}
			i = fr.read();
			i = fr.read();
			firstPath = sb.toString();
			sb = new StringBuilder();
			while (i != -1) {
				sb.append((char)i);
				i = fr.read();
			}
			secondPath = sb.toString();
			fr.close();

			//2) Make new files
			File data = new File(firstPath),
				 pic = new File(secondPath);
			if (data.length() > 0)
				updateDS();
			exten = secondPath.substring(secondPath.length() - 3, secondPath.length());
			if (exten.equals("bmp")) {
				BufferedImage b = ImageIO.read(pic);
				this.px = b.getWidth() * b.getHeight();
				updateStg();
			}
		}catch(Exception e){}

		JPanel jpanel = new JPanel(new GridLayout(2,1));
		jpanel.add(stg);
		jpanel.add(dataSize);
		JButton enc = new JButton("Encode"),
				ccl = new JButton("Cancel");
		enc.addActionListener(new EncodeListener());
		this.jb = enc;
		enc.setEnabled(false);
		ccl.addActionListener(new CclListener());
		jp[4].add(jpanel);
		jp[4].add(enc);
		jp[4].add(ccl);

		//Make main window
		for (int i = 0; i < 5; i++)
			add(jp[i]);

		if (firstPath != null) {
			if (!firstPath.equals("") && exten != null && exten.equals("bmp"))
				this.jb.setEnabled(true);
			else
				System.out.println(firstPath.equals(""));
		}
		addWindowListener(new CclListener());
		setVisible(true);
	}

	private void updateStg() {
		long fileCap = (long)((double)(slider.getValue() * (px - 78))/8);
		if (fileCap % (1099511627776L) != fileCap)
			stg.setText("Total storage available: " + fileCap / (1099511627776L) + " TB");
		else if (fileCap % (1073741824) != fileCap)
			stg.setText("Total storage available: " + fileCap / (1073741824) + " GB");
		else if (fileCap % (1048576) != fileCap)
			stg.setText("Total storage available: " + fileCap / (1048576) + " MB");
		else if (fileCap % 1024 != fileCap)
			stg.setText("Total storage available: " + fileCap / 1024 + " KB");
		else
			stg.setText("Total storage available: " + fileCap + " bytes");
	}

	private void updateDS() {
		long fileSize = new File(path.getText()).length();
		if (fileSize % (1099511627776L) != fileSize)
			dataSize.setText("Size of data: " + fileSize / (1099511627776L) + " TB");
		else if (fileSize % (1073741824) != fileSize)
			dataSize.setText("Size of data: " + fileSize / (1073741824) + " GB");
		else if (fileSize % (1048576) != fileSize)
			dataSize.setText("Size of data: " + fileSize / (1048576) + " MB");
		else if (fileSize % 1024 != fileSize)
			dataSize.setText("Size of data: " + fileSize / 1024 + " KB");
		else
			dataSize.setText("Size of data: " + fileSize + " bytes");
	}

	public class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider s = (JSlider)(e.getSource());
			int sliderVal = s.getValue();
			slabel.setText(sliderVal + "");
			updateStg();
		}
	}

	public class CclListener extends WindowAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}

	public class BrowseListener implements ActionListener {
		private int bnum;
		public BrowseListener(int i) {
			bnum = i;
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int i = fc.showOpenDialog(EncodeWindow.this);
			File f = null;
			String ext = null;

			if (i != JFileChooser.APPROVE_OPTION)
				return;
			else {
				f = fc.getSelectedFile();
				String filepath = f.getPath();
				if (bnum == 0)
					EncodeWindow.this.path.setText(filepath);
				else if (bnum == 1)
					EncodeWindow.this.path2.setText(filepath);

				ext = filepath.substring(filepath.length()-3, filepath.length());
				if (bnum == 1 && ext.equals("bmp") && path.getText() != null && !path.getText().equals(""))
					jb.setEnabled(true);
				else if (bnum == 0 && path2.getText() != null && !path2.getText().equals("")
						 && path2.getText().substring(path2.getText().length()-3, path2.getText().length()).equals("bmp"))
					jb.setEnabled(true);
				else
					jb.setEnabled(false);
			}

			if (bnum == 1 && f != null && ext.equals("bmp")) {
				BufferedImage b = null;
				try {
					b = ImageIO.read(f);
				} catch (Exception x) {
				}
				int h = b.getHeight(),
					w = b.getWidth(),
					px = h * w;
				EncodeWindow.this.px = px;
				updateStg();
			}

			if (bnum == 1 && !ext.equals("bmp")) {
				px = 78;
				updateStg();
			}

			if (bnum == 0)
				updateDS();
		}
	}

	public class EncodeListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//Find parent directory
			String s = path.getText();
			int i = s.length()-1;
			while (s.charAt(i) != "\\".charAt(0))
				i--;
			i++;
			String dir = s.substring(0, i);

			//Get PW
			String pw = JOptionPane.showInputDialog("Set a password for this encoding (optional):");

			//Call encoder
			String encoding = (String)(cb.getSelectedItem());
			boolean comp = chk.isSelected();
			boolean encryption = chk2.isSelected();
			try {
				new MaskEncoderII(new File(path.getText()), new File(path2.getText()), encoding, slider.getValue(), comp, encryption, pw);
			}catch (Exception E) {}

			//After encoding, inform user of success
			EncodeWindow.this.setVisible(false);
			JDialog d = new JDialog(EncodeWindow.this, "Encoding Complete");
			d.setSize(450,120);
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			int w = (int)size.getWidth();
			int h = (int)size.getHeight();
			d.setLocation(w/2 - 225, h/2 - 60);

			//Build panels
			JPanel top = new JPanel(),
			jpCtr = new JPanel(),
			btm = new JPanel(),
			total = new JPanel();
			JLabel jl = new JLabel("Done! Output saved to same directory as input data file (NOT image).");
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

			//Save path name to a file in current directory. Will be checked next time.
			String currentDir = System.getProperty("user.dir");
			PrintWriter wtr = null;
			String str = currentDir + "\\encode.txt";
			try {
				wtr = new PrintWriter(str);
			}catch (FileNotFoundException x) {}
			wtr.print(path.getText());
			wtr.println();
			wtr.print(path2.getText());
			wtr.close();
		}
	}

	//Tester method
	public static void main(String[] args) {
		new EncodeWindow();
	}
}