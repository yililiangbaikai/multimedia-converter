package com.bingo.multimediaconverter.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject; 
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.bingo.multimediaconverter.common.FFMpegUtil;

/**
 * 远程调用实现类，本类主要处理远程调用MP4转换服务
 * @author TSOSilence
 *
 */
public class IRmiServiceImpl extends UnicastRemoteObject implements IRmiService{

	/**
	 * 序列化号
	 */
	private static final long serialVersionUID = 3893786219827278705L;
	
	private String name; 

	public IRmiServiceImpl(String name) throws RemoteException { 
		this.name = name; 
	} 
	
	/**
	 * ffmpeg 文件转换进程
	 * @author long.xin
	 *
	 */
	private class Flv2Mp4Process implements Runnable{
		
		private String originPath;
		
		private String destPath;
		
		
		public Flv2Mp4Process(String originPath, String destPath) {
			super();
			this.originPath = originPath;
			this.destPath = destPath;
		}

		@Override
		public void run() {
			String beginTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			new FFMpegUtil("ffmpeg", originPath, destPath).flv2Mp4();
			String endTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			System.out.println(originPath + "远程调用转换开始时间:" + beginTime + "结束时间：" + endTime );
		}
		
	}

	@Override
	public String convert2MP4(String videoSourcePath) throws RemoteException {
		String fileType = videoSourcePath.substring(videoSourcePath.lastIndexOf("."));
		String destPath = StringUtils.replace(videoSourcePath, fileType, ".mp4");
		Flv2Mp4Process flv2Mp4Process = new Flv2Mp4Process(videoSourcePath, destPath);
		new Thread(flv2Mp4Process).start();
		System.out.println("线程执行中，不等待直接返回。");
		return destPath;
	}
	
	public static void main(String[] args) {
		String fileType = "sdfsdfsddf.m2p".substring("sdfsdfsddf.m2p".lastIndexOf("."));
		System.out.println(fileType);
	}

}
