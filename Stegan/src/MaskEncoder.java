import java.util.*;


public class MaskEncoder {
	private ArrayList byteList;
	private int bitsPerPixel, remainingDigits;
	private long header;
	private String mySettings;
	
	
	public MaskEncoder(String settings, int bPP) {
		byteList = new ArrayList();
		bitsPerPixel = bPP;
		mySettings = settings;
		
	}
	
	private void applyInfoToMask(){
		byteList = InfoProcessor.getGroups();
		
	}
}
