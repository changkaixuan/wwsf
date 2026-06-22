package com.bocsoft.wwsf.webconsole.control;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.UploadUtils;
import com.bocsoft.wwsf.webconsole.model.FileSource;

@Configuration
@RestController
public class ResourcesController {

	@Value("${user.root.directory}")
	private String root;
	
	@Value("${user.file.inodes}")
	private int inode;
	
	@PostMapping(value="/resource/upload")
	public String upload(@RequestParam("product") String uProduct,@RequestParam("resourceFile") MultipartFile multipartFile) {
		RestResponse response = new RestResponse();
		String oFileName = multipartFile.getOriginalFilename();
		try {
			multipartFile.getInputStream();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/resource/dirTree")
	public String dirTree(@RequestParam("product") String product) {
		RestResponse response = new RestResponse();
		try {
			File prodDir = new File(root, product);
			if (!prodDir.exists()) {
				prodDir.mkdirs();
			}
			FileSource fs = listDirectory(product, prodDir.getAbsolutePath(), prodDir);
			if (fs != null) {
				response.getData().put("dirTreeData", fs);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/resource/folder")
	public String folder(@RequestParam("product") String product, @RequestParam("folder") String folder) {
		RestResponse response = new RestResponse();
		try {
			File prodDir = new File(root, product);
			if (!prodDir.exists()) {
				prodDir.mkdirs();
			}
			File targDir = new File(prodDir, folder);
			FileSource targetFS = new FileSource();
			targetFS.setProduct(product);
			targetFS.setLength(targDir.length());
			targetFS.setFolder("true");
			targetFS.setExpanded("true");
			targetFS.setLastModified(CalendarUtil.getTime(targDir.lastModified(), CalendarUtil.DEFAULT_DATE_PATTERN));
			if (targDir.getAbsolutePath().equals(root)) {
				targetFS.setKey(File.separator);
			} else {
				targetFS.setKey(targDir.getAbsolutePath().replace(root, ""));
			}
			targetFS.setTitle(targDir.getName());
			targetFS.setChildren(new ArrayList<FileSource>());
			
			File[] files = new File(prodDir, folder).listFiles();
			for (File sfile : files) {
				FileSource fs = new FileSource();
				fs.setProduct(product);
				fs.setLength(sfile.length());
				fs.setFolder(sfile.isDirectory() ? "true" : "false");
				fs.setExpanded("true");
				fs.setLastModified(CalendarUtil.getTime(sfile.lastModified(), CalendarUtil.DEFAULT_DATE_PATTERN));
				fs.setKey(sfile.getAbsolutePath().replace(root, ""));
				fs.setTitle(sfile.getName());
				targetFS.getChildren().add(fs);
			}
			
			response.getData().put("folder", targetFS);
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping(value="/resource/makeFolder", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse makeFolder(
			@RequestParam("product") String product, 
			@RequestParam("parentFolder") String parent, 
			@RequestParam("folderName") String folderName) {
		RestResponse response = new RestResponse();
		try {
			File prodDir = new File(root, product);
			if (!prodDir.exists()) {
				prodDir.mkdirs();
			}
			File parentDir = new File(prodDir, parent);
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			File folderDir = new File(prodDir, folderName);
			if (!folderDir.exists()) {
				folderDir.mkdirs();
			}
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
		}
		return response;
	}
	
	@PostMapping(value="/resource/upload", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse uploadFile(
			@RequestParam("product") String product, 
			@RequestParam("parentFolder") String parent, 
			HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			File prodDir = new File(root, product);
			if (!prodDir.exists()) {
				prodDir.mkdirs();
			}
			File parentDir = new File(prodDir, parent);
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			
			Collection<Part> parts = request.getParts();
			
			for (Part part : parts) {
				File targetFile = new File(parentDir, part.getName());
				UploadUtils.writeFile(part.getInputStream(), targetFile);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
		}
		return response;
	}
	
	public static String fileSHA1(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		int len;
		byte[] readBuf = new byte[8192];
		try {
			digest = MessageDigest.getInstance("SHA-1");
			in = new FileInputStream(file);
			while((len = in.read(readBuf)) != -1) {
				digest.update(readBuf, 0, len);
			}
			return new BigInteger(1, digest.digest()).toString(16);
		} catch (Exception e) {
			return null;
		} finally {
			try { if (in != null) in.close(); } catch (Exception ex) { }
		}
	}
	
	public static String fileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		int len;
		byte[] readBuf = new byte[8192];
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while((len = in.read(readBuf)) != -1) {
				digest.update(readBuf, 0, len);
			}
			return new BigInteger(1, digest.digest()).toString(16);
		} catch (Exception e) {
			return null;
		} finally {
			try { if (in != null) in.close(); } catch (Exception ex) { }
		}
	}
	
	public FileSource listDirectory(String product, String root, File file) {
		if (file == null || !file.isDirectory()) {
			return null;
		}
		FileSource fs = new FileSource();
		fs.setProduct(product);
		fs.setFolder("true");
		fs.setExpanded("true");
		fs.setLength(file.length());
		fs.setLastModified(CalendarUtil.getTime(file.lastModified(), CalendarUtil.DEFAULT_DATE_PATTERN));
		if (file.getAbsolutePath().equals(root)) {
			fs.setKey(File.separator);
		} else {
			fs.setKey(file.getAbsolutePath().replace(root, ""));
		}
		fs.setTitle(file.getName());
		
		File[] subFiles = file.listFiles();
		for (File sfile : subFiles) {
			if (sfile.isDirectory()) {
				if (fs.children == null) {
					fs.setChildren(new ArrayList<FileSource>());
				}
				fs.getChildren().add(listDirectory(product, root, sfile));
			}
		}
		return fs;
	}
}
