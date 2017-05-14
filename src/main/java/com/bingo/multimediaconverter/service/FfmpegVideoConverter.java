package com.bingo.multimediaconverter.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bingo.multimediaconverter.common.CmdExecuter;
import com.bingo.multimediaconverter.common.FFMpegUtil;
import com.bingo.multimediaconverter.common.Log;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
/**
 * flv文件转mp4转换工具类
 * @author long.xin
 *
 */
public class FfmpegVideoConverter {
	
	private final static Log log = Log.getLog(FfmpegVideoConverter.class);
	
	/**
	 * 待转换文件根目录
	 */
	public final static String ROOT_PATH = "/nas/SYPB";
	
	/**
	 * 初始化ehcahe缓存实例
	 */
	public static Cache cache = null;
	
	static {
		if(cache == null){
			CacheManager manager = CacheManager.create(FfmpegVideoConverter.class.getResourceAsStream("/ehcache.xml"));
			cache = manager.getCache("ffmpeg-flv-file");
		}
	}
	
	public static void main(String[] args) {
		System.out.println("This is ffmpegVideoConverter program.");
		File targetDir = new File(ROOT_PATH);
		if(!targetDir.exists() || !targetDir.isDirectory()){
			log.error("根目录不存在");
			return;
		}
		
		FfmpegVideoConverter.FileScannerProcess scanProcess = new FfmpegVideoConverter().new FileScannerProcess();
		scanProcess.run();
		
		while(true){
			if(cache.getSize() == 0){
				log.info("没有找到需要转换的文件，当前无操作。");
			}else{
				for(int i=0; i<cache.getKeys().size(); i++){
					Object thekey = cache.getKeys().get(i);
					//File file = (File)cache.get(thekey);
					File file = (File)cache.get(thekey).getObjectValue();
					String originPath = file.getAbsolutePath();
					String destPath = originPath.toLowerCase().replace(".flv", ".mp4");
					//保证ffmpeg进程数只为10才往下执行
					while(!countFFmpegProcessLessThan10("ffmpeg")){
						//写日志，挂起程序
						log.info("当前ffmpeg进程数为：" + countFFmpegProcessLessThan10("ffmpeg"));
					}
					FfmpegVideoConverter.Flv2Mp4Process flv2Mp4Process = new FfmpegVideoConverter().new Flv2Mp4Process(originPath, destPath);
					flv2Mp4Process.run();
				}
			}
		}
	}
	
	/**
	 * 获取进程数据
	 * @param processName 进程id（如ffmpeg）
	 * @return
	 */
	private static boolean countFFmpegProcessLessThan10(String processName){
		List<String> cmdParams = new ArrayList<String>();
		cmdParams.add("ps");
		cmdParams.add("-ef");
		cmdParams.add("|grep");
		cmdParams.add(processName);
		cmdParams.add("|wc");
		cmdParams.add("-l");
		return CmdExecuter.getProcessCount(cmdParams) < 10;
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
			//扫描文件夹
			new FFMpegUtil("ffmpeg", originPath, destPath).flv2Mp4();
		}
		
	}
	
	/**
	 * 文件夹扫描内部类
	 * @author long.xin
	 *
	 */
	private class FileScannerProcess implements Runnable{

		@Override
		public void run() {
			//扫描文件夹
			new FileScannerToCache(cache);
			FileScannerToCache.scan(new File(ROOT_PATH));
		}
		
	}

}
