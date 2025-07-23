package com.bocsoft.wwsf.webconsole.encoder.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class MD5PasswordEncoderImpl extends AbstractPasswordEncoderImpl {
	
	public MD5PasswordEncoderImpl() {}

	protected String encodeInternal(String input) {
		if (!isEncodeHashAsBase64()) {
			return DigestUtils.md5Hex(input);
		}
		byte[] encoded = Base64.encodeBase64(DigestUtils.md5(input));
		return new String(encoded);
	}
	
}
