package com.stupidbeauty.ftpserver.lib;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

public class ThumbnailSender
{
  private FilePathInterpreter filePathInterpreter=null; //!< the file path interpreter.
  private static final String TAG = "ThumbnailSender"; //!< 输出调试信息时使用的标记
  private long restSTart=0; //!< 跳过位置。
  private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
  private ControlConnectHandler controlConnectHandler=null; //!< 控制连接处理器。
  private AsyncSocket data_socket=null; //!< 当前的数据连接。
  private File rootDirectory=null; //!< 根目录。
  private DocumentFile fileToSend=null; //!< 要发送的文件。
  private Context context=null; //!< Context.
  private String wholeDirecotoryPath= ""; //!< The full path of the file to send.
  private byte[] imageBytes; //!< The bytes of the thumbanil image.
  
  /**
  * 设置重启位置。
  */
  public void setRestartPosition(long data51) 
  {
    restSTart=data51; // 记录。
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
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send : " + fileToSend + ", uri: " + fileToSend.getUri().toString()); // Debug.
        
        notifyFileSendStarted(); // Notify the file send started.
        
        // try
        {
          Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send : " + fileToSend + ", uri: " + fileToSend.getUri().toString() + ", data_socket: " + data_socket); // Debug.

	      Util.writeAll(data_socket, imageBytes, new CompletedCallback() 
          //Util.pump(is, data_socket, new CompletedCallback()
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
                    
              delayednotifyFileSendCompleted(); // Notify the file send completed after a short delay.

              fileToSend = null; // 将要发送的文件对象清空。

              data_socket.close(); // Close the connection.
              data_socket=null; // Forget the data socket.
            } // public void onCompleted(Exception ex)
          });
        }
      } //if (fileToSend.exist()) // 文件存在
    } //private void startSendFileContentForLarge()
    
/**
* Calculate the inSampleSize value based on the given options, maxWidth, and maxHeight, while keeping the aspect ratio.
*/
private static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > maxHeight || width > maxWidth) {
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= maxHeight && (halfWidth / inSampleSize) >= maxWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}

    /**
    * 发送文件内容。
    */
    public void sendThumbnail(String data51, String currentWorkingDirectory, int maxWidth, int maxHeight) 
    {
      wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
      wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
//       File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // 照片目录。

      fileToSend = photoDirecotry; // 记录，要发送的文件对象。
      Log.d(TAG, CodePosition.newInstance().toString()+  ", file to send : " + fileToSend + ", uri: " + fileToSend.getUri().toString()); // Debug.


	  // Load the content of 'fileToSend' in a bitmap, with width and height limited by maxWidth and maxHeight
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;

      try {
          InputStream is = context.getContentResolver().openInputStream(fileToSend.getUri());
          if (is == null) {
              throw new FileNotFoundException("Failed to open input stream for file: " + fileToSend.getUri());
          }
      
      // Calculate inSampleSize
      options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
      
      // Decode bitmap with inSampleSize set
      options.inJustDecodeBounds = false;
          Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
	  // Export the content of 'bitmap' as jpeg data into a byte array
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Quality set to maximum (100)
      imageBytes = baos.toByteArray();
      } catch (FileNotFoundException e) {
          Log.e(TAG, "Failed to find file: ", e);
          throw new RuntimeException("Failed to send thumbnail", e);
      }

	  // 陈欣。


        
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
