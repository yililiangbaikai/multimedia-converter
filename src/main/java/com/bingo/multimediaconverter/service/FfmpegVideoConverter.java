package com.bingo.multimediaconverter.service;

import java.io.File;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
	public final static String ROOT_PATH = "/nas/SYPB/2017";
	
	/**
	 * 初始化ehcahe缓存实例
	 */
	public static Cache cache = null;
	
	public static String defaultFileTypeStr = ".flv|.m2p|.wmv|.avi|.swf";
	
	public static Set<String> d = new java.util.concurrent.ConcurrentSkipListSet<String>();

	
	static {
		if(cache == null){
			CacheManager manager = CacheManager.create(FfmpegVideoConverter.class.getResourceAsStream("/ehcache.xml"));
			cache = manager.getCache("ffmpeg-flv-file");
		}
	}
	
	public static void main(String[] args) {
		System.out.println("This is ffmpegVideoConverter program.");
		System.out.println("参数1：" + args[0]  + "\n参数2" + args[1]);
		try { 
		  LocateRegistry.createRegistry(1099);
	      //实例化实现了IRmiService接口的远程服务IRmiServiceImpl对象 
	      IRmiService service = new IRmiServiceImpl("convert2MP4"); 
	      //将名称绑定到对象,即向命名空间注册已经实例化的远程服务对象 
	      Naming.rebind("rmi://20.20.23.203:1099/convert2MP4", service); 
	    } catch (Exception e) {
	      e.printStackTrace();
	      log.error(e);
	    } 
	    System.out.println("服务器向命名表注册了1个远程服务对象！"); 
		//取命令行中传递过来的路径参数,第一个为待扫描文件根目录，第二个为待扫描文件类型
		String destDir = args[0];
		String fileTypeStr = args[1];
		File targetDir = null;
		//如果没有传参取默认值
		if(StringUtils.isNotBlank(destDir)){
			targetDir = new File(destDir);
		}else{
			targetDir = new File(ROOT_PATH);
		}
		if(StringUtils.isBlank(fileTypeStr)){
			fileTypeStr = defaultFileTypeStr;
		}
		 
		if(!targetDir.exists() || !targetDir.isDirectory()){
			log.error("根目录不存在");
			return;
		}
		
		FileScannerProcess scanProcess = new FfmpegVideoConverter().new FileScannerProcess(fileTypeStr, destDir);
		new Thread(scanProcess).start();
		
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while(true){
			if(cache.getSize() == 0){
				log.info("没有找到需要转换的文件，当前无操作。");
				//没有可以转换的视频则终止本程序
				System.exit(0);
			}else{
				for(int i=0; i<cache.getKeys().size(); i++){
					System.out.println("待转换文件个数："+cache.getSize());
					log.info("待转换文件个数："+cache.getSize());
					Object thekey = cache.getKeys().get(i);
					//File file = (File)cache.get(thekey);
					File file = (File)cache.get(thekey).getObjectValue();
					String originPath = file.getAbsolutePath();
					
					String destPath = originPath.toLowerCase().replaceAll("("+fileTypeStr+")", ".mp4");
					if(new File(destPath).exists()){
						log.info(destPath+"清空该缓存并跳过转换");
						cache.remove(thekey);
						break;
					}
					log.info("video源文件地址:" + originPath + "dest地址:" + destPath);
					//保证ffmpeg进程数只为10才往下执行
				    if(d.size() > 8){//|| !){
						//写日志，挂起程序
				    	countFFmpegProcessLessThan10("ffmpeg");
						log.info("队列已满，等待文件处理完出队。");
						break;
					}
					d.add(originPath);
					Flv2Mp4Process flv2Mp4Process = new FfmpegVideoConverter().new Flv2Mp4Process(originPath, destPath);
					new Thread(flv2Mp4Process).start();
				}
				//主进程休眠，等待转换任务完成，减小磁盘IO压力
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
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
		System.out.println(processName + "进程数：" + CmdExecuter.getProcessCount(cmdParams));
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
			String beginTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			new FFMpegUtil("ffmpeg", originPath, destPath).flv2Mp4();
			String endTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			System.out.println(originPath+"转换开始时间:"+beginTime+"结束时间："+endTime );
			d.remove(originPath);
		}
		
	}
	
	/**
	 * 文件夹扫描内部类
	 * @author long.xin
	 *
	 */
	private class FileScannerProcess implements Runnable{
		
		private String fileTypeStr;
		
		private String destPath;
		
		public FileScannerProcess(String fileTypeStr, String destPath){
			this.fileTypeStr = fileTypeStr;
			this.destPath = destPath;
		}

		@Override
		public void run() {
			//扫描文件夹
			new FileScannerToCache(cache, fileTypeStr).scan(new File(destPath));
		}
		
	}

}
