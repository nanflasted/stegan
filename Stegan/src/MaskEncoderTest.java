import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;


public class MaskEncoderTest {

	@Test
	public void test() {
		try {
			MaskEncoder e = new MaskEncoder("data.txt", "img.bmp", "", "darken", 4, "bmp", false, false, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//headerBits = new ArrayList<Integer>();
		
	}

}
