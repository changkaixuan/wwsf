package com.bocsoft.wwsf.webconsole;

/**
 * @author shijie 安全程序 DESede/DES加密 Created on 2005-7-22
 */

/*import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;*/

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
/**
 * 安全程序 DESede/DES测试
 * 
 */
public class DesEncrypt {
	/* 密钥 */
	static byte[] bytKey = { 'a', '6', 'g', '9', '2', '5', 'd', 'f' };

	static String Algorithm = "DES"; //定义 加密算法,可用 DES,DESede,Blowfish

	public static void main(String[] args) {

		/*String rr="";
		System.out.println("请输入加密信息：");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			rr = reader.readLine();
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		String cipheredinfo = encrypt(rr);
		System.out.println("加密信息密文:");
		System.out.println(cipheredinfo);*/
		
		System.out.println(decrypt("18EF52920175902212F7BEEAF6D47190"));
		System.out.println(encrypt("wws!123P"));
		System.out.println(encrypt("db_dcds2"));
		
	}
	/**
	 * 解密字符串
	 * @param cipheredinfo
	 * @return decrypt
	 */
	public static String decrypt(String cipheredinfo) {
		byte[] cipherByte = hex2byte(cipheredinfo);
		String decrypt = null;
		//		添加新安全算法,如果用JCE就要把它添加进去
		//Security.addProvider(new com.sun.crypto.provider.SunJCE());

		/* 密文 */
		//cipherByte
		try {
			DESKeySpec desKS;
			desKS = new DESKeySpec(bytKey);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			SecretKey sk = skf.generateSecret(desKS);

			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.DECRYPT_MODE, sk);

			byte[] clearByte = c1.doFinal(cipherByte);
			//			System.out.println("解密后的信息:" + (new String(clearByte)));
			decrypt = new String(clearByte);

		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (java.lang.Exception e2) {
			e2.printStackTrace();
		}

		return decrypt;
	}
	/**
	 * 加密字符串
	 * @param myinfo
	 * @return byte2hex(cipherByte)
	 */
	public static String encrypt(String myinfo) {

		byte[] cipherByte = null;
		//		添加新安全算法,如果用JCE就要把它添加进去
		//Security.addProvider(new com.sun.crypto.provider.SunJCE());

		try {
			DESKeySpec desKS = new DESKeySpec(bytKey);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			SecretKey sk = skf.generateSecret(desKS);

			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.ENCRYPT_MODE, sk);
			cipherByte = c1.doFinal(myinfo.getBytes());



		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return byte2hex(cipherByte);
	}
	/**
	 * 二进制转字符串
	 * byte转换为int，再将int转换成16进制以String类型显示
	 * @param byte[] b
	 * @return hs.toUpperCase()
	 */
public static String byte2hex(byte[] b) 
	{
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			/* byte转换为int，再将int转换成16进制以String类型显示 */
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
		
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}	/**
		  * 16进制字符串转换为byte[]
		  * 
		  * @param 16进制字符串,2个字符表示1个byte，字符串长度为2的整数倍
		  *            F47BB46273B15EB5F47BB46273B15EB5AD6A88B4FA37833D
		  * @return byt
		  */
	public static byte[] hex2byte(String hex) {
		if (hex.length() % 2 == 1)
			return null;

		byte[] byt = new byte[hex.length() / 2];
		String strbyt = "";
		for (int n = 0; n < hex.length() / 2; n++) {
			/* 16进制以String类型表示byte转换为int，再将int转换成byte */
			strbyt = hex.substring(n * 2, n * 2 + 2);

			byt[n] = Integer.valueOf(strbyt, 16).byteValue();

		}

		return byt;
	}
}
