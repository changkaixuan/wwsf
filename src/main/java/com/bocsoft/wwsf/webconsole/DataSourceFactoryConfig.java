package com.bocsoft.wwsf.webconsole;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="wws.ds")
public class DataSourceFactoryConfig {
	
	private Map<String,String> factories = new HashMap<>();
	
	public Map<String, String> getFactories() {
		return factories;
	}
	
	public void setFactories(Map<String, String> factories) {
		this.factories = factories;
	}
    
    
	
}
