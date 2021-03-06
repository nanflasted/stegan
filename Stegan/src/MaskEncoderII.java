import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import sun.awt.image.ToolkitImage;

//Electric Boogaloo












import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


public class MaskEncoderII {

	public File infoFile;
	public File maskFile;
	String encoding;
	int bitsPerPixel;
	boolean compression;
	boolean encryption = false;
	String password;
	BufferedImage mask;
	int imageWidth;
	int imageHeight;
	InfoProcessor byteGetter;
	int arraySize = 64;
	int xPtr;
	int yPtr;
	int xRem;
	int yRem;
	int bPPmodValue;

	public MaskEncoderII(File infoFile, File maskFile, String encoding, int bPP, boolean compression, String password) throws Exception {
		this.infoFile = infoFile;
		this.maskFile = maskFile;
		mask = ImageIO.read(maskFile);
		this.encoding = encoding;
		this.bitsPerPixel = bPP;
		this.bPPmodValue = (int) Math.pow(2, bPP);
		this.compression = compression;
		System.out.println("Password: " + password);

		if(password != null)
			encryption = true;
		
		System.out.println("Compression? " + compression);
		
		if(compression){
			System.out.println("Compressing... ");
			CompressionTool.compress(infoFile);
		}

		this.password = password;
		imageWidth = mask.getWidth();
		imageHeight= mask.getHeight();
		byteGetter = new InfoProcessor(infoFile, bitsPerPixel, encryption, password);
		//Works for evens, no compression
		try{

			System.out.println("Writing Header...");
			writeHeader();
			System.out.println("Writing File...");
			readInfoFile();
			System.out.println("Filling in....");
			fillRest();
			System.out.println("Done!");
		} catch (Exception e){
			e.printStackTrace();
		}

		ImageIO.write(mask, "bmp", maskFile);
	}

	private void fillRest() throws Exception {

		while(xPtr != imageWidth-1 || yPtr != imageHeight - 1){
			colorChange((int)(Math.random()*bPPmodValue),bPPmodValue);
		}

	}
	
	//preview example!
	public MaskEncoderII(File picFile,String encoding, int bPP) throws Exception{
		mask = ImageIO.read(picFile);
		mask = toBufferedImage(mask.getScaledInstance(150, 150, 0));
		this.encoding = encoding;
		this.bitsPerPixel = bPP;
		this.bPPmodValue = (int) Math.pow(2, bPP);
		imageWidth = mask.getWidth();
		imageHeight= mask.getHeight();
		fillRest();
	}

	private void colorChange(int value, int mod) throws Exception{

			//System.out.println("Read: " + value);
		int newRed = readPixelRed();
		int newBlue = readPixelBlue();
		int newGreen = readPixelGreen();


		if(encoding.equals("Darken")){

		if(readPixelValue() < value){
				//bounce up
				while((newRed + newBlue + newGreen) != value){
					int random = (int) (Math.random()*3);
					if(random == 0 && newRed < 255)
						newRed++;
					else if (random == 1 && newBlue < 255)
						newBlue++;
					else if (random == 2 && newGreen < 255)
						newGreen++;
				}

		} else {
			//bounce down
			while((newRed + newBlue + newGreen) % mod != value){
				int random = (int) (Math.random()*3);
				if(random == 0 && newRed > 0)
					newRed--;
				else if (random == 1 && newBlue > 0)
					newBlue--;
				else if (random == 2 && newGreen > 0)
					newGreen--;
			}
		}

		}

		if(encoding.equals("Lighten")){
			//bounce down
			while((newRed + newBlue + newGreen) % mod != value){
				//wrap-around incrementer.
				int random = (int) (Math.random()*3);

				if(random == 0)
					newRed++;
				if(random == 1)
					newBlue++;
				if(random == 2)
					newGreen++;

				if(newRed > 255)
					newRed = newRed - mod;

				if(newBlue > 255)
					newBlue = newBlue - mod;

				if(newGreen > 255)
					newGreen = newGreen - mod;

				}

		}



		//	System.out.println( "Mod is now :" + ((newRed + newBlue + newGreen) % mod));
			writeColor(xPtr,yPtr,newRed,newGreen,newBlue);
			incrementPixel();
	}


	public void writeHeader() throws Exception{
		//write BPP
		if(bitsPerPixel == 8){
		colorChange(1,2);colorChange(1,2);colorChange(1,2);
		}
		if(bitsPerPixel == 7){
		colorChange(1,2);colorChange(1,2);colorChange(0,2);
		}
		if(bitsPerPixel == 6){
		colorChange(1,2);colorChange(0,2);colorChange(1,2);
		}
		if(bitsPerPixel == 5){
		colorChange(1,2);colorChange(0,2);colorChange(0,2);
		}
		if(bitsPerPixel == 4){
		colorChange(0,2);colorChange(1,2);colorChange(1,2);
		}
		if(bitsPerPixel == 3){
		colorChange(0,2);colorChange(1,2);colorChange(0,2);
		}
		if(bitsPerPixel == 2){
		colorChange(0,2);colorChange(0,2);colorChange(1,2);
		}
		if(bitsPerPixel == 1){
		colorChange(0,2);colorChange(0,2);colorChange(0,2);
		}

		//bits written. write file size!
		//System.out.print("size?");
		long size = (long) ((8.0 * infoFile.length())/(bitsPerPixel));
		writeInt((int)size,30);

		//WORKS!
		//get file extension?
		String extension = "";

		int i = infoFile.getName().lastIndexOf('.');

		if (i > 0) {
		    extension = infoFile.getName().substring(i+1);
		}

		System.out.println("Extension: " + extension);


		//write chars 0 - 5
		//TXT's only for now!
		for(int j = 0; j != 5 ; j++){
			if(j < extension.length())
				writeInt((int)extension.charAt(j),7);
			else
				writeInt((int)' ',7);
		}



		//remainder flag (zero cause 8 bit!
		xRem= xPtr;
		yRem= yPtr;
		colorChange(0,2);
		//compression flag
		colorChange(compression? 1 : 0 ,2);



	}

	public void incrementPixel() throws Exception{
		xPtr++;

		if(xPtr== imageWidth){
			yPtr++;
			xPtr = 0;
		}

		if(yPtr == imageHeight)
			throw new Exception ("Storage Failed: Info File Too Big.");
	}

public int readPixelValue() throws Exception{
		int rgb = mask.getRGB(xPtr, yPtr);
		int red =   (rgb >> 16) & 0xFF;
		int green = (rgb >>  8) & 0xFF;
		int blue =  (rgb      ) & 0xFF;
		return red+ green + blue;
	}
public int readPixelRed() throws Exception{
	int rgb = mask.getRGB(xPtr, yPtr);
	int red =   (rgb >> 16) & 0xFF;
	return red;
}
public int readPixelGreen() throws Exception{
	int rgb = mask.getRGB(xPtr, yPtr);
	int green = (rgb >>  8) & 0xFF;
	return green;
}
public int readPixelBlue() throws Exception{
	int rgb = mask.getRGB(xPtr, yPtr);
	int blue =  (rgb ) & 0xFF;
	return blue;
}

public void writeColor(int x, int y,int r,int g, int b){
		//System.out.println(r + " " + g+ " " + b+ " ");
			mask.setRGB(xPtr, yPtr, new Color(r,g,b).getRGB());
}


public void writeInt(int i,int count) throws Exception{
	//for each I, get the MSB
	//0 to 23.


	int bytesPos = count;

	while(bytesPos > -1){

		if(i >= Math.pow(2,bytesPos) ){
			i -= Math.pow(2,bytesPos);
		//	System.out.print("1");
			colorChange(1,2);
		} else {
	//		System.out.print("0");
			colorChange(0,2);
		}

		bytesPos--;
	}

	//System.out.println();

}

private void readInfoFile() throws Exception
{
	ArrayList<Integer> buffer;
	int numberOfGroups = 10;//change numberOfGroups to an arbitrary integer indicating
	  						//how many integers are to be pulled out of info file in a batch
	buffer = byteGetter.getGroups(numberOfGroups);
	while (buffer.get(0)!=1)
	{
		for (int i = 1; i < buffer.size(); i++)
		{
			colorChange(buffer.get(i).intValue(),bPPmodValue);
		}
		buffer = byteGetter.getGroups(numberOfGroups);
	}
	//loop ends when buffer.get(0) == 1,
	//when this happens,

	writeRemainder(buffer.get(1));
		for (int i = 2; i != buffer.size(); i++)
		{
			colorChange(buffer.get(i).intValue(),bPPmodValue);
		}

}

private void writeRemainder(int integer) throws Exception {
	int tempx = xPtr;
	int tempy = yPtr;
	xPtr = xRem;
	yPtr = yRem;
	colorChange(integer,bPPmodValue);
	xPtr = tempx;
	yPtr = tempy;
}

public static BufferedImage toBufferedImage(Image img)
{
    if (img instanceof BufferedImage)
    {
        return (BufferedImage) img;
    }

    // Create a buffered image with transparency
    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();

    // Return the buffered image
    return bimage;
}

}

