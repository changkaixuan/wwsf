package com.bocsoft.wwsf.webconsole.cryptor;

public class BadKeySectionException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public BadKeySectionException(){
		super();
	}
	
	public BadKeySectionException(String msg){
		super(msg);
	}
}
