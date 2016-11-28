package fr.upem.decoder;

import java.util.Base64;

public class Decoder {
	
	public static String decode(String string) {
		byte[] byteArray = Base64.getDecoder().decode(string.getBytes());
		return new String(byteArray);
	} 
	
}
