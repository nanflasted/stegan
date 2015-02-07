import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class Decoder {
	
	BufferedImage img;
	int pxReadX = 0;
	int pxReadY = 0;
	int sizeX = 0;
	int sizeY = 0;
	int bitPtr = 7;
	byte writeByte = 0;
	OutputStream out;
	int bitsPerPx;
	boolean passwordEnabled = false;
	int passCode;
	double percent;
	double totalSize;
	double percentDone;
	String fileExt;
	
	
	
	public Decoder(File f, String outputFileLoc,String password) throws Exception{
		img = ImageIO.read(f);
		sizeX = img.getWidth();
		sizeY = img.getHeight();
			
		//get the first three pixels to get the bits/pixel
		bitsPerPx = 1;
		bitsPerPx += 4*readPixelValue(2);
		bitsPerPx += 2*readPixelValue(2);
		bitsPerPx += readPixelValue(2);
	
		System.out.println("Bits Per Px: " + bitsPerPx);
		
		int bitMod = (int)Math.pow(2,bitsPerPx);
		
		System.out.println("BitMod: " + bitMod);
		
		//get the size, encoded in the next 31 pixels
		int pxCount = 0;
		
		for (int msb = 30; msb != -1; msb--){
			pxCount += Math.pow(2,msb)*readPixelValue(2);
		}
		
		System.out.println("Size: " + pxCount);
		//read five 
		fileExt = "";
		for(int extChars = 0; extChars != 5; extChars++){
			
			int extChar = 0;
			for (int nameByte=7; nameByte != -1; nameByte--){
				extChar += readPixelValue(2)*(int)(Math.pow(2, nameByte));
			}
			System.out.println((char)extChar);
			fileExt += ((char)(extChar));
		}

		System.out.println("Encoding " + fileExt);
		fileExt = fileExt.trim().toLowerCase();
	
		//the next bit is the remainder bit.
		int remainder = readPixelValue(bitMod);
		System.out.println(remainder);
		
		//the next bit is the remainder bit that tells us if we use compression.
		int compression = readPixelValue(2);
		System.out.println(compression);
		
		out = new BufferedOutputStream(new FileOutputStream(outputFileLoc + "output." + fileExt),1024);
		
		////System.out.println("bitsPerPx " + bitsPerPx);
	//	//System.out.println("pxCount " + pxCount);
		//System.out.println("remainder " + remainder);
		

		//enable the password and get to work writing the values.
		if(password != null){
			passCode = password.hashCode();
			passwordEnabled = true;
		} else {
			passwordEnabled = false;
		}
		
		totalSize = pxCount;
		
		while (pxCount != 0){
			int value = readPixelValue(bitMod);
			System.out.println("read: " + value);
			writeInt(value);
			pxCount--;
		//	//System.out.println(pxCount);
		//	percentDone = (totalSize - pxCount) / totalSize;
		//	System.out.println(percentDone);
		}
		
		//kay, now I have the remainder
		if(remainder > 0){
			//The remainder is the number of bits left.
			bitMod = (int) Math.pow(2,remainder);
			bitsPerPx = remainder;
			int value = readPixelValue(bitMod);
		//	//System.out.println("read: " + value);
			writeInt(value);

	}
		out.close();		
			
	}



public void incrementPixel() throws Exception{
	pxReadX++;
	
	if(pxReadX == img.getWidth()){
		pxReadY++;
		pxReadX = 0;
	}
	
	if(pxReadY == img.getHeight())
		throw new Exception ("Reached End of file before finished!");	
}


public int readPixelValue(int mod) throws Exception{
	int rgb = img.getRGB(pxReadX, pxReadY);
	int red =   (rgb >> 16) & 0xFF;
	int green = (rgb >>  8) & 0xFF;
	int blue =  (rgb      ) & 0xFF;
	
	int total = red+ green + blue;
	
	incrementPixel();
	
	return (total % mod);
}

public void writeInt(int i) throws Exception{
	//for each I, get the MSB
	//0 to 23.
	
	int bytesPos = bitsPerPx - 1;
	//if this is 1? 
	////System.out.println(i + " is:");

	
	while(bytesPos > -1){
		
		if(i >= Math.pow(2,bytesPos) ){
			i -= Math.pow(2,bytesPos);
		//	//System.out.print("1");
			writeBit(true);
		} else {
		//	//System.out.print("0");
			writeBit(false);
		}
		
		bytesPos--;
	}
	
	//System.out.println();
	
}

public void writeBit(boolean one) throws IOException{
	if(one)
		writeByte += Math.pow(2,bitPtr);
	
	bitPtr--;
	
	if(bitPtr == -1){
		//byte's done bitches!
		//System.out.println("\nwrote byte!");
		if(passwordEnabled)
			writeByte = encrypt(writeByte);
		out.write(writeByte);
		bitPtr = 7;
		writeByte = 0;
	}
	
}

private byte encrypt(byte plaintext) {
	int temp = plaintext;
	int hashpart = passCode & 0xFF;
	temp = temp ^ hashpart;
	passCode = passCode >> 8;
	passCode = passCode | (hashpart << 24);
	return (byte) temp;
}

}