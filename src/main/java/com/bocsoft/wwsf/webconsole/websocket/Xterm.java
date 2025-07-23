package com.bocsoft.wwsf.webconsole.websocket;

import java.io.IOException;

public class Xterm {

	public static final String COMMAND_CTRL_C = "^C"; //Ctrl+C
	
	public static String replaceString(byte[] buffer,String charsetName) throws IOException{
		String retValue = new String(buffer,charsetName); //执行Ctrl+C,会返回^C
		if(retValue.indexOf(Xterm.COMMAND_CTRL_C) != -1){ //执行Ctrl+C,会返回^C
			retValue = retValue.replace(Xterm.COMMAND_CTRL_C, ""); 
		}
		return retValue;
	}
	
}
