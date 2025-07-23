package com.bocsoft.wwsf.webconsole.websocket;

public class Command {
	
	private String type;
	private String command;
	
	public final static String Type_Command = "command";
	public final static String Type_Encoding = "encoding";
	public final static String Type_Close = "close";
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
}
