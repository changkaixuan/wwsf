package com.bocsoft.wwsf.webconsole;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FtpClientUtils {
	
	/** 
	* Description: 向FTP服务器上传文件 
	* @param url FTP服务器hostname 
	* @param port FTP服务器端口 
	* @param username FTP登录账号 
	* @param password FTP登录密码 
	* @param srcFileFullPath 源文件全路径 
	* @param destFilePath FTP服务器保存目录    值格式：/adpswork/workspace/test
	* @param destFileName 上传到FTP服务器上的文件名   
	* @return 
	*/
	public static void uploadFile(String ip,int port,String username, String password, String srcFileFullPath,String destFilePath, String destFileName) throws Exception{ 
		FTPClient fTPClient = null;
		InputStream srcFileInputStream = null;
		try { 
			File srcFile = new File(srcFileFullPath);
			if(!srcFile.exists()){
				throw new Exception("源目录 " + srcFileFullPath+" 不存在.");
			}
			fTPClient = new FTPClient();
			srcFileInputStream = new FileInputStream(srcFile);
			int reply; 
			fTPClient.connect(ip, port);//连接FTP服务器 
			//如果采用默认端口，可以使用ftp.connect(ip)的方式直接连接FTP服务器 
			fTPClient.login(username, password);//登录 
			reply = fTPClient.getReplyCode(); 
			
			//判断reply的code是否正常，一般正常为2开头的code
			if (!FTPReply.isPositiveCompletion(reply)) { 
				fTPClient.disconnect();
				throw new Exception("FTP "+ip+":"+port+" 无应答.");
			} 
			
			//boolean a = fTPClient.changeWorkingDirectory("/"); 
			//System.out.println("转换目录结果1:"+a);
			
			//if(StringUtils.isBlank(destFilePath) || destFilePath.equals("/")){//如果输入的路径为空或者为根路径，则不转换操作目录
			if(null == destFilePath || destFilePath.equals("") || destFilePath.equals("/")){//如果输入的路径为空或者为根路径，则不转换操作目录
				fTPClient.changeWorkingDirectory("/"); 
			}else{//否则创建想要上传文件的目录，并且将操作目录转为新创建的目录
				//fTPClient.makeDirectory(path);
				boolean isDirectory = fTPClient.changeWorkingDirectory(destFilePath); 
				if(!isDirectory){
					throw new Exception("目标目录 " +  destFilePath + " 不存在.");
				}
				//System.out.println("转换目录结果2:"+b);
			}
			
			//ftp上传文件是以文本形式传输的，所以多媒体文件会失真，需要转为二进制形式传输
			fTPClient.setFileType(FTP.BINARY_FILE_TYPE);
			//转码后可以文件名可以为中文
			//fTPClient.storeFile(new String(destFileName.getBytes("GBK"), "iso-8859-1"), srcFileInputStream);
			fTPClient.storeFile(destFileName, srcFileInputStream);
			
			fTPClient.logout(); 
		} catch (Exception e) { 
			throw new Exception(e.getMessage());
		} finally { 
			if(null != srcFileInputStream){
				srcFileInputStream.close(); 
			}
			if (null != fTPClient && fTPClient.isConnected()) { 
				try { 
					fTPClient.disconnect(); 
				} catch (Exception ioe) { 
					
				} 
			} 
		} 
	}
	
	/*public static void main(String[] args) {
		try {
			//uploadFile("22.188.13.105", 21, "dcds", "Aa-dcds01!","E:/wwseWorkspace0430/wwse.sandbox/wwsf-v1.0.0-20190816.zip", "/dcds/test/install", "wwsf-v1.0.0-20190816.zip");
			//uploadFile("22.188.13.105", 21, "dcds", "Aa-dcds01!","F:/test测试.bat", "/dcds/test/install", "test测试.bat");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}*/
	
}
