package com.bocsoft.wwsf.webconsole.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import java.net.URLDecoder;
import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/webssh/{serverJson}")
@Component
public class WebSocketServer {

	private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
	
	private WebsocketService websocketService = null;
	
	@PostConstruct
    public void init() {
        //logger.info("apache sshd start load wwsf websocket...");
    }

    //连接建立成功调用的方法
    @OnOpen
    public void onOpen(Session session,@PathParam("serverJson") String serverJson){
        try{
        	serverJson = URLDecoder.decode(serverJson, "UTF-8");
        	SshConnInfo sshConnInfo = JSONObject.parseObject(serverJson, SshConnInfo.class);
        	//根据产品号和节点号，取节点信息
        	websocketService = new WebsocketServiceImpl();
        	websocketService.connect(sshConnInfo,session);
        	logger.info("create websocket session：" + session.getId());
        }catch(Exception e){
        	logger.error("webssh连接异常:{}", e.getMessage());
        	try {
        		session.close();
        	}catch(Exception e2) {
        		e2.printStackTrace();
        	}
        }
    }
    
    //收到客户端消息后调用的方法
    @OnMessage
    public void onMessage(String message, Session session) {
    	try{
    		Command command = JSONObject.parseObject(message, Command.class);
    		if(command.getType().equals(Command.Type_Close)) {
    			session.close();
    		}else if(command.getType().equals(Command.Type_Encoding)) {
    			websocketService.setCharsetName(command.getCommand());
    		}else if(command.getType().equals(Command.Type_Command)) {
    			websocketService.processWebInfo(command.getCommand(), session);
    		}else {
    			logger.error("not exists command type: "+command.getType());
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    		try{
    			websocketService.sendConnectionCloseInfo("error connect colsed.", session);
    			session.close();
    		}catch(Exception e2){
    			e2.printStackTrace();
    		}
    	}
    }

    //连接关闭调用的方法
    @OnClose
    public void onClose(Session session) {
    	if(null != websocketService){
    		logger.info("close websocket session：" + session.getId());
    		try{
    			websocketService.close();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    }

    //出现错误
    @OnError
    public void onError(Session session, Throwable error) {
    	logger.info("发生错误：" + error.getMessage() + "，Session ID： " + session.getId());
        error.printStackTrace();
    }

}
