package com.bocsoft.wwsf.webconsole.encoder;

public interface PasswordEncoder {

	public boolean isPasswordValid(String encPassword , String rawPassword , Object salt);
	
	public String encodePassword(String rawPassword , Object salt);
	
	public String decodePassword(String encPassword);
	
}
