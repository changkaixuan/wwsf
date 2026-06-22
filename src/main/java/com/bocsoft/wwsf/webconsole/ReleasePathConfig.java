package com.bocsoft.wwsf.webconsole;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ReleasePathConfig  implements WebMvcConfigurer{

	@Value("${file.staticAccessPath}")
	private String staticAccessPath;
	@Value("${file.releasePath}")
	private String releasePath;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(staticAccessPath).addResourceLocations("file:" + releasePath);
		WebMvcConfigurer.super.addResourceHandlers(registry);
	}

}
