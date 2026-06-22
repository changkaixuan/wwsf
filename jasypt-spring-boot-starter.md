spring-boot/datasource密码加密（application.properties/spring.datasource.password）

1 pom.xml文件需加内容：
  	<dependency>
		<groupId>com.github.ulisesbocchio</groupId>
		<artifactId>jasypt-spring-boot-starter</artifactId>
		<version>2.1.1</version>
	</dependency>

2   对jasypt默认加密方式（PBEWithMD5AndDES）必输参数password进行加密
	加密方法: com.bocsoft.wwse.webconsole.DesEncrypt.encrypt
	解密方法: com.bocsoft.wwse.webconsole.DesEncrypt.decrypt
	password明文：wwsedbpwd; password密文：DCEA49D1876805B18F736C8E12F8BFBA

3 cmd(cd D:\java\apache-maven-3.3.3\maven\Repository\org\jasypt\jasypt\1.9.0)
	加密命令（密码明文db_dcds2）：
  		java -cp jasypt-1.9.0.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=db_dcds2 password=DCEA49D1876805B18F736C8E12F8BFBA algorithm=PBEWithMD5AndDES
	解密命令（密码密文uRTLtD7mgv6zeZNMnJc9Zb7zPd8bOfYC）：
  		java -cp jasypt-1.9.0.jar org.jasypt.intf.cli.JasyptPBEStringDecryptionCLI input=uRTLtD7mgv6zeZNMnJc9Zb7zPd8bOfYC password=DCEA49D1876805B18F736C8E12F8BFBA algorithm=PBEWithMD5AndDES

4 application.properties文件需修改两处内容：
	spring.datasource.password ENC(uRTLtD7mgv6zeZNMnJc9Zb7zPd8bOfYC)
	jasypt.encryptor.password=DCEA49D1876805B18F736C8E12F8BFBA

	{"pool.maxTotal":"20","pool.maxIdle":"10","pool.maxWaitMillis":"5000","pool.testOnReturn":"true"}
	{"c3p0.maxPoolSize":"20","c3p0.minPoolSize":"5"}