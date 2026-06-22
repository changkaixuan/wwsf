package com.bocsoft.wwsf.webconsole;

import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.apache.sshd.client.subsystem.sftp.extensions.SpaceAvailableExtension;
import org.apache.sshd.common.subsystem.sftp.SftpException;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bocsoft.wwsf.webconsole.model.DbConn;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.Server;

public class SshClientUtils {

	private static final Logger log = LoggerFactory.getLogger(SshClientUtils.class);
	//wws.cfg中配置项与值的分隔符
	private static final String SPLIT_KV = " ";
	private static final String space2 = "      ";
	private static final String space4 = "            ";
	/**
	 * SHH 授权默认1-公钥认证方式
	 */
	public final static String Ssh_Default_OsSshAuth = "1";

	public static String getNowTime(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}
	
	/*public static String getLogFileRowStartInfo(boolean addSpace4) {
		return getNowTime("yyy-MM-dd HH:mm:ss") + "  INFO " + (addSpace4 ? space4 : "");
	}*/
	
	public static String getLogFileRowStartError() {
		//return getNowTime("yyy-MM-dd HH:mm:ss") + " ERROR ";
		return "出错: ";
	}

	public static void uploadFileToSftp(ClientSession session, InputStream inputStream, String genFilePath) throws Exception {
		SftpClient sftpClient = null;
		BufferedInputStream bis = null;
		OutputStream outputStream = null;
		try {
			sftpClient = session.createSftpClient();
			SpaceAvailableExtension space = sftpClient.getExtension(SpaceAvailableExtension.class);
			if (space != null) {
				outputStream = sftpClient.write(genFilePath);//       /dcds/ssh/readme.txt
				bis = new BufferedInputStream(inputStream);
				byte[] buffer = new byte[8192];
				int i = 0;
				while ((i = bis.read(buffer)) > 0) {
					outputStream.write(buffer, 0, i);
				}
			}
		} catch(SftpException se) {
			throw new Exception("SftpException");
	    } catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if (null != bis) {
				bis.close();
			}
			if (null != outputStream) {
				outputStream.close();
			}
			if (null != inputStream) {
				inputStream.close();
			}
			if (null != sftpClient) {
				sftpClient.close();
			}
		}
	}
	
	public static String install(Map<String, Object> paraMap, List<String> uploadFilePathList, StringBuffer logStringBuffer) throws Exception {
		SshClient client = null;
		ClientSession session = null;
		Server server = (Server) paraMap.get("Server");
		String userName = server.getOsSshUser();
		String password = server.getOsSshPswd();
		String ip = server.getIpAddr();
		int port = server.getOsSshPort();

		// 获取到SH认证方式、密钥路径、密钥密码
		String sshAuth = (String) paraMap.get("sshAuth");
		String privateKeyPath = (String)paraMap.get("privateKeyPath");
		String passphrase = (String)paraMap.get("passphrase");
		try {
			logStringBuffer.append(getNowTime("yyy-MM-dd HH:mm:ss") + " 安装产品/服务器【 " + server.getProduct() + "/" + server.getServerId() + "】\r\n");
			logStringBuffer.append("第1步 建立连接 " + userName + "/***" + "@" + ip + ":" + port + "\r\n");
			client = SshClient.setUpDefaultClient();
			client.start();
			ConnectFuture connectFuture = null;
			try {
				connectFuture = client.connect(userName, ip, port);
			}catch(Exception ee) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + userName + "@" + ip + ":" + port + " 建立连接失败." + "\r\n");
				throw new Exception(userName + "@" + ip + ":" + port + " 建立连接失败.");
			}
			connectFuture.await();
			if (!connectFuture.isConnected()) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + userName + "@" + ip + ":" + port + " 建立连接失败." + "\r\n");
				throw new Exception(userName + "@" + ip + ":" + port + " 建立连接失败.");
			}
			session = connectFuture.getSession();

            // SSH服务认证改造，支持公钥认证（免密）和密码认证两种方式，通过配置文件配置选择认证方式，默认公钥认证
			String authStr = "";
			if(Ssh_Default_OsSshAuth.equals(sshAuth)){
				authStr = " 【公钥认证】";
				logStringBuffer.append(" 选择【公钥认证】." + "\r\n");
				KeyPair keyPair = loadPrivateKey(privateKeyPath,passphrase);
				if(keyPair == null ){
					throw new Exception("用户名：" + userName + " 无法加载私钥,验证失败.");
				}
				session.addPublicKeyIdentity(keyPair);
			}else {
				authStr = " 【密码认证】";
				logStringBuffer.append(" 选择【密码认证】." + "\r\n");
				session.addPasswordIdentity(password);
			}
			AuthFuture authTrue = session.auth();
			authTrue.await();
			if (!authTrue.isSuccess()) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + "用户名：" + userName  + authStr + " 验证失败." + "\r\n");
				throw new Exception("用户名：" + userName   + authStr +  " 验证失败.");
			}

			//通过执行Shell命令来创建目录    /hadoop/cirrus
			String homeDir = server.getHomeDir();//值：/hadoop/cirrus
			//目录agentHome（/hadoop/cirrus）不存在则自动创建
			logStringBuffer.append("第2步 创建目录并设置权限 " + homeDir + "\r\n");
			String commands[] = new String[] { "mkdir -p " + homeDir };
			executeCommands(session, commands, paraMap, logStringBuffer, false);
			//设置目录权限homeDir
			commands = new String[] { "chmod u+x " + homeDir};
			executeCommands(session, commands, paraMap, logStringBuffer, false);
			//将压缩后的安装文件(karaf-4.0.9.zip、maven-3.3.3.zip)拷贝到目录/hadoop/cirrus下并解压
			//Map<String,String> installMap = new HashMap<String,String>();//key=maven-3.3.3,value=/hadoop/cirrus/maven-3.3.3
			//String installFileName = (String) karafMap.get("installFileName");
			//String karafHome = homeDir + "/" + installFileName;
			String karafHome = homeDir;
			String javaHome = getJavaHome(session, paraMap, logStringBuffer); //javaHome值格式： export JAVA_HOME=/usr/java/jdk1.8.0_161
			if (null == javaHome) {
				throw new Exception("未安装或未设置JDK");
			}
			logStringBuffer.append("第3步 上传文件并解压" + "\r\n");
			if (null != uploadFilePathList && uploadFilePathList.size() > 0) {
				//删除已安装文件
				//commands = new String[] { "cd " + homeDir + ";rm -rf " + installFileName};
				commands = new String[] { "cd " + homeDir + ";rm -rf *"};
				executeCommands(session, commands, paraMap, logStringBuffer, false);
				boolean sftp = true;
				for (int i = 0; i < uploadFilePathList.size(); i++) {//循环要拷贝的本地文件列表
					String uploadAgentFilePath = uploadFilePathList.get(i);//本地文件路径
					File file = new File(uploadAgentFilePath);
					//删除上传过的文件  
					//rm参数 【 -r:递归删除目录及其内容   -f:强制删除，忽略不存在的文件，不提示确认】
					commands = new String[] { "cd " + homeDir + ";rm -rf " + file.getName()};
					executeCommands(session, commands, paraMap, logStringBuffer, false);
					//上传文件
					InputStream inputStream = null;
					try {
						inputStream = new FileInputStream(file);
						logStringBuffer.append(space2 + "文件：" + file.getName() + " 上传到  " + ip + ":" + port + " " + homeDir + "\r\n");
						if(sftp) {
							try {
								uploadFileToSftp(session, inputStream, homeDir + "/" + file.getName());//genFilePath + "/" + file.getName() = /hadoop/cirrus/maven-3.3.3.zip
							}catch(Exception e) {
								if(e.getMessage().equals("SftpException")) {//改用ftp方式上传
									sftp = false;
									FtpClientUtils.uploadFile(ip, 21, userName, password, homeDir + "/" + file.getName(), homeDir, file.getName()); //new String(file.getName().getBytes("UTF-8"), "iso-8859-1")
								}else{
									throw e;
								}
							}
						}else{
							FtpClientUtils.uploadFile(ip, 21, userName, password, homeDir + "/" + file.getName(), homeDir, file.getName());
						}
					} catch (Exception e) {
						e.printStackTrace();
						logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
						throw new Exception("上传文件 " + file.getName() + " 到 " + ip + ":" + port + " " + homeDir + " 失败.");
					}
					//解压文件
					if (file.getName().indexOf(".zip") != -1) {
						//解压上传的文件
						//unzip参数 【 -q:执行时不显示任何信息   -o:不必先询问用户，覆盖原有的文件 -d:指定文件解压后所要存储的目录】
						//commands = new String[] { "cd " + homeDir + ";unzip -q -o -d " + homeDir + " " + file.getName() };
						logStringBuffer.append(space2 + "文件：" + file.getName() + " 解压到  " + homeDir + "\r\n");
						commands = new String[] { "cd " + homeDir + ";unzip -q -o -d " + homeDir + " " + file.getName() };
						//linux查看unzip安装路径命令：which unzip
						//commands = new String[] { "cd " + homeDir + ";export PATH=$PATH:/oracle/product/12c/rdbms/bin;unzip -q -o -d " + homeDir + " " + file.getName() };
						try {
							executeCommands(session, commands, paraMap, logStringBuffer, false);
						}catch(Exception unzipException) {
							if(unzipException.getMessage().indexOf("bash: unzip: command not found") != -1) {
								throw new Exception("操作失败，系统未安装unzip");
							}else {
								throw unzipException;
							}
						}
						//删除zip文件
						logStringBuffer.append(space2 + "删除压缩文件：" + homeDir + "/" + file.getName() + "\r\n");
						commands = new String[] { "cd " + homeDir + ";rm -rf " + file.getName() };
						executeCommands(session, commands, paraMap, logStringBuffer, false);
						//移动文件文件
						String tempDir = homeDir + "/" + Server.Server_Default_Install_Name;
						logStringBuffer.append(space2 + "目录：" + tempDir + " 下所有文件移动到目录 " + homeDir + " 下\r\n");
						commands = new String[] { "cd " + tempDir + ";mv * " + homeDir };
						executeCommands(session, commands, paraMap, logStringBuffer, false);
						//删除被移动的目录
						logStringBuffer.append(space2 + "删除解压目录：" + tempDir + "\r\n");
						commands = new String[] { "rm -rf " + tempDir };
						executeCommands(session, commands, paraMap, logStringBuffer, false);
						//删除目录instances
						logStringBuffer.append(space2 + "删除目录：" + homeDir + "/instances" + "\r\n");
						commands = new String[] { "cd " + homeDir + ";rm -rf instances"};
						executeCommands(session, commands, paraMap, logStringBuffer, false);
						//删除目录data
						logStringBuffer.append(space2 + "删除目录：" + homeDir + "/data" + "\r\n");
						commands = new String[] { "cd " + homeDir + ";rm -rf data"};
						executeCommands(session, commands, paraMap, logStringBuffer, false);
						//删除文件karaf.pid
						logStringBuffer.append(space2 + "删除文件：" + homeDir + "/karaf.pid" + "\r\n");
						commands = new String[] { "cd " + homeDir + ";rm -rf karaf.pid"};
						executeCommands(session, commands, paraMap, logStringBuffer, false);
						//删除文件lock
						logStringBuffer.append(space2 + "删除文件：" + homeDir + "/lock" + "\r\n");
						commands = new String[] { "cd " + homeDir + ";rm -rf lock"};
						executeCommands(session, commands, paraMap, logStringBuffer, false);
						/*//设置解压后的zip文件权限
						commands = new String[] { "chmod 775 " + karafHome };
						executeCommands(session, commands, paraMap, logStringBuffer, true);
						commands = new String[] { "cd " + karafHome + "/bin;chmod u+x *" };
						executeCommands(session, commands, paraMap, logStringBuffer, true);*/
					}
				}
				//设置安装目录权限homeDir
				logStringBuffer.append("第4步 设置安装目录权限 " + homeDir + "\r\n");
				commands = new String[] { "cd " + homeDir + ";chmod -R u+x *" };  //-R:对目前目录下的所有文件与子目录进行相同的权限变更（即以递归的方式逐个变更）
				executeCommands(session, commands, paraMap, logStringBuffer, false);
				//替换文件中参数
				logStringBuffer.append("第5步  修改文件" + "\r\n");
				//1.wws-2.0.0\etc\wws.cfg
				logStringBuffer.append(space2 + "文件1 " + karafHome + "/etc/wws.cfg" + "\r\n");
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","node.ip"," ",server.getIpAddr());
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","node.id"," ",server.getServerId());
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","node.type"," ",server.getServerType());
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","node.product"," ",server.getProduct());
				Product tProduct = (Product)paraMap.get("product");
				String prodDesc = "";
				if(null != tProduct.getpDesc() && !tProduct.getpDesc().equals("")) {
					prodDesc = tProduct.getpDesc();
				}
				replaceFileContentAscii(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","node.product.desc"," ",prodDesc,StringUtil.stringToAscii(prodDesc));
				//替换数据库连接
				DbConn dbConn = (DbConn)paraMap.get("dbConn");
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","mybatis.driver"," ",dbConn.getDbDriver());
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","mybatis.url"," ",dbConn.getDbUrl());
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","mybatis.username"," ",dbConn.getDbUserName());
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","mybatis.password"," ",dbConn.getENCDbPassword());
				//替换zookeeper命名空间
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","zoo.namespace"," ",server.getZooNamespace());
				//替换zookeeper连接串
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","zoo.connect.string"," ",server.getZooConnectStr());
				//替换zookeeper密码
				String zooAclString = (String)paraMap.get("zooAclString");
				if(zooAclString.equals("")) {//注释配置文件(wws.cfg)中属性zoo.acl.string
					annotationFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","zoo.acl.string"," ");
				}else{
					replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","zoo.acl.string"," ",zooAclString);
				}
				//替换ignore.plugins
				String tIgnorePlugins = "-";
				if(null != server.getIgnorePlugins() && !server.getIgnorePlugins().equals("")) {
					tIgnorePlugins = server.getIgnorePlugins();
				}
				replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/wws.cfg","ignore.plugins"," ",tIgnorePlugins);
				//2.wws-2.0.0\etc\org.apache.karaf.management.cfg
				logStringBuffer.append(space2 + "文件2 " + karafHome + "/etc/org.apache.karaf.management.cfg" + "\r\n");
				if(server.getRmiServerPort() > 0){//44444
					replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/org.apache.karaf.management.cfg","rmiServerPort"," = ",String.valueOf(server.getRmiServerPort()));
				}
				if(server.getRmiRegistryPort() > 0) {//1099
					replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/org.apache.karaf.management.cfg","rmiRegistryPort"," = ",String.valueOf(server.getRmiRegistryPort()));
				}
				//3.wws-2.0.0\etc\org.apache.karaf.shell.cfg
				logStringBuffer.append(space2 + "文件3 " + karafHome + "/etc/org.apache.karaf.shell.cfg" + "\r\n");
				if(server.getSshPort() > 0){
					replaceFileContent(session,paraMap,logStringBuffer,karafHome+"/etc/org.apache.karaf.shell.cfg","sshPort"," = ",String.valueOf(server.getSshPort()));
				}
				//4.wws-2.0.0\bin\setenv (设置：1.JAVA_HOME; 2.KARAF_HOME)
				logStringBuffer.append(space2 + "文件4 " + karafHome + "/etc/setenv" + "\r\n");
				List<String> fileContentList = new ArrayList<String>();
				fileContentList.add(javaHome);
				fileContentList.add("export KARAF_HOME="+karafHome);
				appendFileContent(session,paraMap,logStringBuffer,karafHome+"/bin/setenv",fileContentList);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != session) {
				session.close();
			}
			if (null != client) {
				client.stop();
			}
		}
		return null;
	}
	
	public static String uninstall(Map<String, Object> paraMap, StringBuffer logStringBuffer) throws Exception {
		SshClient client = null;
		ClientSession session = null;
		Server server = (Server) paraMap.get("Server");
		String userName = server.getOsSshUser();
		String password = server.getOsSshPswd();
		String ip = server.getIpAddr();
		int port = server.getOsSshPort();

		// 获取到SH认证方式、密钥路径、密钥密码
		String sshAuth = (String) paraMap.get("sshAuth");
		String privateKeyPath = (String)paraMap.get("privateKeyPath");
		String passphrase = (String)paraMap.get("passphrase");

		try {
			logStringBuffer.append(getNowTime("yyy-MM-dd HH:mm:ss") + " 卸载产品/服务器【" + server.getProduct() + "/" + server.getServerId() + "】\r\n");
			logStringBuffer.append("第1步 建立连接 " + userName + "/***" + "@" + ip + ":" + port + "\r\n");
			client = SshClient.setUpDefaultClient();
			client.start();
			ConnectFuture connectFuture = null;
			try {
				connectFuture = client.connect(userName, ip, port);
			}catch(Exception ee) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + userName + "@" + ip + ":" + port + " 建立连接失败." + "\r\n");
				throw new Exception(userName + "@" + ip + ":" + port + " 建立连接失败.");
			}
			connectFuture.await();
			if (!connectFuture.isConnected()) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + userName + "@" + ip + ":" + port + " 建立连接失败." + "\r\n");
				throw new Exception(userName + "@" + ip + ":" + port + " 建立连接失败.");
			}
			session = connectFuture.getSession();

			// SSH服务认证改造，支持公钥认证（免密）和密码认证两种方式，通过配置文件配置选择认证方式，默认公钥认证
			String authStr = "";
			if(Ssh_Default_OsSshAuth.equals(sshAuth)){
				authStr = " 【公钥认证】";
				logStringBuffer.append(" 选择【公钥认证】." + "\r\n");
				KeyPair keyPair = loadPrivateKey(privateKeyPath,passphrase);
				if(keyPair == null ){
					throw new Exception("用户名：" + userName + " 无法加载私钥,验证失败.");
				}
				session.addPublicKeyIdentity(keyPair);
			}else {
				authStr = " 【密码认证】";
				logStringBuffer.append(" 选择【密码认证】." + "\r\n");
				session.addPasswordIdentity(password);
			}

			AuthFuture authTrue = session.auth();
			authTrue.await();
			if (!authTrue.isSuccess()) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + "用户名：" + userName  + authStr + " 验证失败." + "\r\n");
				throw new Exception("用户名：" + userName   + authStr +  " 验证失败.");
			}

			//通过执行Shell命令来创建目录    /hadoop/cirrus
			String homeDir = server.getHomeDir();//值：/hadoop/cirrus
			//目录agentHome（/hadoop/cirrus）不存在则自动创建
			logStringBuffer.append("第2步 删除目录 " + homeDir + " 下文件.\r\n");
			String commands[] = new String[] { "rm -rf " + homeDir + "/*" };
			executeCommands(session, commands, paraMap, logStringBuffer, false);
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != session) {
				session.close();
			}
			if (null != client) {
				client.stop();
			}
		}
		return null;
	}
	
	//注释属性(不存在就忽略)
	public static boolean annotationFileContent(ClientSession session, Map<String, Object> paraMap, StringBuffer logStringBuffer, String filePath, String key, String splitStr) throws Exception {
		String commands[] = null;
		//判断文件是否包含指定内容"key+splitStr+oValue"
		String rowNoAndContent = getRowNoAndContent2(session, paraMap, logStringBuffer, filePath, key+splitStr);//findContent = key+splitStr+oValue = redis.ip
		if(null != rowNoAndContent){
			int needRepRowNum = Integer.parseInt(rowNoAndContent.split(":")[0]);
			String rowContent = rowNoAndContent.replace(needRepRowNum+":", "").replace("\r", "");
			if(rowContent.split(splitStr).length != 2) {//"文件"+ filePath + "中，不存在"+ key
				return true;
			}
			logStringBuffer.append(space4 + "注释内容" + " " + rowContent + "\r\n");
			//替换文件内容
			//1.修改后的内容存入临时文件
			try {
				//sed 1,20s/old/new/g filePath>filePath.tmp
				commands = new String[] { "sed '"+needRepRowNum+"s/" + (rowContent).replace("/", "\\/") + "/"
						+ ("#"+rowContent).replace("/", "\\/").replace("&", "\\&") + "/g' " + filePath + ">"
						+ filePath + ".tmp" };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("写入临时文件失败[" + filePath + " " + key + ": " + "#"+rowContent + "]");
			}
			//2.将临时文件内容存入原文件
			try {
				commands = new String[] { "mv " + filePath + ".tmp" + " " + filePath };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("修改文件失败[" + filePath + " " + key + ": " + "#"+rowContent + "]");
			}
		}
		return true;
	}

	//文件中key对应的value值有单行
	public static boolean replaceFileContent(
			ClientSession session, 
			Map<String, Object> paraMap, 
			StringBuffer logStringBuffer, 
			String filePath, 
			String key, 
			String splitStr, 
			String replaceValue) throws Exception {
		
		if (!StringUtil.hasText(replaceValue)) {
			logStringBuffer.append(space4 + "配置项（" + key + "）未赋值").append(System.lineSeparator());
			throw new Exception("配置项（" + key + "）未赋值");
		}
		//1:node.ip 127.0.0.1
		
		//判断文件是否包含指定内容"key+splitStr+oValue"
		String rowNoAndContent = getRowNoAndContent(session, paraMap, logStringBuffer, filePath, key+splitStr);//findContent = key+splitStr+oValue = redis.ip
		int rnum = Integer.parseInt(rowNoAndContent.split(":")[0].trim());
		String rowContent = rowNoAndContent.replace(rnum+":", "").replace("\r", "");
		String[] prop = rowContent.split(splitStr);
		if(prop.length != 2) {
			logStringBuffer.append(space4 + "配置项（"+ key + "）未找到").append(System.lineSeparator());
			throw new Exception("文件"+ filePath + "中，不存在"+ key);
		}
		String propValue = prop[1];
		//replace value
		if(!propValue.equals(replaceValue)) {
			if(key.equals("mybatis.password")) {
				logStringBuffer.append(space4 + "配置项（" + key + "） '" + propValue + "' --> '******'").append(System.lineSeparator());
			}else {
				logStringBuffer.append(space4 + "配置项（" + key + "） '" + propValue + "' --> '" + replaceValue + "'").append(System.lineSeparator());
			}
			//替换文件内容
			String commands[] = null;
			//1.修改后的内容存入临时文件
			try {
				
				//sed 1,20s/old/new/g filePath>filePath.tmp
				commands = new String[] { "sed '"+rnum+"s/" + (key + splitStr + propValue).replace("/", "\\/") + "/"
						+ (key + splitStr + replaceValue).replace("/", "\\/").replace("&", "\\&") + "/g' " + filePath + ">"
						+ filePath + ".replace" };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage()).append(System.lineSeparator());
				throw new Exception("配置项（" + key + "）替换失败");
			}
			//2.将临时文件内容存入原文件
			try {
				commands = new String[] { "mv " + filePath + ".replace" + " " + filePath };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append("配置项（" + key + "）内容写入失败").append(System.lineSeparator());
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError()).append(System.lineSeparator());
				logStringBuffer.append(StringUtil.stringifyException(e));
				throw new Exception("配置项（" + key + "）内容写入失败");
			}
		}
		return true;
	}
	
	public static boolean replaceFileContentAscii(ClientSession session, Map<String, Object> paraMap, StringBuffer logStringBuffer, String filePath, String key, String splitStr, String nValue, String nValueAscii)
			throws Exception {
		/*if (null == oValue || oValue.equals("")) {
			logStringBuffer.append("替换" + filePath + ": karaf.properties中未配置" + key + "值" + "\r\n");
			throw new Exception("替换" + filePath + ": karaf.properties中未配置" + key + "值");
		}*/
		if (null == nValue || nValue.equals("")) {
			logStringBuffer.append("替换" + filePath + ": Server未设置" + key + "值" + "\r\n");
			throw new Exception("替换" + filePath + ": Server未设置" + key + "值");
		}
		String commands[] = null;
		//判断文件是否包含指定内容"key+splitStr+oValue"
		String rowNoAndContent = getRowNoAndContent(session, paraMap, logStringBuffer, filePath, key+splitStr);//findContent = key+splitStr+oValue = redis.ip
		int needRepRowNum = Integer.parseInt(rowNoAndContent.split(":")[0]);
		String rowContent = rowNoAndContent.replace(needRepRowNum+":", "").replace("\r", "");
		if(rowContent.split(splitStr).length != 2) {
			logStringBuffer.append(space4 + "不存在"+ key + "'\r\n");
			throw new Exception("文件"+ filePath + "中，不存在"+ key);
		}
		String oValueAscii = rowContent.split(splitStr)[1];
		String oValue = StringUtil.asciiToString(oValueAscii);
		if(!oValue.equals(nValue)) {
			logStringBuffer.append(space4 + key + " '" + oValue + "' 改为 '" + nValue + "'\r\n");
			//替换文件内容
			//1.修改后的内容存入临时文件
			try {
				//sed 1,20s/old/new/g filePath>filePath.tmp
				commands = new String[] { "sed '"+needRepRowNum+"s/" + (key + splitStr + oValueAscii.replace("\\", "\\\\")).replace("/", "\\/") + "/"
						+ (key + splitStr + nValueAscii.replace("\\", "\\\\")).replace("/", "\\/").replace("&", "\\&") + "/g' " + filePath + ">"
						+ filePath + ".tmp" };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("写入临时文件失败[" + filePath + " " + key + ": " + oValue + "]");
			}
			//2.将临时文件内容存入原文件
			try {
				commands = new String[] { "mv " + filePath + ".tmp" + " " + filePath };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("修改文件失败[" + filePath + " " + key + ": " + oValue + "]");
			}
			//logStringBuffer.append(SshClientUtils.getLogFileRowStartInfo(true) + "successed." + "\r\n");
		}
		return true;
	}

	//文件中key对应的value值有多行
	public static boolean replaceFileContent2(ClientSession session, Map<String, Object> paraMap,
			StringBuffer logStringBuffer, String filePath, String key, String splitStr, String oValue, String nValue)
			throws Exception {
		if (null == oValue || oValue.equals("")) {
			logStringBuffer.append("替换" + filePath + ": karaf.properties中未配置" + key + "值" + "\r\n");
			throw new Exception("替换" + filePath + ": karaf.properties中未配置" + key + "值");
		}
		if (null == nValue || nValue.equals("")) {
			logStringBuffer.append("替换" + filePath + ": Server未设置" + key + "值" + "\r\n");
			throw new Exception("替换" + filePath + ": Server未设置" + key + "值");
		}
		if (!oValue.equals(nValue)) {
			String commands[] = null;
			String extcResult = "";
			//判断文件中key值是否包含指定字符"\"
			try {
				//查文件内容所在行号
				commands = new String[] { "grep '" + key + "' " + filePath };
				extcResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
				if (extcResult.indexOf("\\") == -1) {//文件中key值不包含"\"，按key与多个值在同一行上处理
					return replaceFileContent(session, paraMap, logStringBuffer, filePath, key, splitStr, nValue);
				}
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception(filePath + ": 不包含'" + key + "'");
			}
			logStringBuffer.append(space4 + key + " '" + oValue + "' 改为 '" + nValue + "'\r\n");
			//判断文件是否包含指定内容"key+splitStr"
			int startRow = getContentRow(session, paraMap, logStringBuffer, filePath, key + splitStr);//findContent = key+splitStr = redis.cluster.nodes \
			int endRow = startRow;
			String oValueArr[] = oValue.split(",");
			for (String fOValue : oValueArr) {
				int tRow = getContentRow(session, paraMap, logStringBuffer, filePath, fOValue);//findContent = 127.0.0.1:6379
				if (endRow < tRow) {
					endRow = tRow;
				}
			}
			//新值
			String tNValue = "";
			String nValueArr[] = nValue.split(",");
			for (int i = 0; i < nValueArr.length; i++) {
				if (i == nValueArr.length - 1) {
					tNValue += "		" + nValueArr[i]; //空格:两个tab
				} else {
					tNValue += "		" + nValueArr[i] + ",\\\\" + "\\\n"; //空格:两个tab
				}
			}
			//System.out.println(tNValue);
			//替换文件内容
			//1.修改后的内容存入临时文件
			try {
				//sed '1,3c dd' filePath>filePath.tmp    1到3行内容替换为dd
				commands = new String[] { "sed '" + (startRow + 1) + "," + endRow + "c\\ " + tNValue + "' " + filePath
						+ ">" + filePath + ".tmp" };
				//sed 1,20s/old/new/g filePath>filePath.tmp
				//commands = new String[]{"sed 's/"+tOValue+"/"+tNValue+"/g' "+filePath+">"+filePath+".tmp"};
				//commands = new String[]{"sed 's/"+"127.0.0.1:6379,\\\\"+"/"+tNValue+"/g' "+filePath+">"+filePath+".tmp"};
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("写入临时文件失败[" + filePath + " " + key + ": " + oValue + "]");
			}
			//2.将临时文件内容存入原文件
			try {
				commands = new String[] { "mv " + filePath + ".tmp" + " " + filePath };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("修改文件失败[" + filePath + " " + key + ": " + oValue + "]");
			}
			//logStringBuffer.append(SshClientUtils.getLogFileRowStartInfo(true) + "successed." + "\r\n");
		}
		return true;
	}

	//替换文件中指定内容行
	public static boolean replaceFileContent3(ClientSession session, Map<String, Object> paraMap,
			StringBuffer logStringBuffer, String filePath, String key, String oValue, String nValue) throws Exception {
		if (null == oValue || oValue.equals("")) {
			logStringBuffer.append("替换" + filePath + ": karaf.properties中未配置" + key + "值" + "\r\n");
			throw new Exception("替换" + filePath + ": karaf.properties中未配置" + key + "值");
		}
		if (null == nValue || nValue.equals("")) {
			logStringBuffer.append("替换" + filePath + ": Server未设置" + key + "值" + "\r\n");
			throw new Exception("替换" + filePath + ": Server未设置" + key + "值");
		}
		if (!oValue.equals(nValue)) {
			String commands[] = null;
			//判断文件是否包含指定内容"key+splitStr"
			String tArr[] = getContentRows(session, paraMap, logStringBuffer, filePath, oValue);
			/*
			 tArr值格式：
			 28:             mvn:com.bocsoft/wws.features/2.0.0/xml/features, \
			 60:             wws.agent
			 */
			int tRow = -1;
			String tContent = "";
			String tBlankSpace = "";
			for (int i = 0; i < tArr.length; i++) {
				if (tArr[i].indexOf(":") != -1) {
					tContent = tArr[i].split(":")[1];
					if (null != tContent && tContent.trim().equals(oValue)) {
						tRow = Integer.parseInt(tArr[i].split(":")[0]);
						for (int j = 0; j < tContent.length(); j++) {
							if (tContent.charAt(j) != ' ' && tContent.charAt(j) != '	') {
								break;
							}
							tBlankSpace += tContent.charAt(j);
						}
						break;
					}
				}
			}
			if (tRow == -1) {
				logStringBuffer.append(filePath + ": 不包含'" + oValue + "'" + "\r\n");
				throw new Exception(filePath + ": 不包含'" + oValue + "'");
			}
			logStringBuffer.append(space4 + "第" + tRow + "行 替换为 '" + tBlankSpace + nValue + "'\r\n");
			//替换文件内容
			//1.修改后的内容存入临时文件
			try {
				//sed '3c dd' filePath>filePath.tmp    第3行内容替换为dd
				commands = new String[] {
						"sed '" + tRow + "c\\ " + tBlankSpace + nValue + "' " + filePath + ">" + filePath + ".tmp" };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("写入临时文件失败[" + filePath + " " + key + ": " + oValue + "]");
			}
			//2.将临时文件内容存入原文件
			try {
				commands = new String[] { "mv " + filePath + ".tmp" + " " + filePath };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("修改文件失败[" + filePath + " " + key + ": " + oValue + "]");
			}
			//logStringBuffer.append(SshClientUtils.getLogFileRowStartInfo(true) + "successed." + "\r\n");
		}
		return true;
	}

	//文件中key对应的value值有单行
	public static boolean appendFileContent(ClientSession session, Map<String, Object> paraMap,
			StringBuffer logStringBuffer, String filePath, List<String> fileContentList) throws Exception {
		if (null != fileContentList && fileContentList.size() > 0) {
			String tFileContent = "";
			String tfc = "";
			for (String fFileContent : fileContentList) {
				tFileContent = tFileContent + fFileContent + "\\\n";
				tfc = tfc + fFileContent + " | ";
			}
			tFileContent = tFileContent.substring(0, tFileContent.length() - 2);
			tfc = tfc.substring(0, tfc.length() - 3);
			logStringBuffer.append(space4 + "追加内容 '" + tfc + "'" + "\r\n");
			String commands[] = null;
			//追加文件内容
			//1.修改后的内容存入临时文件
			try {
				//sed 1,20s/old/new/g filePath>filePath.tmp
				commands = new String[] { "sed '$a " + tFileContent + "' " + filePath + ">" + filePath + ".tmp" };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("写入临时文件失败[" + filePath + " " + tfc + "]");
			}
			//2.将临时文件内容存入原文件
			try {
				commands = new String[] { "mv " + filePath + ".tmp" + " " + filePath };
				executeCommands(session, commands, paraMap, logStringBuffer, false);
			} catch (Exception e) {
				logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
				throw new Exception("修改文件失败[" + filePath + " " + tfc + "]");
			}
			//logStringBuffer.append(SshClientUtils.getLogFileRowStartInfo(true) + "successed." + "\r\n");
		}
		return true;
	}

	public static int getContentRow(ClientSession session, Map<String, Object> paraMap, StringBuffer logStringBuffer,
			String filePath, String findContent) throws Exception {
		String commands[] = null;
		try {
			//查文件内容所在行号 
			commands = new String[] { "cat " + filePath + " | grep -n '" + findContent + "'" };
			String extcResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
			String tArr[] = extcResult.split("\n"); //tArr[0]值格式： 6:redis.ip 127.0.0.1
			if (tArr.length > 0) {
				List<String> rowList = new ArrayList<String>();
				for(int z=0;z<tArr.length;z++) {
					if(tArr[z].split(":")[1].startsWith("#")) {
						continue;
					}
					rowList.add(tArr[z].split(":")[0]);
				}
				if(rowList.size() == 0) {
					throw new Exception(filePath + ": 不包含'" + findContent + "'");
				}else if(rowList.size() > 1) {
					logStringBuffer.append(filePath + ": 多次包含'" + findContent + "'" + "\r\n");
					throw new Exception(filePath + ": 多次包含'" + findContent + "'");
				}else{
					return Integer.parseInt(rowList.get(0));
				}
			}else{
				throw new Exception(filePath + ": 不包含'" + findContent + "'");
			}
		} catch (Exception e) {
			logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
			throw new Exception(filePath + ": 不包含'" + findContent + "'");
		}
	}
	
	//获取行号和行内容(获取不到抛异常)
	public static String getRowNoAndContent(ClientSession session, Map<String, Object> paraMap, StringBuffer logStringBuffer,
			String filePath, String findContent) throws Exception {
		String commands[] = null;
		try {
			//查文件内容所在行号 
			commands = new String[] { "cat " + filePath + " | grep -n '" + findContent + "'" };
			String extcResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
			String tArr[] = extcResult.split("\n"); //tArr[0]值格式： 6:redis.ip 127.0.0.1
			if (tArr.length > 0) {
				List<String> rowList = new ArrayList<String>();
				for(int z=0;z<tArr.length;z++) {
					if(tArr[z].split(":")[1].startsWith("#")) {
						continue;
					}
					rowList.add(tArr[z]);
				}
				if(rowList.size() == 0) {
					throw new Exception(filePath + ": 不包含'" + findContent + "'");
				}else if(rowList.size() > 1) {
					logStringBuffer.append(filePath + ": 多次包含'" + findContent + "'" + "\r\n");
					throw new Exception(filePath + ": 多次包含'" + findContent + "'");
				}else{
					return rowList.get(0);
				}
			}else{
				throw new Exception(filePath + ": 不包含'" + findContent + "'");
			}
		} catch (Exception e) {
			logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
			throw new Exception(filePath + ": 不包含'" + findContent + "'");
		}
	}
	
	//获取行号和行内容(获取不到返回空)     注释属性(annotationFileContent)方法专用
	public static String getRowNoAndContent2(ClientSession session, Map<String, Object> paraMap, StringBuffer logStringBuffer,
			String filePath, String findContent) throws Exception {
		String commands[] = null;
		int throwFlag = 0;
		try {
			//查文件内容所在行号 
			commands = new String[] { "cat " + filePath + " | grep -n '" + findContent + "'" };
			String extcResult = executeCommands(session, commands, paraMap); //执行commands(查询文件中内容，查找不到会报错，不写日志内容)
			String tArr[] = extcResult.split("\n"); //tArr[0]值格式： 6:redis.ip 127.0.0.1
			if (tArr.length > 0) {
				List<String> rowList = new ArrayList<String>();
				for(int z=0;z<tArr.length;z++) {
					if(tArr[z].split(":")[1].startsWith("#")) {
						continue;
					}
					rowList.add(tArr[z]);
				}
				if(rowList.size() == 0) {
					return null;
				}else if(rowList.size() > 1) {
					throwFlag = 1;
					logStringBuffer.append(filePath + ": 多次包含'" + findContent + "'" + "\r\n");
					throw new Exception(filePath + ": 多次包含'" + findContent + "'");
				}else{
					return rowList.get(0);
				}
			}else{
				return null;
			}
		} catch (Exception e) {
			if(throwFlag == 1) {
				throw e;
			}
			return null;
		}
	}

	public static String[] getContentRows(ClientSession session, Map<String, Object> paraMap,
			StringBuffer logStringBuffer, String filePath, String findContent) throws Exception {
		String commands[] = null;
		try {
			//查文件内容所在行号 
			commands = new String[] { "cat " + filePath + " | grep -n '" + findContent + "'" };
			String extcResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
			String tArr[] = extcResult.split("\n"); //tArr[0]值格式： 6:redis.ip 127.0.0.1
			if (tArr.length > 0) {
				List<String> rowList = new ArrayList<String>();
				for(int z=0;z<tArr.length;z++) {
					if(tArr[z].split(":")[1].startsWith("#")) {
						continue;
					}
					rowList.add(tArr[z]);
				}
				if(rowList.size() == 0) {
					throw new Exception(filePath + ": 不包含'" + findContent + "'");
				}else{
					String rArr[] = new String[rowList.size()];
					rowList.toArray(rArr);
					return rArr;
				}
			}else {
				throw new Exception(filePath + ": 不包含'" + findContent + "'");
			}
			//return extcResult.split("\n"); //tArr[0]值格式： 6:redis.ip 127.0.0.1
		} catch (Exception e) {
			logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + e.getMessage() + "\r\n");
			throw new Exception(filePath + ": 不包含'" + findContent + "'");
		}
	}
	
	//执行commands
	public static String executeCommands(ClientSession session, String commands[], Map<String, Object> paraMap, StringBuffer logStringBuffer, boolean printLog) throws Exception {
		String extcResult = "";
		//commands = new String[3];
		//commands[0] = "cd /dcds/ssh;gzip -d apache-maven-3.3.3-bin.tar.gz;tar -xf apache-maven-3.3.3-bin.tar";
		//commands[1] = "export JAVA_HOME=/usr/java7_64/jre;export MAVEN_HOME=/dcds/ssh/apache-maven-3.3.3;PATH=$PATH:$MAVEN_HOME/bin/;mvn -version";
		//commands[2] = "mvn -version";
		String charsetName = "UTF-8";
		if (null != paraMap && null != paraMap.get("charsetName")) {
			charsetName = (String) paraMap.get("charsetName");
		}
		for (int i = 0; i < commands.length; i++) {
			if (null != commands[i] && !commands[i].equals("")) {
				//执行command
				ByteArrayOutputStream stderr = null;
				try {
					stderr = new ByteArrayOutputStream();
					if (printLog) {
						logStringBuffer.append(space4 + "execute commands[" + i + "]: " + commands[i] + "\r\n");
					}
					extcResult = session.executeRemoteCommand(commands[i], stderr, Charset.forName(charsetName));
					if (null == extcResult || extcResult.trim().equals("")) {
						if (commands[i].indexOf(".sh ") != -1) {//执行.sh脚本 不需回车换行
							if (printLog) {
								logStringBuffer.append(space4 + "successed.");
							}
						} else {
							if (printLog) {
								logStringBuffer.append(space4 + "successed." + "\r\n");
							}
						}
					} else {
						if (commands[i].indexOf(".sh ") != -1) {//执行.sh脚本 不需回车换行
							if (printLog) {
								logStringBuffer.append(space4 + "successed. " + extcResult);
							}
						} else {
							if (printLog) {
								logStringBuffer.append(space4 + "successed. " + extcResult + "\r\n");
							}
						}
					}
				} catch (Exception tExc) {
					byte[] error = stderr.toByteArray();
					String errorMessage = new String(error, Charset.forName(charsetName));
					if (null == errorMessage || errorMessage.equals("")) {
						if (commands[i].indexOf(".sh ") != -1) {//执行.sh脚本 不需回车换行
							logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + tExc.getMessage());
						} else {
							logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + tExc.getMessage() + "\r\n");
						}
						throw new Exception("execute commands[" + i + "]: " + commands[i] + " fail. " + "</br>" + tExc.getMessage());
					} else {
						if (commands[i].indexOf(".sh ") != -1) {//执行.sh脚本 不需回车换行
							logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + errorMessage);
						} else {
							logStringBuffer.append(SshClientUtils.getLogFileRowStartError() + errorMessage + "\r\n");
						}
						throw new Exception("execute commands[" + i + "]: " + commands[i] + " fail." + "</br>" + errorMessage);
					}
				}
			} else {
				logStringBuffer.append(space4 + "not execute null " + commands[i] + "." + "\r\n");
			}
		}
		return extcResult;
	}
	
	//执行commands(查询文件中内容，查找不到会报错，不写日志内容)
	public static String executeCommands(ClientSession session, String commands[], Map<String, Object> paraMap) throws Exception {
		String extcResult = "";
		String charsetName = "UTF-8";
		if (null != paraMap && null != paraMap.get("charsetName")) {
			charsetName = (String) paraMap.get("charsetName");
		}
		for (int i = 0; i < commands.length; i++) {
			if (null != commands[i] && !commands[i].equals("")) {
				//执行command
				ByteArrayOutputStream stderr = null;
				try {
					stderr = new ByteArrayOutputStream();
					extcResult = session.executeRemoteCommand(commands[i], stderr, Charset.forName(charsetName));
				} catch (Exception tExc) {
					byte[] error = stderr.toByteArray();
					String errorMessage = new String(error, Charset.forName(charsetName));
					if (null == errorMessage || errorMessage.equals("")) {
						throw new Exception("execute commands[" + i + "]: " + commands[i] + " fail. " + "</br>" + tExc.getMessage());
					} else {
						throw new Exception("execute commands[" + i + "]: " + commands[i] + " fail." + "</br>" + errorMessage);
					}
				}
			}
		}
		return extcResult;
	}

	public static String start(Map<String,Object> paraMap) throws Exception{
		SshClient client = null;
		ClientSession session = null;
		Server server = (Server)paraMap.get("Server");
		String userName = server.getOsSshUser();
		String password = server.getOsSshPswd();
		String ip = server.getIpAddr();
		int port = server.getOsSshPort();
		StringBuffer logSb = new StringBuffer();

		// 获取到SH认证方式、密钥路径、密钥密码
		String sshAuth = (String) paraMap.get("sshAuth");
		String privateKeyPath = (String)paraMap.get("privateKeyPath");
		String passphrase = (String)paraMap.get("passphrase");

		try{
			client = SshClient.setUpDefaultClient();
			client.start();
			ConnectFuture connectFuture = client.connect(userName, ip, port);
			connectFuture.await();
			if(!connectFuture.isConnected()){
				throw new Exception(userName+"@"+ip+":"+port+" connect fail.");
			}
			session = connectFuture.getSession();

			// SSH服务认证改造，支持公钥认证（免密）和密码认证两种方式，通过配置文件配置选择认证方式，默认公钥认证
			String authStr = "";
			if(Ssh_Default_OsSshAuth.equals(sshAuth)){
				authStr = " 【公钥认证】";
				KeyPair keyPair = loadPrivateKey(privateKeyPath,passphrase);
				if(keyPair == null ){
					throw new Exception("用户名：" + userName + " 无法加载私钥,验证失败.");
				}
				session.addPublicKeyIdentity(keyPair);
			}else {
				authStr = " 【密码认证】";
				session.addPasswordIdentity(password);
			}

			AuthFuture authTrue = session.auth();
			authTrue.await();
			if (!authTrue.isSuccess()) {
				throw new Exception("用户名：" + userName   + authStr +  " 验证失败.");
			}

			//通过执行Shell命令来创建目录    /hadoop/cirrus
			//Map<String,String> karafMap = (Map<String,String>)paraMap.get("karafMap");
			//if(null == karafMap.get("installFileName")){
				//throw new Exception("karaf.properties未配置属性installFileName");
			//}
			boolean karafRunning = karafRunning(session,paraMap,logSb,server.getHomeDir());
			if(!karafRunning){
				//String installFileName = (String)karafMap.get("installFileName");
				//String karafHome = server.getHomeDir()+"/"+installFileName;//值：/home/cirrus/test/wws-2.0.0
				String karafHome = server.getHomeDir();
				String commands[] = new String[]{karafHome+"/bin/start"};
				executeCommands(session,commands,paraMap,logSb,false);
			}
		}catch(Exception e){
			throw e;
		}finally{
			log.info(logSb.toString());
			if(null != session){
				session.close();
			}
			if(null != client){
				client.stop();
			}
		}
		return null;
	}

	public static String stop(Map<String,Object> paraMap) throws Exception{
		SshClient client = null;
		ClientSession session = null;
		Server server = (Server)paraMap.get("Server");
		String userName = server.getOsSshUser();
		String password = server.getOsSshPswd();
		String ip = server.getIpAddr();
		int port = server.getOsSshPort();
		StringBuffer logSb = new StringBuffer();

		// 获取到SH认证方式、密钥路径、密钥密码
		String sshAuth = (String) paraMap.get("sshAuth");
		String privateKeyPath = (String)paraMap.get("privateKeyPath");
		String passphrase = (String)paraMap.get("passphrase");

		try{
			client = SshClient.setUpDefaultClient();
			client.start();
			ConnectFuture connectFuture = client.connect(userName, ip, port);
			connectFuture.await();
			if(!connectFuture.isConnected()){
				throw new Exception(userName+"@"+ip+":"+port+" connect fail.");
			}
			session = connectFuture.getSession();

			// SSH服务认证改造，支持公钥认证（免密）和密码认证两种方式，通过配置文件配置选择认证方式，默认公钥认证
			String authStr = "";
			if(Ssh_Default_OsSshAuth.equals(sshAuth)){
				authStr = " 【公钥认证】";
				KeyPair keyPair = loadPrivateKey(privateKeyPath,passphrase);
				if(keyPair == null ){
					throw new Exception("用户名：" + userName + " 无法加载私钥,验证失败.");
				}
				session.addPublicKeyIdentity(keyPair);
			}else {
				authStr = " 【密码认证】";
				session.addPasswordIdentity(password);
			}
			AuthFuture authTrue = session.auth();
			authTrue.await();
			if (!authTrue.isSuccess()) {
				throw new Exception("用户名：" + userName   + authStr +  " 验证失败.");
			}

			//通过执行Shell命令来创建目录    /hadoop/cirrus
			//Map<String,String> karafMap = (Map<String,String>)paraMap.get("karafMap");
			//if(null == karafMap.get("installFileName")){
				//throw new Exception("karaf.properties未配置属性installFileName");
			//}
			boolean karafRunning = karafRunning(session,paraMap,logSb,server.getHomeDir());
			if(karafRunning){
				//String installFileName = (String)karafMap.get("installFileName");
				//String karafHome = server.getHomeDir()+"/"+installFileName;//值：/home/cirrus/test/wws-2.0.0
				String karafHome = server.getHomeDir();
				String commands[] = new String[]{karafHome+"/bin/stop"};
				executeCommands(session,commands,paraMap,logSb,false);
			}
		}catch(Exception e){
			throw e;
		}finally{
			log.info(logSb.toString());
			if(null != session){
				session.close();
			}
			if(null != client){
				client.stop();
			}
		}
		return null;
	}

	public static String getJavaHome(ClientSession session, Map<String, Object> paraMap, StringBuffer logStringBuffer) {
		String javaHome = null;
		String commands[] = null;
		try {
			commands = new String[] { "cat /etc/profile | grep JAVA_HOME=" };
			String tResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
			javaHome = tResult.split("\n")[0];
		} catch (Exception e33) {
		}
		if (null == javaHome) {
			try {
				commands = new String[] { "cat ~/.bash_profile | grep JAVA_HOME=" };
				String tResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
				javaHome = tResult.split("\n")[0];
			} catch (Exception e22) {
			}
			try {
				if (null == javaHome) {
					commands = new String[] { "cat ~/.profile | grep JAVA_HOME=" };
					String tResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
					javaHome = tResult.split("\n")[0];
				}
			} catch (Exception e11) {
			}
		}
		return javaHome;
	}

	public static boolean karafRunning(ClientSession session, Map<String, Object> paraMap, StringBuffer logStringBuffer,
			String agentHome) {
		String extcResult = null;
		boolean running = false;
		try {
			String commands[] = new String[] { "ps -ef | grep "+agentHome+"/bin/karaf" };
			extcResult = executeCommands(session, commands, paraMap, logStringBuffer, false);
			String retArr[] = extcResult.split("\n");
			for (String ret : retArr) {
				if (ret.indexOf(agentHome+"/bin/karaf server") != -1) {
					running = true;
					break;
				}
			}
		} catch (Exception e) {
		}
		return running;
	}

	public static void writeToFileWriter(FileWriter pFileWriter, String pWriteLineString) throws Exception {
		pFileWriter.write(pWriteLineString);
		pFileWriter.write("\n");
	}

	//pGenShPath="projectPath+"/upload/agent/1.0"
	public static FileWriter genInstallLog(String pGenShPath, StringBuffer pLogStringBuffer) throws Exception {
		File pLogFile = new File(pGenShPath);
		if (!pLogFile.exists()) {
			pLogFile.mkdirs();
		}
		pLogFile = new File(pGenShPath + "/install.log");
		FileWriter tFileWriter = null;
		try {
			tFileWriter = new FileWriter(pLogFile);
			writeToFileWriter(tFileWriter, pLogStringBuffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("generate install.log fail.");
		}
		return tFileWriter;
	}

	/**
	 *  加载密钥对-私钥
	 * @param privateKeyPath 私钥
	 * @param password 加密私钥的密码
	 * @return java识别的密钥
	 * @throws Exception
	 */
	public static KeyPair loadPrivateKey(String privateKeyPath, String password ) throws Exception{
		File privateKeyFile = new File(privateKeyPath);
		PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
		Object o = pemParser.readObject();
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		if(o instanceof PEMEncryptedKeyPair){
			if (StringUtil.isNullOrEmpty(password)){
				throw new IllegalStateException("需要私钥密码");
			}
			PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
			PEMKeyPair decryptKeyPair = ((PEMEncryptedKeyPair)o).decryptKeyPair(decryptorProvider);
			return converter.getKeyPair(decryptKeyPair);
		}
		if(o instanceof PEMKeyPair){
			return converter.getKeyPair((PEMKeyPair) o);
		}

		if(o instanceof KeyPair){
			return (KeyPair)o;
		}
		throw  new IllegalStateException("不支持的密钥格式");
	}
}
