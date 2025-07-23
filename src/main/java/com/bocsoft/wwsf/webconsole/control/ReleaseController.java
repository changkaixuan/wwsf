package com.bocsoft.wwsf.webconsole.control;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.model.ReleaseInfo;

@RestController
public class ReleaseController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${file.releasePath}")
	private String releaseDir;
	
	@GetMapping("/release/info")
	public RestResponse release() {
		RestResponse response = new RestResponse();
		try {
			File releaseDirFile = new File(releaseDir);
			Map<String,ReleaseInfo> releaseInfos = new TreeMap<String,ReleaseInfo>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					return - o1.compareTo(o2);
				}
			});
			if(releaseDirFile.isDirectory()) {
				File[] rfiles= releaseDirFile.listFiles();
				if(null != rfiles && rfiles.length > 0) {
					for(File rfile : rfiles) {
						String rfilename = rfile.getName();
						if(rfilename.startsWith("wwsf-release") && rfilename.endsWith(".html")) {
							String[] rla = rfilename.split("-");
							ReleaseInfo rinfo = new ReleaseInfo();
							rinfo.setAppName(rla[0]);
							rinfo.setVersion(rla[2]);
							rinfo.setTime(rla[3].substring(0, rla[3].lastIndexOf(".")));
							String batchFileName = rinfo.getAppName()+"-"+rinfo.getVersion()+"-"+rinfo.getTime()+".zip";
							File batchFile = new File(releaseDirFile, batchFileName);
							if (batchFile.exists()) {
								rinfo.setBatchZipUrl(batchFileName);
								rinfo.setBatchZipSize(StringUtil.formatSeparator(batchFile.length()));
							} else {
								rinfo.setBatchZipSize("0");
							}
							String docFileName = rinfo.getAppName()+"-doc-"+rinfo.getVersion()+"-"+rinfo.getTime()+".zip";
							File docFile = new File(releaseDirFile, docFileName);
							if (docFile.exists()) {
								rinfo.setDocUrl(docFileName);
								rinfo.setDocSize(StringUtil.formatSeparator(docFile.length()));
							} else {
								rinfo.setDocSize("0");
							}
							String consoleFileName = rinfo.getAppName()+"-console-"+rinfo.getVersion()+"-"+rinfo.getTime()+".zip";
							File consoleFile = new File(releaseDirFile, consoleFileName);
							if (consoleFile.exists()) {
								rinfo.setConsoleZipUrl(consoleFileName);
								rinfo.setConsoleZipSize(StringUtil.formatSeparator(consoleFile.length()));
							} else {
								rinfo.setConsoleZipSize("0");
							}
							rinfo.setTextUrl(rfilename);
							releaseInfos.put(rinfo.getTime() + "_" + rinfo.getAppName() + "_" + rinfo.getVersion(), rinfo);
						}
					}
				}
			}
			response.put("releases", releaseInfos.values());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("获取版本说明失败",e);
			response.setSuccess(false);
			response.setInfo("获取版本说明失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

}
