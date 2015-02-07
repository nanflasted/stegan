

import java.io.*;
import java.util.*;

public class InfoProcessor {

	private BufferedInputStream wrapper;
	private FileInputStream stream;
	private int bpp;
	private Stack<Boolean> boolQ = new Stack<Boolean>();
	private byte[] buffer = new byte[1];
	
	public InfoProcessor(String fileName, int bitsPerPixel) throws IOException
	{
		stream = new FileInputStream(fileName);
		wrapper = new BufferedInputStream(stream);
		bpp = bitsPerPixel;
	}
	
	private Integer calc()
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
	
	private byte reverse(byte input)
	{
		byte res = 0;
		for (int i = 0; i < 8; i++)
		{
			res |= input&1;
			res <<= res;
		}
		res >>= res;
		return res;
	}
	
	public ArrayList<Integer> getGroups(int numberOfGroups) throws IOException
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		int eof = 0;
		while (((eof=wrapper.read(buffer,0,1))!=-1)&&(res.size()<numberOfGroups+1))
		{
			int temp = reverse(buffer[0]);
			int k = 0;
			while (k < 8)
			{
				boolQ.push((temp&1)==1);
				if (boolQ.size()==bpp) res.add(calc());
				k++;
				temp >>= temp;
			}
		}
		if (eof==-1)
		{
			res.set(0,1);
			res.add(1, boolQ.size());
			res.add(calc());
		}
		return res;
	}
	
	public static void main(String args[])
	{
		
	}
}
