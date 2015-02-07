

import java.io.*;
import java.util.*;

public class InfoProcessor {

	private BufferedInputStream wrapper;
	private FileInputStream stream;
	private int bpp;
	private Stack<Boolean> boolQ = new Stack<Boolean>();
	private byte[] buffer = new byte[1];
	private Queue<Integer> resQ = new LinkedList<Integer>();
	
	
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
	
	private int reverse(byte input)
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
	
	private void makeResQ() throws IOException
	{
		int eof = 0;
		while (((eof=wrapper.read(buffer,0,1))!=-1))
		{
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
			
		}
	}
	
	public ArrayList<Integer> getGroups(int numberOfGroups) throws IOException
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
		InfoProcessor test = new InfoProcessor("src/InfoProcessorTestingFile.in",5);
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
