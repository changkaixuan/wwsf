package com.bocsoft.wwsf.webconsole;

import java.util.Properties;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataBaseIdProvider {

	@Bean
	public DatabaseIdProvider getDatabaseIdProvide() {
		DatabaseIdProvider databaseIdProvider =  new VendorDatabaseIdProvider();
		Properties properties = new Properties();
		properties.put("MySQL", "mysql");
		properties.put("Oracle", "oracle");
		properties.put("SQL Server", "sqlserver");
		properties.put("DB2", "DB2");
		databaseIdProvider.setProperties(properties);
		return databaseIdProvider;
	}
	
}
