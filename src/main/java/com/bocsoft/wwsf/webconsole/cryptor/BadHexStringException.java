package com.bocsoft.wwsf.webconsole.cryptor;

public class BadHexStringException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public BadHexStringException(){
		super();
	}
	
	public BadHexStringException(String msg){
		super(msg);
	}
}
