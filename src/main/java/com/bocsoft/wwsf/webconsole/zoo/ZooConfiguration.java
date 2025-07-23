package com.bocsoft.wwsf.webconsole.zoo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bocsoft.wwsf.webconsole.StringUtil;

@Configuration
public class ZooConfiguration {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Value("${zoo.connect.string}")
    private String connectUrl;
    @Value("${zoo.session.timeout}")
    private int sessionTimeout;
    @Value("${zoo.connect.timeout}")
    private int connectTimeout;
    @Value("${zoo.namespace}")
    private String nameSpace;
    @Value("${zoo.acl.string:}") //默认值为"". 不设默认值,application.properties中不配置zoo.acl.string,会报错.
    private String aclString;
    
	@Bean
	public CuratorFramework initCuratorFramework() {
		CuratorFramework curator = null;
		logger.info("--Configuration of CuratorFramework-------------------------------");
		logger.info("        nameSpace : {}", nameSpace);
		logger.info("    connectString : {}", connectUrl);
		logger.info(" connectTimeoutMs : {}", connectTimeout);
		logger.info(" sessionTimeoutMs : {}", sessionTimeout);
		
		if (StringUtil.hasText(aclString)) {
			String[] acls = aclString.split("/");
			Map<String,Integer> aclReal = new HashMap<String,Integer>(acls.length);
			String authorizationDigest = null;
			for (String acl : acls) {
				Integer perm = 1;
				String digest = acl;
				if (acl.indexOf(",") > 0) {
					digest = acl.substring(acl.indexOf(",") + 1);
					perm = Integer.parseInt(acl.split(",")[0]);
				}
				if (!aclReal.containsKey(digest)) aclReal.put(digest, perm);
				if (StringUtil.isNullOrEmpty(authorizationDigest)) authorizationDigest = digest;
			}
			ACLProvider aclProvider = new ACLProvider() {
				private List<ACL> acl = null;
				public List<ACL> getDefaultAcl() {
					if (acl == null) {
						ArrayList<ACL> dacl = ZooDefs.Ids.CREATOR_ALL_ACL;
						dacl.clear();
						try {
							for (Entry<String,Integer> aclEntry : aclReal.entrySet()) {
								dacl.add(new ACL(aclEntry.getValue(), 
										 new Id("digest", DigestAuthenticationProvider.generateDigest(aclEntry.getKey()))));
							}
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						this.acl = dacl;
					}
					return acl;
				}
				public List<ACL> getAclForPath(String path) {
					return acl;
				}
			};
			curator = CuratorFrameworkFactory.builder()
					.aclProvider(aclProvider)
					.connectString(connectUrl)
					.sessionTimeoutMs(sessionTimeout)
					.connectionTimeoutMs(connectTimeout)
					.retryPolicy(new RetryOneTime(2000))
					.namespace(nameSpace)
					.authorization("digest", authorizationDigest.getBytes())
					.build();
		} else {
			curator = CuratorFrameworkFactory.builder()
					.connectString(connectUrl)
					.sessionTimeoutMs(sessionTimeout)
					.connectionTimeoutMs(connectTimeout)
					.retryPolicy(new RetryOneTime(2000))
					.namespace(nameSpace)
					.build();
		}
		curator.start();
		logger.info("CuratorFramework Started");
		return curator;
	}
}
