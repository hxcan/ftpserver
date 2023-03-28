package com.stupidbeauty.ftpserver.lib;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
// import android.os.LocaleList;
import android.os.PowerManager;
import 	android.provider.DocumentsContract;
// import java.util.Locale;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.io.IOException;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import org.apache.commons.io.FileUtils;
import com.koushikdutta.async.callback.ConnectCallback;
import java.net.InetSocketAddress;
import android.text.format.Formatter;
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.os.Environment;

public class DocumentTreeBrowseRequest
{
  private String passWord=null; //!< Pass word provided.
  private boolean authenticated=true; //!< Is Login correct?
  private String userName=null; //!< User name provided.
  private UserManager userManager=null; //!< user manager.
  private BinaryStringSender binaryStringSender=new BinaryStringSender(); //!< 以二进制方式发送字符串的工具。
  private EventListener eventListener=null; //!< 事件监听器。
  private AsyncSocket socket; //!< 当前的客户端连接。
  private static final String TAG ="DocumentTreeBrowseRequest"; //!<  输出调试信息时使用的标记。
  private int requestCode; //!< The request code.
  private AsyncSocket data_socket; //!< 当前的数据连接。
  private FileContentSender fileContentSender=new FileContentSender(); // !< 文件内容发送器。
  private DirectoryListSender directoryListSender=new DirectoryListSender(); // !< 目录列表发送器。
  private Intent intent=null; //!< The intent to launch.
  private String currentWorkingDirectory="/"; //!< 当前工作目录
  private int data_port=1544; //!< 数据连接端口。
  private String ip; //!< ip
  private boolean allowActiveMode=true; //!< 是否允许主动模式。
  private File writingFile; //!< 当前正在写入的文件。
  private boolean isUploading=false; //!< 是否正在上传。陈欣
  private InetAddress host;
  private File rootDirectory=null; //!< 根目录。

  /**
  * SEt user manager.
  */
  public void setUserManager(UserManager userManager)
  { 
    this.userManager=userManager;
  } // public void setUserManager(UserManager userManager)
  
  public void setEventListener(EventListener eventListener)
  {
    this.eventListener=eventListener;
  } //eventListener
    
  public void setRootDirectory(File root)
  {
    rootDirectory=root;
    Log.d(TAG, "setRootDirectory, rootDirectory: " + rootDirectory); // Debug.
        
    fileContentSender.setRootDirectory(rootDirectory); // 设置根目录。
    directoryListSender.setRootDirectory(rootDirectory); // 设置根目录。
  }

  /**
  * 从数据套接字处接收数据。陈欣
  */
  private void receiveDataSocket( ByteBufferList bb)
  {
    byte[] content=bb.getAllByteArray(); // 读取全部内容。

    boolean appendTrue=true;

    try
    {
      FileUtils.writeByteArrayToFile(writingFile, content, appendTrue); // 写入。
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  } // private void                         receiveDataSocket( ByteBufferList bb)

  /**
  * Default constructor.
  */
  public DocumentTreeBrowseRequest()
  {
    this.allowActiveMode=allowActiveMode;
    this.host=host;
    this.ip=ip; // Remember ip for data server.
  } // public DocumentTreeBrowseRequest(Context context, boolean allowActiveMode, InetAddress host, String ip)
    
    /**
    * 打开指向客户端特定端口的连接。
    */
    public void setIntent(Intent content)
    {
      intent=content; // Remember intent.
    } //private void openDataConnectionToClient(String content)

    public void notifyFileNotExist() // 告知文件不存在
    {
      String replyString="550 File not exist"; // File does not exist.

      Log.d(TAG, "reply string: " + replyString); //Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送。
    } //private void notifyFileNotExist()

    /**
     * 告知已经发送目录数据。
     */
    public void notifyLsCompleted()
    {
      String replyString="226 Data transmission OK. ChenXin"; // 回复内容。
      
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

      Log.d(TAG, "reply string: " + replyString); //Debug.
    } //private void notifyLsCompleted()
    
    /**
    * Process pass command.
    */
    private void processPassCommand(String targetWorkingDirectory) 
    {
      this.passWord=targetWorkingDirectory; // Remember password.
      
      if (userManager!=null)
      {
        authenticated=userManager.authenticate(userName, passWord); // Authenticate.
      } // if (userManager!=null)
      
      if (authenticated) // Login correct
      {
        binaryStringSender.sendStringInBinaryMode("230 Loged in."); // 回复，登录成功。
      } // if (authenticated) // Login correct
      else // Login not correct
      {
        binaryStringSender.sendStringInBinaryMode("430 Invalid username or password."); // 回复，登录成功。
      }
    } // private void processPassCommand(String targetWorkingDirectory)

    /**
    * Process user command.
    */
    private void processUserCommand(String userName)
    {
      this.userName=userName; // Remember user name.
    
      binaryStringSender.sendStringInBinaryMode("331 Send password"); //  发送回复。
    } // private void processUserCommand(String userName)

    /**
    * 处理尺寸查询命令。
    */
    public Intent getIntent()
    {
      return intent;
    } //private void processSizeCommand(String data51)

    /**
    * Report event.
    */
    public int getRequestCode()
    {   
      return requestCode;
    } //private void notifyEvent(String eventCode)

    /**
     * Accept data connection.
     * @param socket 连接对象。
     */
    public void setRequestCode(int socket)
    {
      this.requestCode=socket; // Remember request code
    } //private void handleDataAccept(final AsyncSocket socket)
}
