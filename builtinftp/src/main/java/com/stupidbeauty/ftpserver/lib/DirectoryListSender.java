package com.stupidbeauty.ftpserver.lib;

import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.os.Environment;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
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
  private boolean fileNameTolerant=false; //!< Set the file name tolerant mode.
  private FilePathInterpreter filePathInterpreter=null; //!< the file path interpreter.
  private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
  private ControlConnectHandler controlConnectHandler=null; //!< 控制连接处理器。
  private AsyncSocket data_socket=null; //!< 当前的数据连接。
  private File rootDirectory=null; //!< 根目录。
  private String wholeDirecotoryPath= ""; //!< The whole directory path to be used.
  private DocumentFile fileToSend=null; //!< 要发送的文件。
  private String subDirectoryName=null; //!< 要列出的子目录名字。
  private static final String TAG ="DirectoryListSender"; //!<  输出调试信息时使用的标记。
  private BinaryStringSender binaryStringSender=new BinaryStringSender(); //!< 以二进制方式发送字符串的工具。
  private String workingDirectory ; //!< Workding directory.
  private boolean extraInformationEnabled = true; //!< Whether we should send extra informations other than file names only.
  private boolean enableDolphinBug474238Placeholder = false;

  public void setEnableDolphinBug474238Placeholder(boolean enable) {
      this.enableDolphinBug474238Placeholder = enable;
  }

  public boolean isEnableDolphinBug474238Placeholder() {
      return enableDolphinBug474238Placeholder;
  }
  
  /**
  * Set the option of enabling extra information or not.
  */
  public void setExtraInformationEnabled(boolean enabled)
  {
    extraInformationEnabled = enabled;
  } // public void setExtraInformationEnabled(boolean enabled)
    
  /**
  * Set the file path interpreter.
  */
  public void setFilePathInterpreter(FilePathInterpreter filePathInterpreter)
  {
    this.filePathInterpreter=filePathInterpreter;
  } // public void setFilePathInterpreter(FilePathInterpreter filePathInterpreter)
  
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
      Log.d(TAG, CodePosition.newInstance().toString()+  ", data socket: " + socket ); // Debug.
      data_socket=socket; // 记录。
      
      binaryStringSender.setSocket(data_socket); // 设置套接字。
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send: " + fileToSend); // Debug.
      if ((fileToSend!=null) && (data_socket!=null)) // 有等待发送的内容。
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send: " + fileToSend); // Debug.
        startSendFileContentForLarge(); // 开始发送文件内容。
      } // if (dataSocketPendingByteArray!=null)
    } //public void setDataSocket(AsyncSocket socket)
    
    /**
    * 构造针对这个文件的一行输出。
    */
    private String construct1LineListFile(DocumentFile photoDirecotry) 
    {
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", path: " + photoDirecotry); // Debug.
//       File path=photoDirecotry;
      DocumentFile path=photoDirecotry;
    
      // -rw-r--r-- 1 nobody nobody     35179727 Oct 16 07:31 VID_20201015_181816.mp4

      String fileName=path.getName(); // 获取文件名。

      Date dateOfFile = new Date(path.lastModified());  
      Date dateNow=new Date();
      boolean sameYear=false; // 是不是相同年份。
            
      if (dateOfFile.getYear() == dateNow.getYear()) // 年份相等
      {
        sameYear=true; // 是相同年份。
      } // if (dateCompareYear.getYear() == dateNow.getYear()) // 年份相等
            
      // LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(path.lastModified()), ZoneId.systemDefault());
      
      Locale localEnUs = new Locale("en", "US"); // The en_US locale.

      // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
      SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", localEnUs);

      String time="8:00";
            
      time = formatter.format(dateOfFile); // 获取时间字符串。

      // DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM");
            
      // DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy").withLocale(Locale.US);
      SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", localEnUs);

      String year = yearFormatter.format(dateOfFile);  // 年份字符串。

      // DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM").withLocale(Locale.US);
      SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM", localEnUs);

      // DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd").withLocale(Locale.US);
      SimpleDateFormat dayFormatter = new SimpleDateFormat("dd", localEnUs);

      String dateString="30";
            
      dateString = dayFormatter.format(dateOfFile); // 获取日期。
                            
      long fileSize=path.length(); // 文件尺寸。
                            
      String group="cx";
                            
      String user = "ChenXin";
      
      
      Uri directoryUri=path.getUri(); // Get the uri.
      String directyoryUriPath=directoryUri.getPath(); // Get the string of the uri.

//       Path filePathObject=path.toPath(); // Get the associated nio Path object.

      // if (Build.VERSION.)
      // if (Build.VERSION.SDK_INT >= 29) // The sdk version is equal to or larger than 29
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) // Android sdk vresion 26. File.toPath
      {
        File fileObject=new File(directyoryUriPath);
        Path filePathObject=fileObject.toPath(); // Get the associated nio Path object.

        if (directoryUri.getScheme().equals("file")) // It is a native file
        {
          try // get the owner name
          {
            UserPrincipal userPrincipal= Files.getOwner(filePathObject);
            user=userPrincipal.getName(); // get the name of the user.
          } // try // get the owner name
          catch(IOException e)
          {
            Log.d(TAG, "construct1LineListFile, failed to get owner name:"); // Debug.
            
            e.printStackTrace();
          } // catch(IOException e)
        } // if (path.getScheme().equals("file")) // It is a native file
      } // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) // Android sdk vresion 26. File.toPath
      


                            
      String linkNumber="1";
                            
//             String permission="-rw-r--r--"; // 权限。
      String permission=getPermissionForFile(path); // 权限。

      String month="Jan"; // 月份 。
            
      month = monthFormatter.format(dateOfFile); // 序列化月份。
            
      String timeOrYear=time; // 时间或年份。
            
      if (sameYear) // 相同的年份。
      {
      } // if (sameYear) // 相同的年份。
      else // 不是相同的年份。
      {
        timeOrYear=year; // 年份。
      } // else // 不是相同的年份。

      String currentLine = ""; // The current line.
      
      if (extraInformationEnabled) // Send extra informations
      {
        currentLine = permission + " " + linkNumber + " " + user + " " + group + " " + fileSize + " " + month + " " + dateString + " " + timeOrYear + " " ; // 构造当前行。
      } // if (extraInformationEnabled) // Send extra informations
      
      currentLine = currentLine + fileName; // 构造当前行。

      return currentLine;
    } // private String construct1LineListFile(File photoDirecotry)
    
  /**
  * File name tolerant. For example: /Android/data/com.client.xrxs.com.xrxsapp/files/XrxsSignRecordLog/Zw40VlOyfctCQCiKL_63sg==, with a trailing <LF> (%0A).
  */
  public void setFileNameTolerant(boolean toleranttrue)
  {
    fileNameTolerant=toleranttrue; // Remember.
  } // public void setFileNameTolerant(boolean toleranttrue)
  
  /**
  * 获取目录的完整列表。
  */
  private String getDirectoryContentList(DocumentFile photoDirecotry, String nameOfFile)
  {
    nameOfFile = nameOfFile.trim(); // 去除空白字符。陈欣

    String result = ""; // 结果。

    if (photoDirecotry.isFile())  // 是一个文件。
    {
      String currentLine = construct1LineListFile(photoDirecotry); // 构造针对这个文件的一行输出。
      binaryStringSender.sendStringInBinaryMode(currentLine); // 发送回复内容。
    }
    else  // 是目录
    {
      DocumentFile[] paths = photoDirecotry.listFiles();

      if (paths.length == 0)  // 空目录
      {
        controlConnectHandler.checkFileManagerPermission(Constants.Permission.Read, null); // 检查权限

        // 👇 新增：如果启用了 Dolphin bug #474238 的绕过选项，则插入一个占位文件
        if (isEnableDolphinBug474238Placeholder())
        {
          String placeholderLine = "-rw-r--r-- 1 user group 0 Jan 01 00:00 .dolphin_placeholder\r\n";
          binaryStringSender.sendStringInBinaryMode(placeholderLine);
        }
      }
      else  // 列出成功
      {
        PathDocumentFileCacheManager pathDocumentFileCacheManager = filePathInterpreter.getPathDocumentFileCacheManager(); // 获取缓存管理器

        for (DocumentFile path : paths)  // 遍历每个文件
        {
          String fileName = path.getName(); // 获取文件名

          Log.d(TAG, CodePosition.newInstance().toString() + ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", target document: " + path.getUri().toString() + ", file name length: " + fileName.length() + ", file name content: " + fileName + ", root directory: " + rootDirectory + ", working directory: " + workingDirectory); // Debug.

          String wholeFilePath = filePathInterpreter.resolveWholeDirectoryPath(rootDirectory, workingDirectory, fileName); // 解析完整路径
          wholeFilePath = wholeFilePath.replace("//", "/"); // 替换双斜杠

          boolean isAVirtualPath = filePathInterpreter.isExactVirtualPath(wholeFilePath); // 是否是虚拟路径

          if (isAVirtualPath)  // 是虚拟路径
          {
            path = filePathInterpreter.getFile(rootDirectory, workingDirectory, fileName); // 替换为实际路径
          }

          String currentLine = construct1LineListFile(path); // 构造一行输出

          String effectiveVirtualPathForCurrentSegment = wholeDirecotoryPath + "/" + fileName; // 构建虚拟路径
          effectiveVirtualPathForCurrentSegment = effectiveVirtualPathForCurrentSegment.replace("//", "/"); // 去掉多余斜杠

          pathDocumentFileCacheManager.put(effectiveVirtualPathForCurrentSegment, path); // 存入缓存

          if (fileNameTolerant)  // 容错文件名特殊字符
          {
            String tolerantEffectiveVirtualPath = effectiveVirtualPathForCurrentSegment.trim();

            if (!tolerantEffectiveVirtualPath.equals(effectiveVirtualPathForCurrentSegment))
            {
              DocumentFile documentFileForTolerantPath = pathDocumentFileCacheManager.get(tolerantEffectiveVirtualPath);

              if (documentFileForTolerantPath == null)
              {
                pathDocumentFileCacheManager.put(tolerantEffectiveVirtualPath, path); // 添加容错映射
              }
            }
          }

          if (fileName.equals(nameOfFile) || nameOfFile.isEmpty())  // 匹配或全部列出
          {
            binaryStringSender.sendStringInBinaryMode(currentLine); // 发送当前行
          }
        }
      }
    }

    Util.writeAll(data_socket, "\r\n".getBytes(), new CompletedCallback()
    {
      @Override
      public void onCompleted(Exception ex)
      {
        if (ex != null) throw new RuntimeException(ex);

        Log.d(TAG, CodePosition.newInstance().toString() + ", [Server] data Successfully wrote message: " + fileToSend + ", going to close data_socket: " + data_socket); // Debug.

        notifyLsCompleted(); // 通知已发送完成
        fileToSend = null; // 清空文件对象
        data_socket.close(); // 关闭连接
      }
    });

    return result;
  }

    /**
    * 获取文件或目录的权限。
    */
    private String getPermissionForFile(DocumentFile path)
    {
      String permission = "-rw-r--r--"; // 默认文件权限

      if (path.isDirectory())   // 如果是目录
      {
        permission = "drwxrwxrwx"; // 最宽松的目录权限
      }
      else
      {
        permission = "-rw-rw-rw-"; // 最宽松的文件权限
      }

      return permission;
    }

    private void startSendFileContentForLarge()
    {
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send: " + fileToSend + ", uri: " + fileToSend.getUri().toString()); // Debug.
      if ( (fileToSend!=null) && fileToSend.exists()) // The file exists
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send: " + fileToSend + ", uri: " + fileToSend.getUri().toString()); // Debug.
        getDirectoryContentList(fileToSend, subDirectoryName); // Get the whole directory list.
      } //if (fileToSend.exist()) // 文件存在
      else // The file exist
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", not exist "); // Debug.
        notifyFileNotExist(); // Notify , file does not exist.
      } // else // The file exist
    } //private void startSendFileContentForLarge()
    
    /**
    * 发送文件内容。
    */
    public void sendDirectoryList(String data51, String currentWorkingDirectory) 
    {
      Log.d(TAG, CodePosition.newInstance().toString()+  ", directory to list: " + data51 + ", working directory: " + currentWorkingDirectory); // Debug.
      
      workingDirectory = currentWorkingDirectory; // Remember working directory.
      
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

      wholeDirecotoryPath = filePathInterpreter.resolveWholeDirectoryPath( rootDirectory, currentWorkingDirectory, parameter); // resolve whole directory path.
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, parameter); // resolve 目录。
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", directory : " + photoDirecotry + ", working directory: " + currentWorkingDirectory + ", directory uri: " + photoDirecotry.getUri().toString() + ", whole directory path: " + wholeDirecotoryPath); // Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", going to set file to send : " + photoDirecotry); // Debug.

      fileToSend=photoDirecotry; // 记录，要发送的文件对象。
        
      if (data_socket!=null) // 数据连接存在。
      {
        startSendFileContentForLarge(); // 开始发送文件内容。
      } //if (data_socket!=null) // 数据连接存在。
      else // The data socket does not exist yet
      {
        // Log.d(TAG, CodePosition.newInstance().toString()+  ", directory : " + photoDirecotry + ", working directory: " + currentWorkingDirectory + ", directory uri: " + photoDirecotry.getUri().toString() + ", whole directory path: " + wholeDirecotoryPath + ", data socket not exist, skip"); // Debug.
      } // else // The data socket does not exist yet
    } // private void sendFileContent(String data51, String currentWorkingDirectory)
    
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
    
    /**
    * Notify that the file does not exist
    */
    private void notifyFileNotExist()
    {
      controlConnectHandler.notifyFileNotExist(wholeDirecotoryPath); // 告知文件不存在。
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
