package com.bingo.multimediaconverter.service;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.bingo.multimediaconverter.common.Log;

public class FileScannerToCache {
	
	private final static Log log = Log.getLog(FileScannerToCache.class);
	
	private static Cache cache;
	
	public FileScannerToCache (Cache cache){
		this.cache = cache;
	}
	
	/**
	 * 扫描文件夹,并将文件信息放入缓存
	 * @param file
	 */
	public static void scan(File file){
		if(file.isFile()){
			return;
		}
		File[] listFiles = file.listFiles();
		for(File lf: listFiles){
			//如果扫描磁盘发现该文件是flv且没有加入缓存则放入缓存
			String fileName = lf.getName();
			if(lf.isFile() && fileName.toLowerCase().contains("flv")){
				if(cache.get(fileName) == null && !new File(lf.getAbsolutePath().replace("flv", "mp4")).exists()){
					cache.put(new Element(fileName , lf));
				}else if(cache.get(fileName) != null && new File(lf.getAbsolutePath().replace("flv", "mp4")).exists()){
					cache.remove(fileName);
				}
				log.info("get flv file name:" + fileName + ", 文件路径" + lf.getAbsolutePath());
			}else{
				scan(lf);
			}
		}
	}


}
