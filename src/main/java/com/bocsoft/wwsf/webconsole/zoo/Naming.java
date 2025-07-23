package com.bocsoft.wwsf.webconsole.zoo;

import java.text.MessageFormat;

public class Naming {

	static String node_plugins_root = "/plugins";
	static String node_plugin = "/plugins/{0}";
	static String node_plugin_agent = node_plugin + "/{1}::{2}";
		
	static String pl_node_root = "/pl_nodes";
	static String pl_node = pl_node_root + "/{0}::{1}"; // {1} -- product::nodeId
	static String pl_master_root = "/pl_master";
	static String pl_master = pl_master_root + "/{0}::{1}";

	static String lock_root = "/lock";
	static String lock_setup = lock_root + "/setup/{0}"; // {1} -- product
	static String lock_spof = lock_root + "/spof/{0}"; // {1} -- product
	static String lock_master = lock_root + "/master";
	static String lock_standby = lock_root + "/standby";
	static String lock_job_root = lock_root + "/job";
	static String lock_job = lock_job_root + "/{0}";
	static String lock_cron_root = lock_root + "/cron";
	static String lock_cron = lock_cron_root + "/{0}";
	
	
	/*****************************************************************************************/
	
	public static String getNodePlugin(String group, String nodeId) {
		return MessageFormat.format(node_plugin, group, nodeId);
	}

	public static String getPluginsRoot() {
		return node_plugins_root;
	}
	
	public static String getPlugin(String plugin) {
		return MessageFormat.format(node_plugin, plugin);
	}
	
	public static String getPluginNode(String plugin, String product, String agent) {
		return MessageFormat.format(node_plugin_agent, plugin, product, agent);
	}

	public static String getPlNodeRoot() {
		return pl_node_root;
	}

	public static String getPlNode(String product, String nodeId) {
		return MessageFormat.format(pl_node, product, nodeId);
	}
	
	public static String getPlMasterRoot() {
		return pl_master_root;
	}

	public static String getPlMaster(String product, String nodeId) {
		return MessageFormat.format(pl_master, product, nodeId);
	}

	public static String getLockSetup(String product) {
		return MessageFormat.format(lock_setup, product);
	}
	
	public static String getLockCron(String cronId) {
		return MessageFormat.format(lock_cron, cronId);
	}
	
	public static String getLockCron() {
		return lock_cron_root;
	}
	
	public static String getLockSpof(String product) {
		return MessageFormat.format(lock_spof, product);
	}

	public static String getLockMaster() {
		return lock_master;
	}

	public static String getLockStandby() {
		return lock_standby;
	}
	
	public static String getLockJob() {
		return lock_job_root;
	}
	
	public static String getLockJob(long jobInsId) {
		return MessageFormat.format(lock_job, String.valueOf(jobInsId));
	}

}
