import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

//Electric Boogaloo




import javax.imageio.ImageIO;


public class MaskEncoderII {

	public File infoFile;
	public File maskFile;
	String setting;
	int bitsPerPixel;
	boolean compression;
	boolean encryption;
	String password;
	BufferedImage mask;
	int imageWidth;
	int imageHeight;
	InfoProcessor byteGetter;
	int arraySize = 64;
	int xPtr;
	int yPtr;
	int bPPmodValue;
	
	public MaskEncoderII(File infoFile, File maskFile, String setting, int bPP, boolean compression, boolean encryption, String password) throws Exception {
		this.infoFile = infoFile;
		this.maskFile = maskFile;
		mask = ImageIO.read(maskFile);
		this.setting = setting;
		this.bitsPerPixel = bPP;
		this.bPPmodValue = (int) Math.pow(2, bPP);
		this.compression = compression;
		this.encryption = encryption;
		this.password = password;
		imageWidth = mask.getWidth();
		imageHeight= mask.getHeight();
		byteGetter = new InfoProcessor(infoFile, bitsPerPixel, encryption, password);
		writeReddenedHeader();
		ImageIO.write(mask, "bmp", maskFile);
		System.out.println("wrote!");
	}
	
	private void writeRedValue(int value, int mod) throws Exception{
		
			//System.out.println("Read: " + value);
			
			int newRed = readPixelRed();
			int newBlue = readPixelBlue();
			int newGreen = readPixelGreen();
			
			int thisMod = (newRed + newBlue + newGreen) % mod;
			
		//	System.out.println("Modding by " + mod);
			
		//	System.out.println("Current Mod: " + thisMod);
			
			int delta = (value - thisMod);
			//if we need to go up
			if(newRed + delta <= 255)
				newRed += delta;
			else
				newRed -= (delta - mod);
			
		//	System.out.println( "Mod is now :" + ((newRed + newBlue + newGreen) % mod));
			writeColor(xPtr,yPtr,newRed,newGreen,newBlue);
			incrementPixel();
	}
	
	
	public void writeReddenedHeader() throws Exception{
		//write BPP
		if(bitsPerPixel == 8){
		writeRedValue(1,2);writeRedValue(1,2);writeRedValue(1,2);
		}
		if(bitsPerPixel == 7){
		writeRedValue(1,2);writeRedValue(1,2);writeRedValue(0,2);
		}
		if(bitsPerPixel == 6){
		writeRedValue(1,2);writeRedValue(0,2);writeRedValue(1,2);
		}
		if(bitsPerPixel == 5){
		writeRedValue(1,2);writeRedValue(0,2);writeRedValue(0,2);
		}
		if(bitsPerPixel == 4){
		writeRedValue(0,2);writeRedValue(1,2);writeRedValue(1,2);
		}
		if(bitsPerPixel == 3){
		writeRedValue(0,2);writeRedValue(1,2);writeRedValue(0,2);
		}
		if(bitsPerPixel == 2){
		writeRedValue(0,2);writeRedValue(0,2);writeRedValue(1,2);
		}
		if(bitsPerPixel == 1){
		writeRedValue(0,2);writeRedValue(0,2);writeRedValue(0,2);
		}
		
		//bits written. write file size!
		long size = infoFile.length();
		System.out.println("size");
		writeInt((int)size,30);
		
		System.out.println("txt");
		//write chars 0 - 5
		writeInt((int)'t',7);
		writeInt((int)'x',7);
		writeInt((int)'t',7);
		writeInt((int)' ',7);
		writeInt((int)' ',7);
		
		//remainder flag
		writeRedValue(0,bPPmodValue);
		//compression flag
		writeRedValue(0,2);
		
		//just write F U C K
		
		writeRedValue((int)'F',256);
		

	}
	
	public void incrementPixel() throws Exception{
		xPtr++;
		
		if(xPtr== imageWidth){
			yPtr++;
			xPtr = 0;
		}
		
		if(yPtr == imageHeight)
			throw new Exception ("Reached End of file before finished!");	
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
	
			mask.setRGB(xPtr, yPtr, new Color(r,g,b).getRGB());
}


public void writeInt(int i,int count) throws Exception{
	//for each I, get the MSB
	//0 to 23.
	
	
	int bytesPos = count;
	
	while(bytesPos > -1){
		
		if(i >= Math.pow(2,bytesPos) ){
			i -= Math.pow(2,bytesPos);
			System.out.print("1");
			writeRedValue(0,2);
		} else {
			System.out.print("0");
			writeRedValue(0,1);
		}
		
		bytesPos--;
	}
	
	//System.out.println();
	
}

}

