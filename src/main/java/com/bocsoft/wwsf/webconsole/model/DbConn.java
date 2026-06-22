package com.bocsoft.wwsf.webconsole.model;

import com.bocsoft.wwsf.webconsole.cryptor.EncryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbConn {

	private static final Logger log = LoggerFactory.getLogger(DbConn.class);

	private static final String ENCRYPTED_PROPERTY_PREFIX = "ENC(";
	private static final String ENCRYPTED_PROPERTY_SUFFIX = ")";
	//数据库连接
	@Value("${spring.datasource.driver-class-name}")
	private String dbDriver;
	
	@Value("${spring.datasource.url}")
	private String dbUrl;
	
	@Value("${spring.datasource.username}")
	private String dbUserName;
	
	@Value("${spring.datasource.password}")
	private String dbPassword;
	
	@Value("${jasypt.encryptor.password}")
	private String jasyptEncryptorPassword;

	@Autowired
	EncryptService encryptService;

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getJasyptEncryptorPassword() {
		return jasyptEncryptorPassword;
	}

	public void setJasyptEncryptorPassword(String jasyptEncryptorPassword) {
		this.jasyptEncryptorPassword = jasyptEncryptorPassword;
	}

	public String getENCDbPassword() throws Exception{
		String dbPassword = getDbPassword();
		return ENCRYPTED_PROPERTY_PREFIX
				+ encryptService.encryptInfo(dbPassword)
				+ ENCRYPTED_PROPERTY_SUFFIX;
	}
	
}
