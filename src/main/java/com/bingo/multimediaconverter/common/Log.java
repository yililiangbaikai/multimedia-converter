package com.bingo.multimediaconverter.common;

import org.apache.log4j.Logger;

/**
 * 获取日志的公共类
 * 
 * @author xlsiek
 *
 */
public class Log {
	private Logger log = null;

	private Log(Class c) {
		log = Logger.getLogger(c);
	}

	public static Log getLog(Class obj) {
		return new Log(obj);
	}

	public void error(Object message) {
		log.error(message);
	}

	public void error(Object message, Throwable t) {
		log.error(message, t);
	}

	public void info(Object message) {
		log.info(message);
	}

	public void info(Object message, Throwable t) {
		log.info(message, t);
	}

	public void debug(Object message) {
		log.debug(message);
	}

	public void debug(Object message, Throwable t) {
		log.debug(message, t);
	}

	public void warn(Object message) {
		log.warn(message);
	}

	public void warn(Object message, Throwable t) {
		log.warn(message, t);
	}

}
