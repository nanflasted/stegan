

import java.io.*;
import java.util.*;

public class InfoProcessor {

	private BufferedInputStream wrapper;
	private FileInputStream stream;
	private int bpp;
	private Queue<Boolean> boolQ = new LinkedList<Boolean>();
	
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
			num+=boolQ.poll()?dig:0;
			dig*=2;
		}
		return new Integer(num); 
	}
	
	public ArrayList<Integer> getGroups(int numberOfGroups) throws IOException
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		int bn = bpp/8+1;
		byte[] buffer = new byte[bn];
		int i = 0;
		int eof = 0;
		res.add(new Integer(0));
		while (((eof=wrapper.read(buffer,0,bn))!=-1)&&(i<numberOfGroups))
		{
			for (int j = 0; j<bn; j++)
			{
				int k = 0;
				while (k<8)
				{
					boolQ.add(new Boolean((buffer[j]&1)==1));
					if (boolQ.size()==bpp) {res.add(calc());}
					buffer[j] >>= buffer[j];
					k++;
				}
			}
		}
		res.set(0, Math.abs(eof));
		return res;
	}
}
