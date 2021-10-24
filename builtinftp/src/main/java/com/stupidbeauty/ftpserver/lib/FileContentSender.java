package com.stupidbeauty.ftpserver.lib;

import com.koushikdutta.async.AsyncSocket;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;
import android.app.Application;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import org.apache.commons.io.FileUtils;
import com.koushikdutta.async.callback.ConnectCallback;

public class FileContentSender
{
    private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
    private ControlConnectHandler controlConnectHandler=null; //!< 控制连接处理器。
    private AsyncSocket data_socket=null; //!< 当前的数据连接。
    private File rootDirectory=null; //!< 根目录。
    private File fileToSend=null; //!< 要发送的文件。
    
    public void  setRootDirectory(File rootDirectory) // 设置根目录。
    {
        this.rootDirectory=rootDirectory;
    } //public void  setRootDirectory(File rootDirectory)
    
    public void setControlConnectHandler(ControlConnectHandler controlConnectHandler) // 设置控制连接处理器。
    {
        this.controlConnectHandler=controlConnectHandler;
    } //public void setControlConnectHandler(ControlConnectHandler controlConnectHandler)
    
    /**
    * 设置数据连接套接字。
    */
    public void setDataSocket(AsyncSocket socket) 
    {
        data_socket=socket; // 记录。
        
        if (fileToSend!=null) // 有等待发送的内容。
        {
            startSendFileContent(); // 开始发送文件内容。
        } // if (dataSocketPendingByteArray!=null)
    } //public void setDataSocket(AsyncSocket socket)
    
    private void startSendFileContent() // 开始发送文件内容。
    {
        byte[] photoBytes=null; //数据内容。

        try //尝试构造请求对象，并且捕获可能的异常。
        {
            photoBytes= FileUtils.readFileToByteArray(fileToSend); //将照片文件内容全部读取。
        } //try //尝试构造请求对象，并且捕获可能的异常。
		catch (Exception e)
		{
			e.printStackTrace();
		}

        Util.writeAll(data_socket, photoBytes, new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] data Successfully wrote message");
                
                notifyFileSendCompleted(); // 告知已经发送文件内容数据。
            }
        });
    } //private void startSendFileContent()

    /**
    * 发送文件内容。
    */
    public void sendFileContent(String data51, String currentWorkingDirectory) 
    {
        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
        File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
        
        fileToSend=photoDirecotry; // 记录，要发送的文件对象。
        
        if (data_socket!=null) // 数据连接存在。
        {
            startSendFileContent(); // 开始发送文件内容。
        } //if (data_socket!=null) // 数据连接存在。
            
    } //private void sendFileContent(String data51, String currentWorkingDirectory)
    
    /**
    * 告知已经发送文件内容数据。
    */
    private void notifyFileSendCompleted() 
    {
        controlConnectHandler.notifyFileSendCompleted(); // 告知文件内容发送完毕。
    } //private void notifyFileSendCompleted()

    /**
    * 将回复数据排队。
    */
    private void queueForDataSocket(byte[] output) 
    {
        dataSocketPendingByteArray=output; // 排队。
    } //private void queueForDataSocket(String output)

    /**
    * 将回复数据排队。
    */
    private void queueForDataSocket(String output) 
    {
        dataSocketPendingByteArray=output.getBytes(); // 排队。
    } //private void queueForDataSocket(String output)
}
