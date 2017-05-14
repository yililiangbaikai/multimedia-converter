package com.bingo.multimediaconverter.common;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CmdExecuter {
	/**
	 * 执行指令
	 * 
	 * @param cmd
	 *            执行指令
	 * @param getter
	 *            指令返回处理接口，若为null则不处理输出
	 */
	static public void exec(List<String> cmd, IStringGetter getter) {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(cmd);
			builder.redirectErrorStream(true);
			Process proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String line;
			while ((line = stdout.readLine()) != null) {
				if (getter != null)
					getter.dealString(line);
			}
			proc.waitFor();
			stdout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 执行指令
	 * 
	 * @param cmd
	 *            执行指令
	 * @param getter
	 *            指令返回处理接口，若为null则不处理输出
	 */
	static public int getTime(List<String> cmd) {
		int min = 0;
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(cmd);
			builder.redirectErrorStream(true);
			Process proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String line;
			while ((line = stdout.readLine()) != null) {
				if (line.contains("Duration")) {
					String t = line.substring(line.indexOf(":") + 1,
							line.indexOf(","));
					t = StringUtils.strip(t);
					String strs[] = t.split(":");
					if (strs[0].compareTo("0") > 0) {
						min += Integer.valueOf(strs[0]) * 60 * 60;// 秒
					}
					if (strs[1].compareTo("0") > 0) {
						min += Integer.valueOf(strs[1]) * 60;
					}
					if (strs[2].compareTo("0") > 0) {
						min += Math.round(Float.valueOf(strs[2]));
					}
				}
			}
			proc.waitFor();
			stdout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return min;
	}

	/**
	 * 获取进程数
	 * @param cmdParams
	 * @return
	 */
	public static int getProcessCount(List<String> cmd) {
		int count = 0;
		try {
			//Process proc = Runtime.getRuntime().exec("ps -ef | grep ffmpeg | wc -l");这个方法会阻塞需要对输入输出流进行处理
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("/bin/sh -c ps -ef | grep ffmpeg | wc -l");
			builder.redirectErrorStream(true);
			Process proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String line;
			while ((line = stdout.readLine()) != null) {
				System.out.println(line);
				count = Integer.parseInt(line.trim()); 
			}
			proc.waitFor();
			stdout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

}
