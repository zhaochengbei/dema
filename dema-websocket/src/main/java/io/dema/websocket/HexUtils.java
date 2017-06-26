package io.dema.websocket;
/**
 * author：zhaochengbei
 * date：2017/6/22
*/
public class HexUtils {
	 /**
	 * Convert byte[] to hex string.   
	 * @param src byte[] data   
	 * @return hex string   
	 */      
	public static String bytesToHexString(byte[] src){   
	    StringBuilder stringBuilder = new StringBuilder("");   
	    if (src == null || src.length <= 0) {   
	        return null;   
	    }   
	    for (int i = 0; i < src.length; i++) {   
	        int v = src[i] & 0xFF;   
	        String hv = Integer.toHexString(v);   
	        if (hv.length() < 2) {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append(hv);   
	    }   
	    return stringBuilder.toString();   
	}   
	/**  
	 * Convert hex string to byte[]  
	 * @param hexString the hex string  
	 * @return byte[]  
	 */  
	public static byte[] hexStringToBytes(String hexString) {   
	    if (hexString == null || hexString.equals("")) {   
	        return null;   
	    }   
	    hexString = hexString.toUpperCase();   
	    int length = hexString.length() / 2;   
	    char[] hexChars = hexString.toCharArray();   
	    byte[] d = new byte[length];   
	    for (int i = 0; i < length; i++) {   
	        int pos = i * 2;   
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
	    }   
	    return d;   
	}   
	/**  
	 * Convert char to byte  
	 * @param c char  
	 * @return byte  
	 */  
	static private byte charToByte(char c) {   
	    return (byte) "0123456789ABCDEF".indexOf(c);   
	}  

	public static void printHexString( byte[] b) {     
	   for (int i = 0; i < b.length; i++) {    
	     String hex = Integer.toHexString(b[i] & 0xFF);    
	     if (hex.length() == 1) {    
	       hex = '0' + hex;    
	     }    
	     System.out.print(hex.toUpperCase() );    
	   }    
	  
	}  
}
