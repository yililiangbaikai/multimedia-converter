package com.bingo.multimediaconverter.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * FFMpegUntil
 * <p>
 * Title: FFMpeg工具类
 * </p>
 * <p>
 * Description: 本类封装FFMpeg对视频的操作
 * </p>
 * <p>
 * @version 1.0
 */
public class FFMpegUtil implements IStringGetter {
	private static Log log = Log.getLog(FFMpegUtil.class);
	private int runtime = 0;
	private String ffmpegUri;// ffmpeg地址
	private String originFileUri; // 视频源文件地址
	private String path ;//视频转换格式之后的地址
	private List<String> cmd = new ArrayList<String>();

	private enum FFMpegUtilStatus {
		Empty, CheckingFile, GettingRuntime
	};

	private FFMpegUtilStatus status = FFMpegUtilStatus.Empty;

	/**
	 * 构造函数
	 * 
	 * @param ffmpegUri
	 *            ffmpeg的全路径 如e:/ffmpeg/ffmpeg.exe 或 /root/ffmpeg/bin/ffmpeg
	 * @param originFileUri
	 *            所操作视频文件的全路径 如e:/upload/temp/test.wmv
	 */
	public FFMpegUtil(String ffmpegUri, String originFileUri, String path) {
		this.ffmpegUri = ffmpegUri;
		this.originFileUri = originFileUri;
		this.path = path;
	}

	/**
	 * 获取视频时长
	 * 
	 * @return
	 */
	public int getRuntime() {
		runtime = 0;
		status = FFMpegUtilStatus.GettingRuntime;
		cmd.clear();
		cmd.add(ffmpegUri);
		cmd.add("-i");
		cmd.add(originFileUri);
		CmdExecuter.exec(cmd, this);
		return runtime;
	}
	
	public int getTime() {
		cmd.clear();
		cmd.add(ffmpegUri);
		cmd.add("-i");
		cmd.add(originFileUri);
		return CmdExecuter.getTime(cmd);
	}
	/**
	 * 转换视频
	 * @return
	 */
	public boolean procesMP4(){
		cmd.clear();
		cmd.add(ffmpegUri);
		cmd.add("-ss");
		cmd.add("00:00:00");
		cmd.add("-t");
		cmd.add("00:01:00");
		cmd.add("-i");
		cmd.add(originFileUri);
		cmd.add("-vcodec");
		cmd.add("copy");
		cmd.add("-acodec");
		cmd.add("copy");
		cmd.add(path);
		CmdExecuter.exec(cmd, this);
		return true;
	}
	
	/**
	 * flv到MP4
	 * 视频转换
	 * @return
	 */
	public boolean flv2Mp4(){
		cmd.clear();
		cmd.add(ffmpegUri);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(originFileUri);
		cmd.add("-acodec");
		cmd.add("libfdk_aac");
		cmd.add("-vcodec");
		cmd.add("libx264");
		cmd.add(path);
		CmdExecuter.exec(cmd, this);
		System.out.println(cmd.toString());
		return true;
	}

	/**
	 * 检测文件是否是支持的格式 将检测视频文件本身，而不是扩展名
	 * 
	 * @return
	 */
	public boolean isSupported() {
		isSupported = false;
		status = FFMpegUtilStatus.CheckingFile;
		cmd.clear();
		cmd.add(ffmpegUri);
		cmd.add("-i");
		cmd.add(originFileUri);
		cmd.add("Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s");
		CmdExecuter.exec(cmd, this);
		return isSupported;
	}

	private boolean isSupported;

	/**
	 * 生成视频截图
	 * 
	 * @param imageSavePath
	 *            截图文件保存全路径
	 * @param screenSize
	 *            截图大小 如640x480
	 */
	public void makeScreenCut(String imageSavePath, String screenSize) {
		cmd.clear();
		cmd.add(ffmpegUri);
		cmd.add("-i");
		cmd.add(originFileUri);
		cmd.add("-y");
		cmd.add("-f");
		cmd.add("image2");
		cmd.add("-ss");
		cmd.add("2");
		cmd.add("-t");
		cmd.add("0.001");
		cmd.add("-s");
		cmd.add(screenSize);
		cmd.add(imageSavePath);
		CmdExecuter.exec(cmd, this);
	}

	public void dealString(String str) {
		switch (status) {
		case Empty:
			break;
		case CheckingFile: {
			if (-1 != str.indexOf("Metadata:"))
				this.isSupported = true;
			break;
		}
		case GettingRuntime: {
			if(str.contains("Duration")){
				//System.out.println("时间:"+str.substring(str.indexOf(":")+1,str.indexOf(",")));
			}
			Matcher m = Pattern.compile("Duration").matcher(str);
			while (m.find()) {
				String msg = m.group();
				msg = msg.replace("Duration: ", "");
				//runtime = TimeUtil.runtimeToSecond(msg);
			}
			break;
		}
		default:{
			log.info("ffmpeg输出:"+str);
		}
		}// switch
	}

	
}