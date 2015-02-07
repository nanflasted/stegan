import java.util.*;
import java.io.*;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class MaskEncoder {
	private ArrayList<Integer> byteList;
	private int bitsPerPixel, imageWidth, imageHeight, activeRemainder;
	private long header; //5 bpp, 31 filesize, 1 remainder
	private String mySettings;  //darken, lighten, red, blue, green, auto, (blur?)
	private InfoProcessor myProc;
	private String fileName;
	private int arraySize = 10000; 
	private BufferedImage image;
	private int totalPixels, fileSize;
	private int headerSize = 76;
	private ArrayList<Integer> headerBits; 
	private String fileExtension;
	private boolean compressionFlag;
	private int bitsToRepBPPInHeader = 3, bitsToRepFSizeInHeader = 31, bitsToRepFExt = 40, fExtLength = 5;
	private int placeCounter = 0;
	private boolean encrypting;
	
	public MaskEncoder(String dFile, String file, String filePath, String settings, int bPP, String fileExt, boolean compBool, boolean enc) throws IOException {
		headerBits = new ArrayList<Integer>();
		encrypting = enc;
		fileExtension = fileExt;
		byteList = new ArrayList<Integer>();
		bitsPerPixel = bPP;
		compressionFlag = compBool;
		mySettings = settings;
		fileName = file;
		myProc = new InfoProcessor(fileName, bitsPerPixel, encrypting);
		image = ImageIO.read(new File(fileName));
		imageWidth = image.getWidth();
		imageHeight= image.getHeight();
		this.applyInfoToMask();
		ImageIO.write(image, "bmp", new File(fileName));
	}
	
	private static void applyInfoToMask() throws IOException{
		byteList = myProc.getGroups(arraySize);
		int widthCounter = headerSize;
		int heightCounter = 0;
		int tempRGB;
		Color tempColor;
		int[] rgbRA = new int[3];
		while(widthCounter >= imageWidth){
			widthCounter -= imageWidth;
			heightCounter ++;
		}
		while(byteList.get(0) != 1){
			for(int i = 1; i < byteList.size(); i++){
				tempRGB = image.getRGB(widthCounter, heightCounter);
				tempColor = new Color(tempRGB);
				rgbRA = changeColor(bitsPerPixel, tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), byteList.get(i), false);
				tempColor = new Color(rgbRA[0], rgbRA[1], rgbRA[2]);
				image.setRGB(widthCounter, heightCounter, tempColor.getRGB());
				
				widthCounter = (widthCounter+1)%imageWidth;
				if(widthCounter == 0)
					heightCounter++;
				if(heightCounter >= imageHeight){
					System.out.println("Image Too Small");
					System.exit(0);
				}
				
				byteList = myProc.getGroups(arraySize);
			}
		}
		activeRemainder = byteList.get(1);
		for(int i = 2; i < byteList.size(); i++){
			tempRGB = image.getRGB(widthCounter, heightCounter);
			tempColor = new Color(tempRGB);
			rgbRA = changeColor(bitsPerPixel, tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), byteList.get(i), false);
			tempColor = new Color(rgbRA[0], rgbRA[1], rgbRA[2]);
			image.setRGB(widthCounter, heightCounter, tempColor.getRGB());
			
			widthCounter = (widthCounter+1)%imageWidth;
			if(widthCounter == 0)
				heightCounter++;
			if(heightCounter >= imageHeight){
				System.out.println("Image Too Small");
				System.exit(0);
			}
			
			byteList = myProc.getGroups(arraySize);
		}
		totalPixels = heightCounter * imageWidth + widthCounter + 1; 
		fileSize = totalPixels - headerSize;
		composeHeader(bitsPerPixel, fileSize, fileExtension, activeRemainder, compressionFlag);
		for(int i = 0; i < headerSize; i++){
			tempRGB = image.getRGB(widthCounter, heightCounter);
			tempColor = new Color(tempRGB);
			if(i >= placeCounter)
				rgbRA = changeColor(bitsPerPixel, tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), headerBits.get(i), false);
			else
				rgbRA = changeColor(2, tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), headerBits.get(i), false);
			tempColor = new Color(rgbRA[0], rgbRA[1], rgbRA[2]);
			image.setRGB(widthCounter, heightCounter, tempColor.getRGB());
			
			widthCounter = (widthCounter+1)%imageWidth;
			if(widthCounter == 0)
				heightCounter++;
			if(heightCounter >= imageHeight){
				System.out.println("Image Too Small");
				System.exit(0);
			}
		}
	}
	
	private static void composeHeader(int bPP, int fSize, String fileExt, int rem, boolean comp){ //headerBits is header
		Stack<Integer> int2Binary = new Stack<Integer>();
		placeCounter = 0;
		int tempInt = bPP;
		int tempCounter = bitsToRepBPPInHeader - 1;
		while(tempInt > 1){
			if(tempInt > Math.pow(2, tempCounter)){
				headerBits.add(1);
				placeCounter++;
				tempInt -= Math.pow(2, tempCounter);
			}
			else{
				headerBits.add(0);
				placeCounter++;
			}
			tempCounter--;	
		}
		tempInt = fSize;
		tempCounter = bitsToRepFSizeInHeader - 1;
		while(tempInt > 0){
			if(tempInt > Math.pow(2, tempCounter)){
				headerBits.add(1);
				placeCounter++;
				tempInt -= Math.pow(2, tempCounter);
			}
			else{
				headerBits.add(0);
				placeCounter++;
			}
			tempCounter--;	
		}
		
		byte[] fileExtBytes = fileExt.getBytes();
		for(int i = 0; i < fileExtBytes.length; i++){
			int tempByteInt = Byte.valueOf(fileExtBytes[i]).intValue();
			tempInt = tempByteInt;
			tempCounter = 8 - 1;
			while(tempInt > 0){
				if(tempInt > Math.pow(2, tempCounter)){
					headerBits.add(1);
					placeCounter++;
					tempInt -= Math.pow(2, tempCounter);
				}
				else{
					headerBits.add(0);
					placeCounter++;
				}
				tempCounter--;	
			}
		}
		
		/*for(int i = 0; i < fExtLength; i++){
			char tempChar = fileExt.charAt(i);
		}*/
		headerBits.add(rem);
		if(comp)
			headerBits.add(1); 
		else
			headerBits.add(0);
	}
	
	private static int[] changeColor(int chosenBPP, int r, int g, int b, int valToHide, boolean retry){ //current plan is to distribute changes equally, based on which operation
		int[] rgbRA = new int[3]; //When Alpha, needs to be updated //0 = r; 1 = g; 2 = b;
		int summed = r + g + b;
		int posNecessary = (((summed % chosenBPP) - valToHide)+chosenBPP)%chosenBPP;
	//	int alreadyCompensated = 0;
		//int[] tempRA = new int[2];
		String chosenSetting = mySettings;
		
		if(retry){
			/*if(summed > 400){ //the operation failed because too bright
				chosenSetting = "darken";
			} else if(summed < 100){
				chosenSetting = "lighten";
			}*/
			chosenSetting = "auto";
		}
		
		switch(chosenSetting){
			case "darken":
				if(r > (posNecessary/3) + 1 & g > (posNecessary/3) + 1 & b > (posNecessary/3) + 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 1, 1, 1, 1, posNecessary);
				else if (r > (4*posNecessary/9) + 1 & g > (posNecessary/9) + 1 & b > (4*posNecessary/9) + 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 4, 1, 4, 1, posNecessary);
				else if (r > (posNecessary/9) + 1 & g > (4*posNecessary/9) + 1 & b > (4*posNecessary/9) + 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 1, 4, 4, 2, posNecessary);
				else if (r > (4*posNecessary/9) + 1 & g > (4*posNecessary/9) + 1 & b > (posNecessary/9) + 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 4, 4, 1, 1, posNecessary);
				else
					rgbRA = changeColor(chosenBPP, r, g, b, valToHide, true);
				break;
			case "lighten":
				if(r < 255 - (posNecessary/3) - 1 & g < 255 - (posNecessary/3) - 1 & b < 255 - (posNecessary/3) - 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 1, 1, 1, 1, posNecessary);
				else if (r > (4*posNecessary/9) + 1 & g > (posNecessary/9) + 1 & b > (4*posNecessary/9) + 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 4, 1, 4, 1, posNecessary);
				else if (r > (posNecessary/9) + 1 & g > (4*posNecessary/9) + 1 & b > (4*posNecessary/9) + 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 1, 4, 4, 2, posNecessary);
				else if (r > (4*posNecessary/9) + 1 & g > (4*posNecessary/9) + 1 & b > (posNecessary/9) + 1)
					rgbRA = defaultDarken(chosenBPP, r, b, g, 4, 4, 1, 1, posNecessary);
				else
					rgbRA = changeColor(chosenBPP, r, g, b, valToHide, true);
				break;
			case "red":
				break;
			case "green":
				break;
			case "blue":
				break;
			case "auto":
				break;
			default:
				break;
		}
		
		
		return rgbRA;
	}
	
	private static int[] defaultDarken(int chosenBPP, int r, int b, int g, int rMod, int gMod, int bMod, int whichRemainder, int posNecessary){
		int[] rgbRA = new int[3]; 
		int[] tempRA = new int[2];
		int[] modRA = new int[3];
		modRA[0] = rMod;
		modRA[1] = gMod;
		modRA[2] = bMod;
		int alreadyCompensated = 0, toBeComped = rMod + bMod + gMod;
		tempRA = singleInstance(r, (-rMod*(chosenBPP-posNecessary)/toBeComped));
		rgbRA[0] = tempRA[0];
		alreadyCompensated += tempRA[1];
		tempRA = singleInstance(g, (-gMod*(chosenBPP-posNecessary)/toBeComped));
		rgbRA[1] = tempRA[0];
		alreadyCompensated += tempRA[1];
		tempRA = singleInstance(b, (-bMod*(chosenBPP-posNecessary)/toBeComped));
		rgbRA[2] = tempRA[0];
		alreadyCompensated += tempRA[1];
		if(alreadyCompensated != (-(chosenBPP-posNecessary))){
			tempRA = singleInstance(rgbRA[whichRemainder - 1], -(chosenBPP-posNecessary) - alreadyCompensated);
			rgbRA[whichRemainder - 1] = tempRA[0];
		}
		return rgbRA;
	}
	
	private static int[] defaultLighten(int r, int b, int g, int rMod, int gMod, int bMod, int whichRemainder, int posNecessary){
		int[] rgbRA = new int[3]; 
		int[] tempRA = new int[2];
		int[] modRA = new int[3];
		modRA[0] = rMod;
		modRA[1] = gMod;
		modRA[2] = bMod;
		int alreadyCompensated = 0, toBeComped = rMod + bMod + gMod;
		tempRA = singleInstance(r, (rMod*posNecessary/toBeComped));
		rgbRA[0] = tempRA[0];
		alreadyCompensated += tempRA[1];
		tempRA = singleInstance(g, (gMod*posNecessary/toBeComped));
		rgbRA[1] = tempRA[0];
		alreadyCompensated += tempRA[1];
		tempRA = singleInstance(b, (bMod*posNecessary/toBeComped));
		rgbRA[2] = tempRA[0];
		alreadyCompensated += tempRA[1];
		if(alreadyCompensated != posNecessary){
			tempRA = singleInstance(rgbRA[whichRemainder - 1], posNecessary - alreadyCompensated);
			rgbRA[whichRemainder - 1] = tempRA[0];
		}
		return rgbRA;
	}
	
	private static int[] singleInstance(int colorVal, int change){
		int[] returnRA = new int[2];//0=val, 1=change
		int newVal = colorVal + change;
		if(newVal < 0){
			newVal = 0;
		} else if(newVal >255){
			newVal = 255;
		} 
		returnRA[0] = newVal;
		returnRA[1] = newVal - colorVal;
		return returnRA;
	}
	
}
