
import java.io.*;
import java.util.ArrayList;

import javax.sound.sampled.*;


public class MaskEncoderWav {
	
	
	private File infoFile;
	private File maskFile;	
	private WavFile inWavFile;
	private WavFile outWavFile;
	//WAV Formatting
	private int numChannels;
	private long numFrames;
	private int bitDepth;
	private long sampleRate;
	
	String encoding;
	int bitsPerSample;
	boolean compression;
	boolean encryption;
	String password;
	InfoProcessor byteGetter;

	

	
	public MaskEncoderWav(File infoFile, File maskFile, String encoding, int bPP, boolean compression, boolean encryption, String password) throws Exception {
		this.infoFile = infoFile;
		inWavFile = WavFile.openWavFile(maskFile);
		this.bitDepth = inWavFile.getValidBits();
		this.numChannels = inWavFile.getNumChannels();
		this.numFrames = inWavFile.getNumFrames();
		this.sampleRate = inWavFile.getSampleRate();
		outWavFile = WavFile.newWavFile(new File("Enc"+maskFile.getName()), numChannels, numFrames, bitDepth, sampleRate);
		this.encoding = encoding;
		this.bitsPerSample = bPP;
		this.compression = compression;
		this.encryption = encryption;
		this.password = password;
		byteGetter = new InfoProcessor(infoFile, bitsPerSample, encryption, password);
		//Works for evens, no compression
		try{
			//System.out.println("red!");
			writeHeader();
			//System.out.println("info!");
			readInfoFile();
			//fillRest();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		System.out.println("wrote!");
	}
	
	private void writeHeader() throws Exception
	{
		outWavFile.writeFrames(new long[]{(compression)?1:0,0}, 1);
		outWavFile.writeFrames(new long[]{infoFile.length()}, 4);
		
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
}
