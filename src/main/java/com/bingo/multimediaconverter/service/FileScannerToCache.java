package com.bingo.multimediaconverter.service;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.bingo.multimediaconverter.common.Log;

public class FileScannerToCache {
	
	private final static Log log = Log.getLog(FileScannerToCache.class);
	
	private Cache cache;
	/**
	 * 待处理文件类型正则串
	 */
	private static String fileTypeStr = ".avi|.m2p|.swf|.flv|.wmv";
	
	public FileScannerToCache (Cache cache, String fileTypeStr){
		this.cache = cache;
	}
	
	/**
	 * 扫描文件夹,并将文件信息放入缓存
	 * @param file
	 * @param fileTypeStr 待扫描处理的文件类型 如：".avi|.mp4|.swf|.flv|.wmv"
	 */
	public void scan(File file){
		if(file.isFile()){
			return;
		}
		File[] listFiles = file.listFiles();
		for(File lf: listFiles){
			//如果扫描磁盘发现该文件是flv且没有加入缓存则放入缓存
			String fileName = lf.getName();
			if(lf.isFile() && fileTypeStr.contains(fileName.substring(fileName.lastIndexOf(".")))){
				if(cache.get(fileName) == null && !new File(lf.getAbsolutePath().replaceAll(fileTypeStr, ".mp4")).exists()){
					cache.put(new Element(fileName , lf));
				}else if(cache.get(fileName) != null && new File(lf.getAbsolutePath().replaceAll(fileTypeStr, ".mp4")).exists()){
					cache.remove(fileName);
				}
				log.info("get video file name:" + fileName + ", 文件路径" + lf.getAbsolutePath());
			}else{
				scan(lf);
			}
		}
	}


}
