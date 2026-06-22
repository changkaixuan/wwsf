package com.bocsoft.wwsf.webconsole;

import com.bocsoft.wwsf.webconsole.cryptor.BadHexStringException;

public class BytesUtil {
	
	public static byte[] intToByte(int flag){
		byte[] bytes = new byte[4];
    	bytes[0] = (byte)(flag & 0xff);
    	bytes[1] = (byte)((flag >> 8) & 0xff);
    	bytes[2] = (byte)((flag >> 16) & 0xff);
    	bytes[3] = (byte)((flag >> 24) & 0xff);
    	return bytes;
	}
	
	public static int byteToInt(byte[] bytes){
		if(bytes.length < 4){
    		throw new IllegalArgumentException("Argument byte array is illegal");
    	}
    	int flag = (bytes[0] & 0xff) |
    			   ((bytes[1]<<8)&0xff00) |
    			   ((bytes[2]<<24)>>>8) |
    			   (bytes[3] <<24);
    	return flag;
	}
	
	public static String byteToHexString(byte[] bytes, int start, int end) {
		if (bytes == null) {
			throw new IllegalArgumentException("bytes == null");
		}
		StringBuilder s = new StringBuilder();
		for (int i = start; i < end; i++) {
			s.append(String.format("%02x", bytes[i]));
		}
		return s.toString();
	}

	public static String byteToHexString(byte bytes[]) {
		return byteToHexString(bytes, 0, bytes.length);
	}


	public static byte[] hexStringToByte(String hex) {
		byte[] bts = new byte[hex.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2),
					16);
		}
		return bts;
	}
	
	public static String byte2hex(byte[] data){
		StringBuilder hexStr = new StringBuilder();
		for(int n = 0; n < data.length; n++) {
			String oneHex = Integer.toHexString(data[n] & 0XFF);
			if (oneHex.length()==1) hexStr.append("0");
			hexStr.append(oneHex);
		}
		return hexStr.toString().toUpperCase();
	}
	
	public static byte[] hex2byte(String hex) throws Exception{
		if (hex.length() % 2 == 1) throw new BadHexStringException("arg's length must be divided with no remainder");
		int byteSize = hex.length()/2;
		byte[] data = new byte[byteSize];
		for (int n = 0; n < byteSize; n++) {
			data[n] = Integer.valueOf(
					hex.substring(n * 2, n * 2 + 2),
					16)
					.byteValue();

		}
		return data;
	}

}
