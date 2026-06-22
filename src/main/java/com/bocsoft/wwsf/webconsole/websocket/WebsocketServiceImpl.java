package com.bocsoft.wwsf.webconsole.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.websocket.Session;

import com.bocsoft.wwsf.webconsole.SshClientUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketServiceImpl implements WebsocketService {
	
	private Logger logger = LoggerFactory.getLogger(WebsocketServiceImpl.class);
	private SshClient sshClient = null;
	private ClientSession clientSession = null;
	private ClientChannel clientChannel = null;
	public String charsetName;
	//线程池
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean stopProcessWebInfo = false;

	public void connect(SshConnInfo sshConnInfo,Session session) throws Exception{
		try{
			charsetName = sshConnInfo.getCharsetName();
			sshClient = SshClient.setUpDefaultClient();
			sshClient.start();
			ConnectFuture connectFuture = null;
			try{
				connectFuture = sshClient.connect(sshConnInfo.getOsSshUser(),sshConnInfo.getIpAddr(),sshConnInfo.getOsSshPort());
			}catch(Exception e){
				throw new Exception("连接失败");
			}
			connectFuture.await();
			if(!connectFuture.isConnected()){
				throw new Exception("连接失败");
			}
			clientSession = connectFuture.getSession();

			// SSH服务认证改造，支持公钥认证（免密）和密码认证两种方式，通过配置文件配置选择认证方式，默认公钥认证
			String authStr = "";
			if(SshClientUtils.Ssh_Default_OsSshAuth.equals(sshConnInfo.getOsSshAuth())){
				authStr = " 【公钥认证】";
				KeyPair keyPair = SshClientUtils.loadPrivateKey(sshConnInfo.getPrivateKeyPath(),sshConnInfo.getPassphrase());
				if(keyPair == null ){
					throw new Exception("用户名：" + sshConnInfo.getOsSshUser() + " 无法加载私钥,验证失败.");
				}
				clientSession.addPublicKeyIdentity(keyPair);
			}else {
				authStr = " 【密码认证】";
				clientSession.addPasswordIdentity(sshConnInfo.getOsSshPswd());
			}

			AuthFuture authFuture = clientSession.auth();
			authFuture.await();
			if(!authFuture.isSuccess()){
				throw new Exception("用户名：" + sshConnInfo.getOsSshUser() + authStr + " 连接失败");
			}
		}catch(Exception e){
			try{
				sshClient.stop();
			}catch(Exception e2){
				e2.printStackTrace();
			}
			throw e;
		}
		processWebInfo("\r",session); //有web页面调用
	}
	
	public void processWebInfo(String message, Session session){
		try{
			//logger.info(session.getId()+"/ input: "+message);
			if(null == clientChannel){
				clientChannel = clientSession.createChannel(Channel.CHANNEL_SHELL);
				clientChannel.open().verify(9L, TimeUnit.SECONDS);
				executorService.execute(new Runnable() {
	                @Override
	                public void run() {
	    		        //读取终端返回的信息流
                		InputStream inputStream = clientChannel.getInvertedOut();
	    		        try {
	    		            //循环读取
	    		            byte[] buffer = new byte[1024];
	    		            int i = 0;
	    		            //如果没有数据来，线程会一直阻塞在这个地方等待数据。
	    		            while (!stopProcessWebInfo && (i = inputStream.read(buffer)) != -1) {
	    		                sendMessage(session, Arrays.copyOfRange(buffer, 0, i));
	    		            }
	    		            session.close();
	    		        } catch(Exception e){
	    		        	e.printStackTrace();
	    		        } finally {
	    		            if (inputStream != null) {
	    		                try {
									inputStream.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
	    		            }
	    		        }
	                }
	            });
			}else{
				if(clientChannel.isOpen()){
					transToSSH(message);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void sendMessage(Session session, byte[] buffer) throws IOException {
		//输入：Ctrl+C,会返回^C，替换掉^C
		String retValue = Xterm.replaceString(buffer,charsetName);
		logger.info(session.getId()+"/ return: "+retValue);
		session.getBasicRemote().sendText(retValue);
    }
	
	private void transToSSH(String message){
		try{
			OutputStream outputStream = clientChannel.getInvertedIn();
			outputStream.write(message.getBytes());
            outputStream.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void close(){
		stopProcessWebInfo = true;
		try{
			if(null != clientChannel){ 
				clientChannel.close(); 
			}
		}catch(Exception e){}
		try{
			if(null != clientSession){ 
				clientSession.close(); 
			}
		}catch(Exception e){}
		try{
			if(null != sshClient){
				sshClient.stop();
			}
		}catch(Exception e){}
	}
	
	public void sendConnectionCloseInfo(String info, Session session){
		try{
			if(null != session && session.isOpen()){
				sendMessage(session, info.getBytes());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}
	
}
