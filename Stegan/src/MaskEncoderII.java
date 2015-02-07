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
		//Works for evens, no compression
		try{

			System.out.println("red!");
			writeReddenedHeader();
			System.out.println("info!");
			readInfoFile();
			fillRest();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		ImageIO.write(mask, "bmp", maskFile);
		System.out.println("wrote!");
	}
	
	private void fillRest() throws Exception {
		while(xPtr!=imageWidth-1 && yPtr != imageHeight - 1){
			darken((int)(Math.random()*bPPmodValue),bPPmodValue);
		}
	}

	private void darken(int value, int mod) throws Exception{
		
			//System.out.println("Read: " + value);
		int newRed = readPixelRed();
		int newBlue = readPixelBlue();
		int newGreen = readPixelGreen();
			
		if(readPixelValue() < value){
				//bounce up
				while((newRed + newBlue + newGreen) != value){
					
					if(newRed < 255)
						newRed++;
					else if (newBlue < 255)
						newBlue++; 
					else if (newGreen < 255)
						newGreen++; 
				}
				
		} else {
			//bounce down
			while((newRed + newBlue + newGreen) % mod != value){
				if(newRed > 0)
					newRed--;
				else if (newBlue > 0)
					newBlue--; 
				else if (newGreen > 0)
					newGreen--; 			
			}		
		}
		
			
			
		//	System.out.println( "Mod is now :" + ((newRed + newBlue + newGreen) % mod));
			writeColor(xPtr,yPtr,newRed,newGreen,newBlue);
			incrementPixel();
	}
	
	
	public void writeReddenedHeader() throws Exception{
		//write BPP
		if(bitsPerPixel == 8){
		darken(1,2);darken(1,2);darken(1,2);
		}
		if(bitsPerPixel == 7){
		darken(1,2);darken(1,2);darken(0,2);
		}
		if(bitsPerPixel == 6){
		darken(1,2);darken(0,2);darken(1,2);
		}
		if(bitsPerPixel == 5){
		darken(1,2);darken(0,2);darken(0,2);
		}
		if(bitsPerPixel == 4){
		darken(0,2);darken(1,2);darken(1,2);
		}
		if(bitsPerPixel == 3){
		darken(0,2);darken(1,2);darken(0,2);
		}
		if(bitsPerPixel == 2){
		darken(0,2);darken(0,2);darken(1,2);
		}
		if(bitsPerPixel == 1){
		darken(0,2);darken(0,2);darken(0,2);
		}
		
		//bits written. write file size!
		//System.out.print("size?");
		long size = infoFile.length();
		writeInt((int)size,30);
		
		//WORKS!
		//get file extension?
		String extension = "";

		int i = infoFile.getName().lastIndexOf('.');
		
		if (i > 0) {
		    extension = infoFile.getName().substring(i+1);
		}
		
		System.out.print(extension);
		

		//write chars 0 - 5
		//TXT's only for now!
		for(int j = 0; j != 5 ; j++){
			if(j < extension.length())
				writeInt((int)extension.charAt(j),7);
			else
				writeInt((int)' ',7);
		}
		
		
		
		//remainder flag (zero cause 8 bit!
		darken(0,bPPmodValue);
		//compression flag
		darken(0,2);
		
		
 
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
		//	System.out.print("1");
			darken(1,2);
		} else {
	//		System.out.print("0");
			darken(0,2);
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
			darken(buffer.get(i).intValue(),bPPmodValue);
		}
		buffer = byteGetter.getGroups(numberOfGroups);
	}
	/*loop ends when buffer.get(0) == 1,
	when this happens,
	buffer.get(1) is the length of the remainder in the original file,
	buffer.get(2)~buffer.get(buffer.size()-2) are the normal elements to be written,
	and buffer.get(buffer.size()-1) which is the very last integer pulled out, is the remainder*/
	
}


}

