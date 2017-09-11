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

	@Override
	public String convert2MP4(String videoSourcePath) throws RemoteException {
		//扫描文件夹
		String beginTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String fileType = videoSourcePath.substring(videoSourcePath.lastIndexOf("."));
		String destPath = StringUtils.replace(videoSourcePath, fileType, ".mp4");
		new FFMpegUtil("ffmpeg", videoSourcePath, destPath).flv2Mp4();
		String endTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		System.out.println(videoSourcePath+"远程调用转换开始时间:"+beginTime+"结束时间："+endTime );
		return destPath;
	}
	public static void main(String[] args) {
		String fileType = "sdfsdfsddf.m2p".substring("sdfsdfsddf.m2p".lastIndexOf("."));
		System.out.println(fileType);
	}

}
