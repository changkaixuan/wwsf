package com.bocsoft.wwsf.webconsole.encoder.impl;

import com.bocsoft.wwsf.webconsole.encoder.PasswordEncoder;

public abstract class AbstractPasswordEncoderImpl implements PasswordEncoder {

	private boolean encodeHashAsBase64 = false;
    /**
     * @return return encodeHashAsBase64.
     */
    public boolean isEncodeHashAsBase64() {
        return encodeHashAsBase64;
    }
	/**
	 * 子类必须提供这个方法的实现，通过不同的编码算法来加密相应的明文密码 例如MD5、SHA算法等等
	 * 
	 * @param input
	 *            明文密码
	 * 
	 * @return String 加密密码
	 */
	protected abstract String encodeInternal(String input);

	public boolean isPasswordValid(String encPassword, String rawPassword,
			Object salt) {
		String pass1 = "" + encPassword;
		String pass2 = encodeInternal(mergePasswordAndSalt(rawPassword, salt,
				false));
		return pass1.equals(pass2);
	}

	public String encodePassword(String rawPassword, Object salt) {
		return encodeInternal(mergePasswordAndSalt(rawPassword, salt, false));
	}

	public String decodePassword(String endPassword) {
		return null;
	}

	private String mergePasswordAndSalt(String password, Object salt,
			boolean strict) {
		if (password == null) {
			password = "";
		}
		if (strict && (salt != null)) {
			if ((salt.toString().lastIndexOf("{") != -1)
					|| (salt.toString().lastIndexOf("}") != -1)) {
				throw new IllegalArgumentException(
						"Cannot use { or } in salt.toString()");
			}
		}
		if ((salt == null) || "".equals(salt)) {
			return password;
		} else {
			return password + "{" + salt.toString() + "}";
		}
	}
	
}
