import java.util.zip.*;
import java.io.*;
public class CompressionTool {

	private static FileInputStream fis;
	private static BufferedInputStream inStream;
	private static FileOutputStream fos;
	private static DeflaterOutputStream outStream;
	private static BufferedInputStream dinStream;
	private static InflaterOutputStream doutStream;
	public static void compress(File file) throws IOException
	{
		fis = new FileInputStream(file);
		inStream = new BufferedInputStream(fis);
		fos = new FileOutputStream("temp");
		outStream = new DeflaterOutputStream(fos);
		int buffer;
		while ((buffer = inStream.read())!=-1)
		{
			outStream.write(buffer);
		}
		inStream.close();
		outStream.close();
	}

	public static void decompress(String fileName) throws IOException
	{
		fis = new FileInputStream("temp");
		dinStream = new BufferedInputStream(fis);
		fos = new FileOutputStream(fileName);
		doutStream = new InflaterOutputStream(fos);
		int buffer;
		while ((buffer = dinStream.read())!=-1)
		{
			doutStream.write(buffer);
		}
		dinStream.close();
		doutStream.close();
	}
	/*public static void main(String[] args) throws IOException
	{
		CompressionTool.compress("compressionTest.in");
		CompressionTool.decompress("compressionTest.in");
	}*/

}
