package com.bocsoft.wwsf.webconsole.control;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.model.Plugin;
import com.bocsoft.wwsf.webconsole.service.PluginService;
import com.bocsoft.wwsf.webconsole.service.WwseService;

@RestController
public class PublicInterfaceController {

	@Autowired
	WwseService wwseService;
	@Autowired
	private PluginService pluginService;
	
	@GetMapping("/wwsapi/pluginInfo")
	public RestResponse selectPlugin(){
		RestResponse response = new RestResponse();
		List<Plugin> pluginList = null;
		try{
			pluginList = pluginService.getPluginDefinition();
			if(null != pluginList && pluginList.size()>0) {
				response.put("plugins", pluginList);
			}
			response.setSuccess(true);
		}catch(Exception e){
			response.setSuccess(false);
			response.setInfo("get plugins failed");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	
	/***************************************************************************************
	 * Job Open Interfaces
	 ******************************************************/
	
	@PostMapping("/wwsapi/job/init")
	public String jobInit(String product, String jobId, String params) {
		RestResponse response = new RestResponse();
		try {
			long jobInsId = wwseService.jobInit(product, jobId, params);
			response.put("jobInsId", jobInsId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/rerununpassed")
	public String jobRerunUnpassed(Long jobInsId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobRerunUnpassed(jobInsId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/rerunfailed")
	public String jobRerunfailed(Long jobInsId, String[] taskInsIds) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobRerunFailed(jobInsId, taskInsIds);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/rerun")
	public String jobRerun(
			Long jobInsId, 
			String[] taskInsIds, boolean recursion, boolean exceptRunning) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobRerun(jobInsId, taskInsIds, recursion, exceptRunning);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/driver")
	public String jobDriver(Long jobInsId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobDriver(new Long[]{jobInsId});
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/renovate")
	public String jobRenovate(Long jobInsId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobRenovate(jobInsId);
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/continue")
	public String jobContinue(Long jobInsId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobContinue(jobInsId);
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/pause")
	public String jobPause(Long jobInsId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobPause(jobInsId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/job/disable")
	public String jobDisabled(String product,Long jobInsId,String[] taskInsIds) {
		RestResponse response = new RestResponse();
		try {
			wwseService.jobDisabled(jobInsId, taskInsIds);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	/***************************************************************************************
	 * DataSource Public Interfaces
	 ******************************************************/
	
	@GetMapping("/wwsapi/ds/online")
	public String getDataSourcesOnLine(String product, String agent) {
		RestResponse response = new RestResponse();
		try {
			String dslist =  wwseService.getDataSourcesOnLine(product, agent);
			if (StringUtil.hasText(dslist)) {
				return dslist;
			} else {
				response.setSuccess(true);
				return JSON.toJSONString(response);
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
			return JSON.toJSONString(response);
		}
	}
	
	@GetMapping("/wwsapi/ds/info")
	public String getDataSourceInfo(String product,String agent,String name) {
		RestResponse response = new RestResponse();
		try {
			String info =  wwseService.getDataSourceInfo(product,agent,name);
			if (StringUtil.hasText(info)) {
				return info;
			} else {
				response.setSuccess(true);
				return JSON.toJSONString(response);
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
			return JSON.toJSONString(response);
		}
	}
	
	@GetMapping("/wwsapi/ds/existed")
	public String hasDataSource(String product,String agent,String name) {
		RestResponse response = new RestResponse();
		try {
			Boolean exist =  wwseService.hasDataSource(product,agent,name);
			response.setSuccess(true);
			response.setResult(exist);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/ds/destroy")
	public String destroyDataSource(String product,String agent,String name) {
		RestResponse response = new RestResponse();
		try {
			wwseService.destroyDataSource(name);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping("/wwsapi/ds")
	public String deleteDataSource(String product,String agent,String name) {
		RestResponse response = new RestResponse();
		try {
			wwseService.deleteDataSource(name);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/ds")
	public String createDataSource(String product,String agent,String name) {
		RestResponse response = new RestResponse();
		try {
			wwseService.createDataSource(name);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	/***************************************************************************************
	 * Cron Job Public Interfaces
	 ******************************************************/
	
	@DeleteMapping("/wwsapi/cron")
	public String delCronJob(String cronJobId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.deleteCronJob(cronJobId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/cron/start")
	public String cronJobStart(String cronJobId, Boolean clearShared) {
		RestResponse response = new RestResponse();
		try {
			wwseService.cronStart(cronJobId, clearShared);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/cron/stop")
	public String cronJobStop(String cronJobId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.cronStop(cronJobId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/wwsapi/cron/existed")
	public String hasCronJob(String cronJobId, String product, String agent) {
		RestResponse response = new RestResponse();
		try {
			Boolean exist = wwseService.hasCron(cronJobId, product, agent);
			response.setResult(exist);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/wwsapi/cron/online")
	public String cronJobOnLine(String product, String agent){
		RestResponse response = new RestResponse();
		try {
			Set<String> crons = null;// wwseService.cronOnLine(product, agent);
			response.setSuccess(true);
			response.put("crons", crons);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/encryption/text")
	public String encryption(String plaintext, String confirm) {
		RestResponse response = new RestResponse();
		try {
			response.put("cipher", wwseService.encryption(plaintext, confirm));
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/wwsapi/decryption/text")
	public String decryption(String ciphertext) {
		RestResponse response = new RestResponse();
		try {
			response.put("plain", wwseService.decryption(ciphertext));
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
}
