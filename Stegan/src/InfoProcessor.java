

import java.io.*;
import java.util.*;

public class InfoProcessor {

	private BufferedInputStream wrapper;						//the actual input stream
	private FileInputStream stream;								//the input stream to be wrapped
	private int bpp;											//bits per pixel
	private Stack<Boolean> boolQ = new Stack<Boolean>();		//a queue of booleans denoting a group of 'bpp' bits 
	private byte[] buffer = new byte[1];						//buffer for reading
	private Queue<Integer> resQ = new LinkedList<Integer>();	//a queue for integers to be sent to Matt
	private boolean encrypting;
	private int hashCode;
	
	public InfoProcessor(String fileName, int bitsPerPixel, boolean enc, String password) throws IOException //constructor: takes in filename to be read, and bits number request
	{
		stream = new FileInputStream(fileName);
		wrapper = new BufferedInputStream(stream);
		bpp = bitsPerPixel;
		encrypting = enc;
		hashCode = password.hashCode();
	}
	
	private Integer calc() //converts current group of bits into integer
	{
		int num = 0;
		int dig = 1;
		while (boolQ.size()>0)
		{
			num+=boolQ.pop()?dig:0;
			dig*=2;
		}
		return new Integer(num); 
	}
	
	private int reverse(byte input) //reverse the byte read by bufferinputstream so that it works.
	{
		int in = input;
		int res = 0;
		for (int i = 0; i < 8; i++)
		{
			res |= in&1;
			in = (in >> 1);
			res = (res << 1);
		}
		res = (res >> 1);
		return res;
	}
	
	private byte encrypt(byte plaintext)
	{
		int temp = plaintext;
		int hashpart = hashCode & 0xFF;
		temp = temp ^ hashpart;
		hashCode = hashCode >> 8;
		hashCode = hashCode | (temp << 24);
		return (byte) temp;
	}
	
	private void makeResQ() throws IOException //make the next integer
	{
		int eof = 0;
		while (((eof=wrapper.read(buffer,0,1))!=-1))
		{
			buffer[0] = encrypting? encrypt(buffer[0]): buffer[0];
			int temp = reverse(buffer[0]);
			int k = 0;
			while (k < 8)
			{
				boolQ.push((temp&1)==1);
				if (boolQ.size()==bpp) resQ.add(calc());
				k++;
				temp = temp >> 1;
			}
		}
		if (eof==-1)
		{
			resQ.add(-1);
			resQ.add(boolQ.size());
			resQ.add(calc());
			wrapper.close();
		}
	}
	
	public ArrayList<Integer> getGroups(int numberOfGroups) throws IOException //returns the numberOfGroups integers, each denoting bpp bits in the original file.
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		res.add(0);
		while (res.size()<numberOfGroups+1)
		{
			if (resQ.isEmpty()) makeResQ();
			int temp = resQ.poll();
			if (temp != -1)
			{
				res.add(temp);
			}
			else
			{
				res.set(0, 1);
				res.add(1,resQ.poll());
				res.add(resQ.poll());
				break;
			}
		}
		return res;
	}
	
	/*public static void main(String args[]) throws IOException
	{
		InfoProcessor test = new InfoProcessor("src/InfoProcessorTestingFile.in",5,true,"swag");
		System.out.println("swag".hashCode());
		System.out.println((byte)'c');
		System.out.println(test.encrypt((byte)'c'));
		System.out.println((byte)3543529);
		for (Integer i : test.getGroups(2))
		{
			System.out.println(i.intValue());
		}
		for (Integer i : test.getGroups(2))
		{
			System.out.println(i.intValue());
		}
		for (Integer i : test.getGroups(2))
		{
			System.out.println(i.intValue());
		}
	}*/
}
