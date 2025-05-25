package com.stupidbeauty.ftpserver.lib;

import java.util.concurrent.ThreadLocalRandom;

import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.content.Context;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.os.Environment;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import java.io.IOException;
import com.koushikdutta.async.*;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;
import android.os.Handler;
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.AsyncServerSocket;

public class DisconnectIntervalManager
{
  private FilePathInterpreter filePathInterpreter=null; //!< the file path interpreter.
  private static final String TAG="DisconnectIntervalManager"; //!< The tag used for debug code.
  private long restSTart=0; //!< 跳过位置。
  private long scheduledDisconnectTimestamp=0; //!< Scheduled disconnect time stamp.
  private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
  private ControlConnectHandler controlConnectHandler=null; //!< 控制连接处理器。
  private AsyncSocket data_socket=null; //!< 当前的数据连接。
  private File rootDirectory=null; //!< 根目录。
  private DocumentFile fileToSend=null; //!< 要发送的文件。
  private Context context=null; //!< Context.
  private String wholeDirecotoryPath= ""; //!< The full path of the file to send.
  private long newCommandAmount=0; //!<  The amount of new command.
  private long newCommandTimeDelayTotal=0; //!< The total delay of new commands.
  
  /**
  * mark scheduled disconnect.
  */
  public void markScheduleDisconnect()
  {
    long startTimestamp=System.currentTimeMillis(); // get the curent time.
    
    scheduledDisconnectTimestamp=startTimestamp;
  } // public void markScheduleDisconnect()
  
  /**
  * Get suggested disconnect interval.
  */
  public long getSuggestedDisconnectInterval()
  {
    // long averageNewCommandDelay = 100; // Get the average delay of new command.
    long averageNewCommandDelay = ThreadLocalRandom.current().nextLong(100, 1001); // [100, 1000]
    
    if (newCommandAmount>0) // we have received new comamnds
    {
      averageNewCommandDelay=newCommandTimeDelayTotal/newCommandAmount; // Get the average delay of new command.
    } // if (newCommandAmount>0) // we have received new comamnds

    Log.d(TAG, CodePosition.newInstance().toString()+  ", averageNewCommandDelay : " + averageNewCommandDelay); // Debug.

    long result=averageNewCommandDelay*10; // Result;

    return result;
  } // public long getSuggestedDisconnectInterval()
  
  /**
  * mark new command.
  */
  public void markNewCommand()
  {
    long startTimestamp=System.currentTimeMillis(); // get the curent time.
    
    if (scheduledDisconnectTimestamp!=0) // remembered schedule time stamp.
    {
      long timeDiff=startTimestamp-scheduledDisconnectTimestamp; // get the time difference.
      
      newCommandTimeDelayTotal+=timeDiff;
      newCommandAmount++;
      
      scheduledDisconnectTimestamp=0;
    } //       long startTimestamp=System.currentTimeMillis(); // 记录开始时间戳。


    // Chen xin
    // restSTart=data51; // 记录。
  } // public void setRestartPosition(long data51)

  /**
  * Set the file path interpreter.
  */
  public void setFilePathInterpreter(FilePathInterpreter filePathInterpreter)
  {
    this.filePathInterpreter=filePathInterpreter;
  } // public void setFilePathInterpreter(FilePathInterpreter filePathInterpreter)

  public void setContext(Context context)
  {
    this.context=context;
  } // fileContentSender
  
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
        
      if ((fileToSend!=null) && (data_socket!=null)) // 有等待发送的内容。
      {
        startSendFileContentForLarge(); // 开始发送文件内容。
      } // if (dataSocketPendingByteArray!=null)
    } //public void setDataSocket(AsyncSocket socket)
    
    /**
    * Start send file for large file. And also small files.
    */
    private void startSendFileContentForLarge()
    {
      if (fileToSend.exists()) // The file exists.
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send : " + fileToSend + ", uri: " + fileToSend.getUri().toString()); // Debug.
        
        notifyFileSendStarted(); // Notify the file send started.
        
        try
        {
          Uri fileUri=fileToSend.getUri(); // Get the uri.
          
          final InputStream is  = context.getContentResolver().openInputStream(fileUri);     
          
          if (restSTart>0) // 要跳过。 
          {
            is.skip(restSTart); // 跳过一段内容。断点续传。
          
            restSTart=0; // 跳过位置归零。
          }

          Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send : " + fileToSend + ", uri: " + fileToSend.getUri().toString() + ", data_socket: " + data_socket); // Debug.

          Util.pump(is, data_socket, new CompletedCallback()
          {
            @Override
            public void onCompleted(Exception ex)
            {
              if(ex != null) // 有异常。陈欣。
              {
                if ( ex instanceof IOException ) // java.lang.RuntimeException: java.io.IOException: Software caused connection abort
                {
                  ex.printStackTrace();
                }
                else // Other exceptions
                {
                  throw new RuntimeException(ex);
                }
              }

              Log.d(TAG, "startSendFileContentForLarge, file sent."); // Debug.
                    
              // notifyFileSendCompleted(); // 告知已经发送文件内容数据。
              delayednotifyFileSendCompleted(); // Notify the file send completed after a short delay.

              fileToSend=null; // 将要发送的文件对象清空。
              Log.d(TAG, CodePosition.newInstance().toString()+  ", file sent. Closing data socket: " + data_socket); // Debug.
              // Log.d(TAG, "startSendFileContentForLarge, file sent. Closing data socket: " + data_socket); // Debug.
              data_socket.close(); // Close the connection.
              data_socket=null; // Forget the data socket.
            } // public void onCompleted(Exception ex)
          });
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      } //if (fileToSend.exist()) // 文件存在
      else // Not exist
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send : " + fileToSend); // Debug.
        notifyFileNotExist(); // 报告文件不存在。
      } // else // Not exist
    } //private void startSendFileContentForLarge()
    
    /**
    * 发送文件内容。
    */
    public void sendFileContent(String data51, String currentWorkingDirectory) 
    {
      wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
      wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
//       File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // 照片目录。

      fileToSend=photoDirecotry; // 记录，要发送的文件对象。
      Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send : " + fileToSend + ", uri: " + fileToSend.getUri().toString()); // Debug.
        
      if (data_socket!=null) // 数据连接存在。
      {
        startSendFileContentForLarge(); // 开始发送文件内容。
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
    * Notify the file send completed after a short delay.
    */
    private void delayednotifyFileSendCompleted()
    {
      controlConnectHandler.delayednotifyFileSendCompleted(); // Delay and notify the file send completed.
    } // private void delayednotifyFileSendCompleted()
    
    /**
    * Notify the file send started.
    */
    private void notifyFileSendStarted()
    {
      controlConnectHandler.notifyFileSendStarted(wholeDirecotoryPath); // Notify that the file send started.
    } // private void notifyFileSendStarted()
    
    /**
    * Notify file not exist
    */
    private void notifyFileNotExist()
    {
      controlConnectHandler.notifyFileNotExist(wholeDirecotoryPath); // Notify that the file does not exist.
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
