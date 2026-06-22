package com.bocsoft.wwsf.webconsole.cryptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.BytesUtil;
import com.kjhxtc.crypto.api.Symmetric.SymDecryptOperator;
import com.kjhxtc.crypto.api.Symmetric.SymEncryptOperator;

@Service
public class EncryptServiceHxtcImpl implements EncryptService{
	
	@Value("${key.encrypt.algorithm:SM4_ECB}")
	private String encryptKeyAlgorithm;
	@Value("${key.encrypt.padding:PAD_NOPAD}")
	private String encryptKeyPadding;
	@Value("${key.encrypt.iv:}")
	private String encryptKeyIV;
	@Value("${text.encrypt.algorithm:SM4_ECB}")
	private String encryptTextAlgorithm;
	@Value("${text.encrypt.padding:PAD_PKCS5}")
	private String encryptTextPadding;
	@Value("${text.encrypt.iv:}")
	private String encryptTextIV;

	/**
	 * 公共组件LMK保护密码密钥和数据库密码等敏感数据暂时使用SM4算法
	 * 这是中心密码算法使用上的要求，密钥长度统一为128bit，加密模式统一为ECB模式
	 */
	public String encryptKey(String key) throws Exception {
		
		SymEncryptOperator symEncrypt = new SymEncryptOperator(
				EncryptContants.getEncryptKeyAlgorithm(encryptKeyAlgorithm),
				EncryptContants.getEncryptKeyPadding(encryptKeyPadding),
				Lmk.LMK_CODE,
				EncryptContants.getEncryptKeyIV(encryptKeyIV));
		byte[] encryptKey = symEncrypt.Boc_Encrypt(BytesUtil.hex2byte(key));
		return BytesUtil.byte2hex(encryptKey);
	}

	public String encryptSectionKey(String keya, String keyb) throws Exception {
		byte[] byteKeya = BytesUtil.hex2byte(keya);
		byte[] byteKeyb = BytesUtil.hex2byte(keyb);
		if (byteKeya.length != byteKeyb.length)
			throw new BadKeySectionException("section keya length not eq section keyb");
		byte[] byteKey = new byte[byteKeya.length];
		for (int k = 0; k < byteKeyb.length; k++) {
			byteKey[k] = (byte) (byteKeya[k] ^ byteKeyb[k]);
		}
		SymEncryptOperator symEncrypt = new SymEncryptOperator(
				EncryptContants.getEncryptKeyAlgorithm(encryptKeyAlgorithm),
				EncryptContants.getEncryptKeyPadding(encryptKeyPadding),
				Lmk.LMK_CODE,
				EncryptContants.getEncryptKeyIV(encryptKeyIV));
		byte[] encryptKey = symEncrypt.Boc_Encrypt(byteKey);
		return BytesUtil.byte2hex(encryptKey);
	}
	
	public String getPlainKey(String key) throws Exception {
		return BytesUtil.byte2hex(getPlainKeyBytes(key));
	}
	
	public byte[] getPlainKeyBytes(String key) throws Exception {
		SymDecryptOperator symDecrypt = new SymDecryptOperator(
				EncryptContants.getEncryptKeyAlgorithm(encryptKeyAlgorithm),
				EncryptContants.getEncryptKeyPadding(encryptKeyPadding),
				Lmk.LMK_CODE,
				EncryptContants.getEncryptKeyIV(encryptKeyIV));
		return symDecrypt.Boc_Decrypt(BytesUtil.hex2byte(key));
	}
	
	/**
	 * 公共组件加密数据库密码等敏感数据时，使用PKCS#5 Padding方式；密钥则不使用Padding。
	 * 应用系统在首尔项目中针对客户证件号码加密时，不使用padding，也就是说应用系统自身控制加密明文长度为16的整数倍。
	 * 
	 * 针对功能3，加密保护敏感信息，由于目前CSPS已有对数据库用户和密码加密的工具，且采用了AES算法nopadding方式。 
	 * 如按组件要求使用SM4算法则会牵涉到相应相关应用产品的修改，影响较大。根据该功能的实际使用用途，
	 * 暂时使用原方式(AES、NoPadding)展示敏感信息密文。后续根据国密算法改造的任务进行标准化改造。
	 */
	public String encryptInfo(String cotent) throws Exception {
		SymEncryptOperator symEncrypt = new SymEncryptOperator(
				EncryptContants.getEncryptTextAlgorithm(encryptTextAlgorithm),
				EncryptContants.getEncryptTextPadding(encryptTextPadding),
				Lmk.LMK_CODE,
				EncryptContants.getEncryptTextIV(encryptTextIV));
		byte[] encryptInfo = symEncrypt.Boc_Encrypt(cotent.getBytes());
		return BytesUtil.byte2hex(encryptInfo);
	}

	public String decryptInfo(String cotent) throws Exception {
		SymDecryptOperator symDecrypt = new SymDecryptOperator(
				EncryptContants.getEncryptTextAlgorithm(encryptTextAlgorithm),
				EncryptContants.getEncryptTextPadding(encryptTextPadding),
				Lmk.LMK_CODE,
				EncryptContants.getEncryptTextIV(encryptTextIV));
		byte[] decryptInfo = symDecrypt.Boc_Decrypt(BytesUtil.hex2byte(cotent));
		return new String(decryptInfo);
	}

	public String getEncryptKeyAlgorithm() {
		return encryptKeyAlgorithm;
	}

	public void setEncryptKeyAlgorithm(String encryptKeyAlgorithm) {
		this.encryptKeyAlgorithm = encryptKeyAlgorithm;
	}

	public String getEncryptKeyIV() {
		return encryptKeyIV;
	}

	public void setEncryptKeyIV(String encryptKeyIV) {
		this.encryptKeyIV = encryptKeyIV;
	}

	public String getEncryptTextAlgorithm() {
		return encryptTextAlgorithm;
	}

	public void setEncryptTextAlgorithm(String encryptTextAlgorithm) {
		this.encryptTextAlgorithm = encryptTextAlgorithm;
	}

	public String getEncryptTextIV() {
		return encryptTextIV;
	}

	public void setEncryptTextIV(String encryptTextIV) {
		this.encryptTextIV = encryptTextIV;
	}

	public String getEncryptKeyPadding() {
		return encryptKeyPadding;
	}

	public void setEncryptKeyPadding(String encryptKeyPadding) {
		this.encryptKeyPadding = encryptKeyPadding;
	}

	public String getEncryptTextPadding() {
		return encryptTextPadding;
	}

	public void setEncryptTextPadding(String encryptTextPadding) {
		this.encryptTextPadding = encryptTextPadding;
	}

}
