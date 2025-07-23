package com.bocsoft.wwsf.webconsole.cryptor;


public interface EncryptService {
	
public static String DATA_KEY_HANDER = "boc.dataKey.ciphertext";
	
	/**
	 * <p>
	 * 一段密钥加密,使用LMK加密最终秘钥
	 * </P>
	 * @param key 密钥明文
	 * @return string hex返回数据密钥密文
	 */
	public String encryptKey(String key) throws Exception;

	/**
	 * 二段密钥加密,使用LMK加密最终秘钥
	 * @param keya 密钥成分a的明文
	 * @param keyb 密钥成分b的明文
	 * @return string hex返回最终密钥密文
	 */
	public String encryptSectionKey(String keya,String keyb) throws Exception;
	
	/**
	 * 输入密钥密文，获取密钥明文
	 * @return
	 * @throws Exception
	 */
	public String getPlainKey(String key) throws Exception;
	
	public String encryptInfo(String cotent) throws Exception;
	
	public String decryptInfo(String cotent) throws Exception;

}
