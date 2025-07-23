package com.bocsoft.wwsf.webconsole.cryptor;

import java.util.HashMap;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.BytesUtil;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.kjhxtc.crypto.api.Symmetric.SymEncryptOperator;

public class EncryptContants {
	
	final static Map<String,Integer> algorithms = new HashMap<String,Integer>();
	
	static{
		algorithms.put("AES_CBC", SymEncryptOperator.ID_AES_CBC);
		algorithms.put("AES_ECB", SymEncryptOperator.ID_AES_ECB);
		algorithms.put("DES_CBC", SymEncryptOperator.ID_DES_CBC);
		algorithms.put("DES_ECB", SymEncryptOperator.ID_DES_ECB);
		algorithms.put("SM4_CBC", SymEncryptOperator.ID_SM4_CBC);
		algorithms.put("SM4_ECB", SymEncryptOperator.ID_SM4_ECB);
		algorithms.put("3DES_CBC", SymEncryptOperator.ID_3DES_CBC);
		algorithms.put("3DES_ECB", SymEncryptOperator.ID_3DES_ECB);
	}
	
	final static Map<String,Integer> paddings = new HashMap<String,Integer>();
	
	static{
		paddings.put("PAD_NOPAD", SymEncryptOperator.PAD_NOPAD);
		paddings.put("PAD_PKCS5", SymEncryptOperator.PAD_PKCS5);
		paddings.put("PAD_PKCS7", SymEncryptOperator.PAD_PKCS7);
		paddings.put("PAD_ZEROS", SymEncryptOperator.PAD_ZEROS);
		paddings.put("PAD_ANSIX923", SymEncryptOperator.PAD_ANSIX923);
		paddings.put("PAD_ISO10126", SymEncryptOperator.PAD_ISO10126);
		
	}
	
	public static int getEncryptKeyAlgorithm(String key_algorithm){
		if(algorithms.containsKey(key_algorithm)){
			return algorithms.get(key_algorithm);
		}else{
			throw new RuntimeException("key.encrypt.algorithm is invaild " + key_algorithm);
		}
	}
	
	public static int getEncryptKeyPadding(String key_padding){
		if(paddings.containsKey(key_padding)){
			return paddings.get(key_padding);
		}else{
			throw new RuntimeException("key.encrypt.padding is invaild " + key_padding);
		}
	}
	
	public static byte[] getEncryptKeyIV(String key_iv){
		if(StringUtil.hasText(key_iv)) {
			try {
				return BytesUtil.hex2byte(key_iv);
			} catch (Exception e) {
				throw new RuntimeException("key.encrypt.iv is invaild, must be a hexadecimal string " + key_iv);
			}
		}
		return null;
	}
	
	public static int getEncryptTextAlgorithm(String text_algorithm){
		if(algorithms.containsKey(text_algorithm)){
			return algorithms.get(text_algorithm);
		}else{
			throw new RuntimeException("text.encrypt.algorithm is invaild " + text_algorithm);
		}
	}
	
	public static int getEncryptTextPadding(String text_padding){
		if(paddings.containsKey(text_padding)){
			return paddings.get(text_padding);
		}else{
			throw new RuntimeException("text.encrypt.padding is invaild " + text_padding);
		}
	}
	
	public static byte[] getEncryptTextIV(String text_iv){
		if(StringUtil.hasText(text_iv)) {
			try {
				return BytesUtil.hex2byte(text_iv);
			} catch (Exception e) {
				throw new RuntimeException("text.encrypt.iv is invaild, must be a hexadecimal string " + text_iv);
			}
		}
		return null;
	}

}
