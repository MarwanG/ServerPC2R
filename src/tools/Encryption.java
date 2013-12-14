package tools;

import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * Class used mainly to encode and decode data that will be saved.
 * @author marwanghanem
 * 
 */
public class Encryption {

	
	/**
	 * Takes the string passed as parameter and returns the result of encoding.
	 * @param str
	 * @return string that is encoded
	 */
	public static String encode(String str) {
	    BASE64Encoder encoder = new BASE64Encoder();
	    str = new String(encoder.encodeBuffer(str.getBytes()));
	    return str;
	}

	/**
	 * Takes a encoded string passed as a parameter and returns the result of decoding.
	 * @param str
	 * @return string after decoding
	 */
	public static String decode(String str) {
	    BASE64Decoder decoder = new BASE64Decoder();
	    try {
	        str = new String(decoder.decodeBuffer(str));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }       
	    return str;
	}

}
