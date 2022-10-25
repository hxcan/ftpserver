package com.stupidbeauty.ftpserver.lib;

import java.util.Locale;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import android.util.Log;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
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

public class DirectoryListSender
{
  private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
  private ControlConnectHandler controlConnectHandler=null; //!< 控制连接处理器。
  private AsyncSocket data_socket=null; //!< 当前的数据连接。
  private File rootDirectory=null; //!< 根目录。
  private File fileToSend=null; //!< 要发送的文件。
  private String subDirectoryName=null; //!< 要列出的子目录名字。
  private static final String TAG ="DirectoryListSender"; //!<  输出调试信息时使用的标记。
  private BinaryStringSender binaryStringSender=new BinaryStringSender(); //!< 以二进制方式发送字符串的工具。
    
    /**
    * 设置根目录。
    */
    public void setRootDirectory(File rootDirectory)
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
        
        binaryStringSender.setSocket(data_socket); // 设置套接字。
        
        if ((fileToSend!=null) && (data_socket!=null)) // 有等待发送的内容。
        {
            startSendFileContentForLarge(); // 开始发送文件内容。
        } // if (dataSocketPendingByteArray!=null)
    } //public void setDataSocket(AsyncSocket socket)
    
    /**
    * 构造针对这个文件的一行输出。
    */
    private String construct1LineListFile(File photoDirecotry) 
    {
      File path=photoDirecotry;
    
      // -rw-r--r-- 1 nobody nobody     35179727 Oct 16 07:31 VID_20201015_181816.mp4

      String fileName=path.getName(); // 获取文件名。

      Date dateCompareYear=new Date(path.lastModified());  
      Date dateNow=new Date();
      boolean sameYear=false; // 是不是相同年份。
            
      if (dateCompareYear.getYear() == dateNow.getYear()) // 年份相等
      {
        sameYear=true; // 是相同年份。
      } // if (dateCompareYear.getYear() == dateNow.getYear()) // 年份相等
            
      LocalDateTime date =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(path.lastModified()), ZoneId.systemDefault());
                            
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
//   String time= date.format(formatter);
      String time="8:00";
            
      time=date.format(formatter); // 获取时间字符串。

      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM");
            
      DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy").withLocale(Locale.US);

      String year=date.format(yearFormatter);  // 年份字符串。

      DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM").withLocale(Locale.US);

      DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd").withLocale(Locale.US);

      String dateString="30";
            
      dateString=date.format(dayFormatter); // 获取日期。
                            
      long fileSize=path.length(); // 文件尺寸。
                            
      String group="cx";
                            
      String user = "ChenXin";
                            
      String linkNumber="1";
                            
//             String permission="-rw-r--r--"; // 权限。
      String permission=getPermissionForFile(path); // 权限。

      String month="Jan"; // 月份 。
            
      month=date.format(monthFormatter); // 序列化月份。
            
      String timeOrYear=time; // 时间或年份。
            
      if (sameYear) // 相同的年份。
      {
      } // if (sameYear) // 相同的年份。
      else // 不是相同的年份。
      {
        timeOrYear=year; // 年份。
      } // else // 不是相同的年份。
            
      String currentLine = permission + " " + linkNumber + " " + user + " " + group + " " + fileSize + " " + month + " " + dateString + " " + timeOrYear + " " + fileName; // 构造当前行。

      return currentLine;
    } // private String construct1LineListFile(File photoDirecotry)
    
    /**
    *  获取目录的完整列表。
    */
    private String getDirectoryContentList(File photoDirecotry, String nameOfFile)
    {
      nameOfFile=nameOfFile.trim(); // 去除空白字符。陈欣
    
      String result=""; // 结果。
        
      if (photoDirecotry.isFile()) // 是一个文件。
      {
        String currentLine=construct1LineListFile(photoDirecotry); // 构造针对这个文件的一行输出。
        
        binaryStringSender.sendStringInBinaryMode(currentLine); // 发送回复内容。
      } // if (photoDirecotry.isFile()) // 是一个文件。
      else // 是目录
      {
        File[] paths = photoDirecotry.listFiles();

        if (paths!=null) // NOt null pointer
        {
          Log.d(TAG, "getDirectoryContentList, path: " + photoDirecotry + ", file amount: " + paths.length); // Debug.
          
          // for each pathname in pathname array
          for(File path:paths) 
          {
            String currentLine=construct1LineListFile(path); // 构造针对这个文件的一行输出。

            String fileName=path.getName(); // 获取文件名。

            if (fileName.equals(nameOfFile)  || (nameOfFile.isEmpty())) // 名字匹配。
            {
              binaryStringSender.sendStringInBinaryMode(currentLine); // 发送回复内容。
            } //if (fileName.equals(nameOfFile)) // 名字匹配。
          }
        }
        
      } // else // 是目录
         
      Util.writeAll(data_socket, ( "\r\n").getBytes(), new CompletedCallback() 
      {
        @Override
        public void onCompleted(Exception ex) 
        {
          if (ex != null) throw new RuntimeException(ex);
          System.out.println("[Server] data Successfully wrote message");
                
          notifyLsCompleted(); // 告知已经发送目录数据。
          fileToSend=null; // 将要发送的文件对象清空。
          data_socket.close(); // 关闭连接。
        }
      });

      return result;
    } //private String getDirectoryContentList(String wholeDirecotoryPath)

    /**
    * 获取文件或目录的权限。
    */
    private String  getPermissionForFile(File path)
    {
      String permission="-rw-r--r--"; // 默认权限。
        
      Log.d(TAG, "getPermissionForFile, path: " + path + ", is directory: " + path.isDirectory()); // Debug.
        
      if (path.isDirectory())
      {
        permission="drw-r--r--"; // 目录默认权限。
      }
        
      return permission;
    } //private String  getPermissionForFile(File path)

    private void startSendFileContentForLarge()
    {
      if (fileToSend.exists()) // 文件存在
      {
        getDirectoryContentList(fileToSend, subDirectoryName); // Get the whole directory list.
      } //if (fileToSend.exist()) // 文件存在
      else
      {
        notifyFileNotExist(); // 报告文件不存在。
      }
    } //private void startSendFileContentForLarge()
    
    /**
    * 开始发送文件内容。
    */
    private void startSendFileContent() 
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

		if (photoBytes!=null) // 读取的文件存在
		{
            Util.writeAll(data_socket, photoBytes, new CompletedCallback() 
            {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] data Successfully wrote message");
                    
                    notifyFileSendCompleted(); // 告知已经发送文件内容数据。
                    fileToSend=null; // 将要发送的文件对象清空。
                }
            });
		} //if (photoBytes!=null) // 读取的文件存在
		else // 读取的文件不存在
		{
      notifyFileNotExist(); // 告知文件不存在
		} //else // 读取的文件不存在
    } //private void startSendFileContent()

    /**
    * 发送文件内容。
    */
    public void sendFileContent(String data51, String currentWorkingDirectory) 
    {
      String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
      wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
      FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。

      fileToSend=photoDirecotry; // 记录，要发送的文件对象。
        
      if (data_socket!=null) // 数据连接存在。
      {
        startSendFileContentForLarge(); // 开始发送文件内容。
      } //if (data_socket!=null) // 数据连接存在。
    } //private void sendFileContent(String data51, String currentWorkingDirectory)
    
    /**
    * 发送文件内容。
    */
    public void sendDirectoryList(String data51, String currentWorkingDirectory) 
    {
      String parameter=""; // 要列出的目录。
      
      int directoryIndex=5; // 要找的下标。
      
      if (directoryIndex<=(data51.length()-1)) // 有足够的字符串长度。
      {
        parameter=data51.substring(directoryIndex).trim(); // 获取额外参数。
      } // if (directoryIndex<=(data51.length()-1)) // 有足够的字符串长度。
        
      if (parameter.equals("-la")) // 忽略
      {
        parameter=""; // 忽略成空白。
      } //if (parameter.equals("-la")) // 忽略
        
      subDirectoryName=parameter; // 记录可能的子目录名字。

      FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, parameter); //照片目录。

      fileToSend=photoDirecotry; // 记录，要发送的文件对象。
        
      if (data_socket!=null) // 数据连接存在。
      {
        startSendFileContentForLarge(); // 开始发送文件内容。
      } //if (data_socket!=null) // 数据连接存在。
    } //private void sendFileContent(String data51, String currentWorkingDirectory)
    
    private void notifyLsCompleted()
    {
      controlConnectHandler.notifyLsCompleted();
    } //private void notifyLsCompleted()

    /**
    * 告知已经发送文件内容数据。
    */
    private void notifyFileSendCompleted() 
    {
      controlConnectHandler.notifyFileSendCompleted(); // 告知文件内容发送完毕。
    } //private void notifyFileSendCompleted()
    
    private void notifyFileNotExist() // 告知文件不存在
    {
      controlConnectHandler.notifyFileNotExist(); // 告知文件不存在。
    } //private void notifyFileNotExist()

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
