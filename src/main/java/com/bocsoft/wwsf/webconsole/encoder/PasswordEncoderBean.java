package com.bocsoft.wwsf.webconsole.encoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.bocsoft.wwsf.webconsole.encoder.impl.MD5PasswordEncoderImpl;

@Configuration
public class PasswordEncoderBean {
	
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		PasswordEncoder passwordEncoder = new MD5PasswordEncoderImpl();
		return passwordEncoder;
	}

}
