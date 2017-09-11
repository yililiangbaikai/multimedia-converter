package com.bingo.multimediaconverter.service;

import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface IRmiService extends Remote{

	//声明服务器端提供文件转换mp4的服务,并返回转换后文件路径 
	String convert2MP4(String content) throws RemoteException; 

}
