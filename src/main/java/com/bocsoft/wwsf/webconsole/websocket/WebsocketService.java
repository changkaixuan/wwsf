package com.bocsoft.wwsf.webconsole.websocket;

import javax.websocket.Session;

public interface WebsocketService {

	public void connect(SshConnInfo sshConnInfo,Session session) throws Exception;
	
	public void sendConnectionCloseInfo(String info,Session session);
	
	public void processWebInfo(String message, Session session);
	
	public void setCharsetName(String charsetName);
	
	public void close();
	
}
