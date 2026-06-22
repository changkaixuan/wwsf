package com.bocsoft.wwsf.webconsole;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.web.multipart.MultipartFile;

public class UploadUtils {

	public static final String JOB_UPLOAD = "/job/upload/";
			
	public static String getUploadPath(String fileReleasePath,String loginName,String uId) {
		String retPath = fileReleasePath + JOB_UPLOAD + loginName + "/" + uId + "/";
		File f = new File(retPath);
		if(!f.exists()) {
			f.mkdirs();
		}
		return retPath;
	}

	public static void backUpExcelFile(String uploadPath, String uploadFileName,MultipartFile multipartFile) {
		File f = new File(uploadPath+uploadFileName);
		if(f.exists()) {
			f.mkdirs();
		}
		FileInputStream fileInputStream;
		try {
			fileInputStream = (FileInputStream) multipartFile.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(fileInputStream);
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			byte[] bytes = new byte[1024];
			while(bis.read(bytes) != -1){
				bos.write(bytes);
			}
			bos.flush();
			bos.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeFile(InputStream in, File file) {
		try(
			OutputStream out = new FileOutputStream(file);
		){
			int readSize;
			byte[] buf = new byte[5120];
			while ((readSize = in.read(buf)) != -1) {
				out.write(buf, 0, readSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) try { in.close(); } catch (Exception ex) { }
		}
	}
	
}
