package com.stupidbeauty.ftpserver.lib;

import 	java.util.Timer;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import com.stupidbeauty.codeposition.CodePosition;
import android.os.ParcelFileDescriptor;
import java.io.FileOutputStream;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import 	android.provider.DocumentsContract;
import java.util.Locale;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.io.IOException;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import java.nio.ByteBuffer;
import com.koushikdutta.async.DataEmitter;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.content.Context;
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
import android.net.wifi.WifiManager;
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.os.Environment;

/**
* The handler of control connection.
*/
public class ControlConnectHandler implements DataServerManagerInterface
{
  private FilePathInterpreter filePathInterpreter=null; //!< the file path interpreter.
  private String passWord=null; //!< Pass word provided.
  private boolean authenticated=true; //!< Is Login correct?
  private String userName=null; //!< User name provided.
  private UserManager userManager=null; //!< user manager.
  private BinaryStringSender binaryStringSender=new BinaryStringSender(); //!< 以二进制方式发送字符串的工具。
  private EventListener eventListener=null; //!< 事件监听器。
  private ErrorListener errorListener=null; //!< Error listener. Chen xin. 
  private AsyncSocket socket; //!< 当前的客户端连接。
  private static final String TAG ="ControlConnectHandler"; //!<  输出调试信息时使用的标记。
  private Context context; //!< 执行时使用的上下文。
  private AsyncSocket data_socket; //!< 当前的数据连接。
  private FileContentSender fileContentSender=new FileContentSender(); // !< 文件内容发送器。
  private ThumbnailSender thumbnailSender = new ThumbnailSender(); // !< Thumbnail sender.
  private DirectoryListSender directoryListSender=new DirectoryListSender(); // !< 目录列表发送器。
  private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
  private String currentWorkingDirectory="/"; //!< 当前工作目录
  private int data_port=1544; //!< 数据连接端口。
  private String ip; //!< ip
  private String clientIp;
  private int clientDataPort; //!< Client data port to connect to.
  private int retryConnectClientDataPortAmount=0; //!< the time retried for connecting client data port.
  private boolean allowActiveMode=true; //!< Whether to allow active mode.
  private DisconnectIntervalManager disconnectIntervalManager=new DisconnectIntervalManager(); //!< Disconnect interval manager
  private DataServerManager dataServerManager=new DataServerManager(); //!< The data server manager.
  private Timer disconnectTimer=null; //!< The timer of automatically disconnect from possible stuck connections.
  
  private DocumentFile writingFile; //!< 当前正在写入的文件。
  private ParcelFileDescriptor pfd = null;
  private FileOutputStream fileOutputStream = null;
  private long totalWritten = 0;         // 用于速度统计
  private long lastLogTime = 0;          // 用于速度统计

  private DocumentFile renamingFile; //!< The file being renamed.
  private boolean isUploading=false; //!< 是否正在上传。陈欣
  private InetAddress host;
  private File rootDirectory=null; //!< 根目录。

  /**
  * 是否启用 Dolphin bug #474238 的绕过方案。
  */
  private boolean enableDolphinBug474238Placeholder = false;

  public void setEnableDolphinBug474238Placeholder(boolean enable)
  {
    this.enableDolphinBug474238Placeholder = enable;

    directoryListSender.setEnableDolphinBug474238Placeholder(enable);
  }

  public boolean isEnableDolphinBug474238Placeholder()
  {
    return enableDolphinBug474238Placeholder;
  }

  /**
  * 将一个完整路径拆分为父路径和最后的目录名。
  * @param fullPath 完整路径
  * @return String[] { parentPath, dirName }
  */
  public static String[] splitPath(String fullPath) {
    if (fullPath == null || fullPath.isEmpty()) {
        return new String[]{"", ""};
    }

    fullPath = fullPath.trim();

    boolean isAbsolute = fullPath.startsWith("/");
    String normalizedPath;

    if (isAbsolute) {
        normalizedPath = fullPath.replaceAll("/+", "/"); // 合并多个斜杠为单个
    } else {
        normalizedPath = fullPath.replaceAll("/+", "/");
    }

    int lastSlashIndex = normalizedPath.lastIndexOf('/');

    String parentPath;
    String dirName;

    if (isAbsolute) {
        if (lastSlashIndex <= 0) {
            // 如 "/abc"
            parentPath = "/";
            dirName = normalizedPath.substring(1);
        } else {
            parentPath = normalizedPath.substring(0, lastSlashIndex);
            dirName = normalizedPath.substring(lastSlashIndex + 1);
        }
    } else {
        // 相对路径
        if (lastSlashIndex == -1) {
            // 只有一个目录名，如 "newdir"
            parentPath = "";
            dirName = normalizedPath;
        } else {
            // 包含层级的相对路径，如 "a/b/c"
            parentPath = normalizedPath.substring(0, lastSlashIndex);
            dirName = normalizedPath.substring(lastSlashIndex + 1);
        }
    }

    return new String[]{parentPath, dirName};
  }
  
  /**
  * Set the user manager.
  */
  public void setUserManager(UserManager userManager)
  { 
    this.userManager=userManager;
  } // public void setUserManager(UserManager userManager)
  
  /**
  * Set the file path interpreter.
  */
  public void setFilePathInterpreter(FilePathInterpreter filePathInterpreter)
  {
    this.filePathInterpreter=filePathInterpreter;
    
    directoryListSender.setFilePathInterpreter(filePathInterpreter);
    fileContentSender.setFilePathInterpreter(filePathInterpreter); // SEt the file path interpreter.
    thumbnailSender.setFilePathInterpreter(filePathInterpreter); // SEt the file path interpreter.
    
    this.filePathInterpreter.setContext(context); // Set context.
  } // public void setFilePathInterpreter(FilePathInterpreter filePathInterpreter)
  
  public void setErrorListener(ErrorListener errorListener)    
  {
    this.errorListener = errorListener;
  } //public void setErrorListener(ErrorListener errorListener)    

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
    thumbnailSender.setRootDirectory(rootDirectory); // Set the root directory.
  } // public void setRootDirectory(File root)

  /**
  * File name tolerant. For example: /Android/data/com.client.xrxs.com.xrxsapp/files/XrxsSignRecordLog/Zw40VlOyfctCQCiKL_63sg==, with a trailing <LF> (%0A).
  */
  public void setFileNameTolerant(boolean toleranttrue)
  {
    directoryListSender.setFileNameTolerant(toleranttrue);
  } // public void setFileNameTolerant(boolean toleranttrue)
  
  /**
  * 从数据套接字处接收数据。陈欣
  * ✅ 优化版：使用持久化 FileOutputStream，避免频繁 open/close
  * ✅ 每秒输出上传速度，便于诊断性能瓶颈
  * ✅ 保留原有 CodePosition 日志风格
  */
  private void receiveDataSocket(ByteBufferList bb)
  {
    // ✅ 检查文件是否已打开
    if (fileOutputStream == null) {
      Log.d(TAG, CodePosition.newInstance().toString() +  ", ⚠️ No file open, dropping data. writingFile=" + 
            (writingFile != null ? writingFile.getUri().toString() : "null") ); // Debug.
      return;
    }

    try {
      // ✅ 避免 getAllByteArray()，直接遍历 ByteBufferList
      while (bb.size() > 0) {
        ByteBuffer buffer = bb.remove();
        byte[] array = buffer.array();
        int len = buffer.remaining();
        fileOutputStream.write(array, 0, len);
        totalWritten += len;
      }

      // ✅ 每秒输出一次上传速度（保留你的日志风格）
      long now = System.currentTimeMillis();
      if (now - lastLogTime >= 1000) {
        double speedKBps = (totalWritten * 1000.0) / (now - lastLogTime) / 1024;
        Log.d(TAG, CodePosition.newInstance().toString() + 
              ", 📤 UPLOAD SPEED: " + String.format("%.1f", speedKBps) + " KiB/s" +
              ", file=" + (writingFile != null ? writingFile.getName() : "unknown") +
              ", total=" + (totalWritten / 1024) + " KiB" ); // Debug.
        lastLogTime = now;
        totalWritten = 0;
      }

    } catch (IOException e) {
      e.printStackTrace();
      Log.d(TAG, CodePosition.newInstance().toString() +  ", ❌ Write failed: " + e.getMessage() ); // Debug.
      finishFileWrite(); // 出错也要关闭资源
      // notifyStorFailed(e);
    }
  } // private void receiveDataSocket(ByteBufferList bb)

  public ControlConnectHandler(Context context, boolean allowActiveMode, InetAddress host, String ip)
  {
    this.context=context;
    this.allowActiveMode=allowActiveMode;
    this.host=host;
    this.ip=ip; // Remember ip for data server.

    fileContentSender.setContext(context); // Set the context.
    thumbnailSender.setContext(context); // Set the context.
  } // public ControlConnectHandler(Context context, boolean allowActiveMode, InetAddress host, String ip)
  
  /**
  * Connect to client data port.
  */
  private void connectToClientDataPort() 
  {
    String ip=clientIp;
    int port=clientDataPort;
    
    Log.d(TAG, CodePosition.newInstance().toString()+  ", connecting to port specified by client: " + port  + ", this: " + this); // Debug.

    AsyncServer.getDefault().connectSocket(new InetSocketAddress(ip, port), new ConnectCallback() 
    {
      @Override
      public void onConnectCompleted(Exception ex, final AsyncSocket socket) 
      {
        handleConnectCompleted(ex, socket);
      } // public void onConnectCompleted(Exception ex, final AsyncSocket socket) 
    }); // AsyncServer.getDefault().connectSocket(new InetSocketAddress(ip, port), new ConnectCallback() 
    
    
  } // private void connectToClientDataPort()
    
  /**
  * 打开指向客户端特定端口的连接。
  */
  private void openDataConnectionToClient(String content)
  {
    String portString=content.split(" ")[1].trim(); // 端口字符串。

    String[] addressStringList= portString.split(","); //获取地址字符串。

    String ip=addressStringList[0]+"."+addressStringList[1]+"."+addressStringList[2]+"."+addressStringList[3]; // 构造IP。陈欣
    int port=Integer.parseInt(addressStringList[4])*256+Integer.parseInt(addressStringList[5]); // 计算出端口号。
    Log.d(TAG, CodePosition.newInstance().toString()+  ", connecting to port specified by client: " + port  + ", this: " + this); // Debug.

    clientIp=ip;
    clientDataPort=port;

    // Make the connection:

    retryConnectClientDataPortAmount=0; // reset the retry times.
    connectToClientDataPort(); // Connect to client data port.
  } //private void openDataConnectionToClient(String content)

    /**
    * Notify the file send started.
    */
    public void notifyFileSendStarted(String filePath)
    {
      String replyString="150 start send content: " + filePath ; // The reply string.

      Log.d(TAG, "reply string: " + replyString); //Debug.

      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

      // controlConnectHandler.notifyFileSendStarted(wholeDirecotoryPath); // Notify that the file send started.
    } // private void notifyFileSendStarted()
    
    /**
    * Notify file not exist.
    */
    public void notifyFileNotExist(String filePath)
    {
      String replyString="550 File not exist " + filePath; // File does not exist.

      // Log.d(TAG, "reply string: " + replyString); //Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString  + ", this: " + this); // Debug.

      binaryStringSender.sendStringInBinaryMode(replyString); // 发送。
    } // private void notifyFileNotExist()
    
    /**
    * Cancel the disconnect tiemr.
    */
    private void cancelDisconnectTimer() 
    {
      if (disconnectTimer!=null) // The disconnect timer exists
      {
        disconnectTimer.cancel(); // Cancel the timer.
      } // if (disconnectTimer!=null) // The disconnect timer exists
      
    } // private void cancelDisconnectTimer()
    
    /**
    * Schedule disconnect.
    */
    private void scheduleDisconnect() // Schedule disconnect.
    {
      Timer timerObj = new Timer();
      
      cancelDisconnectTimer(); // Cancel the disconnect tiemr.
      
      disconnectTimer=timerObj; // Remember timer.
      
      TimerTask timerTaskObj = new TimerTask() 
      {
        public void run() 
        {
          // notifyFileSendCompleted(); // Notify file send completed.
          // Chen xin.
          socket.close(); // close the connection.
        }
      };
      
      long suggestedInterfal20=disconnectIntervalManager.getSuggestedDisconnectInterval(); // Get suggested disconnect interval.
      
      timerObj.schedule(timerTaskObj, suggestedInterfal20); // delay and run.

      disconnectIntervalManager.markScheduleDisconnect(); // mark scheduled disconnect.
    } // private void scheduleDisconnect()
    
    /**
    * Delay and notify the file send completed.
    */
    public void delayednotifyFileSendCompleted()
    {
      // Chen xin.
      Timer timerObj = new Timer();
      TimerTask timerTaskObj = new TimerTask() 
      {
        public void run() 
        {
          notifyFileSendCompleted(); // Notify file send completed.
        }
      };
      timerObj.schedule(timerTaskObj, 20); // delay and run.
    } // public void delayednotifyFileSendCompleted()

    /**
    * 告知已经发送文件内容数据。
    */
    public void notifyFileSendCompleted() 
    {
      String replyString="226 File sent. " + "ChenXin" + " 嘴巴上挂着价签吗" + " 并不好吃，感觉它本身的味道没调好" + " 你还是去闻熏村那种"; // The reply message.

      Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString  + ", this: " + this); // Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送。
      
      scheduleDisconnect(); // Schedule disconnect.
      
      notifyEvent(EventListener.DOWNLOAD_FINISH); // Notify event, file download finished.
    } // private void notifyFileSendCompleted()

    /**
    * 发送文件内容。
    */
    private void sendFileContent(String data51, String currentWorkingDirectory) 
    {
      fileContentSender.setControlConnectHandler(this); // 设置控制连接处理器。
      fileContentSender.setDataSocket(data_socket); // 设置数据连接套接字。
      fileContentSender.sendFileContent(data51, currentWorkingDirectory); // 让文件内容发送器来发送。
      
      notifyEvent(EventListener.DOWNLOAD_START); // 报告事件，开始下载文件。
    } //private void sendFileContent(String data51, String currentWorkingDirectory)
    
    /**
    * Send directory list content.
    */
    private void sendListContentBySender(String fileName, String currentWorkingDirectory, boolean extraInformation)
    {
      directoryListSender.setControlConnectHandler(this); // 设置控制连接处理器。

      directoryListSender.setDataSocket(data_socket); // 设置数据连接套接字。
      directoryListSender.setExtraInformationEnabled(extraInformation); // Set the option of sending extra inforamtion.
      directoryListSender.sendDirectoryList(fileName, currentWorkingDirectory); // 让目录列表发送器来发送。
    } // private void sendListContentBySender(String fileName, String currentWorkingDirectory, boolean extraInformation) 
    
    /**
    * Send directory list content.
    */
    private void sendListContentBySender(String fileName, String currentWorkingDirectory) 
    {
      boolean extraInformation = true; // Send extra informations.
      sendListContentBySender(fileName, currentWorkingDirectory, extraInformation) ;
    } // private void sendListContentBySender(String fileName, String currentWorkingDirectory)

    /**
    * 告知上传完成。
    */
    private void notifyStorCompleted() 
    {
      // if (writingFile!=null)
      String replyString="226 Stor completed."; // 回复内容。

      Log.d(TAG, "reply string: " + replyString); //Debug.

      binaryStringSender.sendStringInBinaryMode(replyString);
      
      notifyEvent(EventListener.UPLOAD_FINISH, (Object)(writingFile)); // Notify event, uplaod finished.
    } //private void notifyStorCompleted()
    
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
    * Process quit command.
    */
    private void processQuitCommand()
    {
      String replyString="221 Quit OK. ChenXin"; // The reply string.
      
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

      Log.d(TAG, "reply string: " + replyString); //Debug.
    } // private void processQuitCommand()

/**
* Handle the command thmb.
*/
private void processThmbCommand(String data51) {
  String[] parts = data51.split(" ");
  if (parts.length < 3) {
    String replyString = "501 Syntax error in parameters or arguments.";
    binaryStringSender.sendStringInBinaryMode(replyString);
    return;
  }
  
  // Extract max-width, max-height, and pathname from parts array
  String maxWidthStr = parts[1];
  String maxHeightStr = parts[2];
  
  int maxWidth = Integer.parseInt(maxWidthStr);
  int maxHeight = Integer.parseInt(maxHeightStr);
  
  sendThumbnail(data51, currentWorkingDirectory, maxWidth, maxHeight); // Use the same method as for file retrieval.
} // private void processThmbCommand(String data51)


/**
* Generate thumbnail and send it.
*/
private void sendThumbnail(String pathname, String currentWorkingDirectory, int maxWidth, int maxHeight) {
      thumbnailSender.setControlConnectHandler(this); // 设置控制连接处理器。
      thumbnailSender.setDataSocket(data_socket); // 设置数据连接套接字。
      thumbnailSender.sendThumbnail(pathname, currentWorkingDirectory, maxWidth, maxHeight); // Adding width and height parameters.
      
} // private void sendThumbnail(String pathname, String currentWorkingDirectory, int maxWidth, int maxHeight)

    /**
    * Process the retr command.
    */
    private void processRetrCommand(String data51)
    {
      sendFileContent(data51, currentWorkingDirectory); // Send file content.
    } // private void processRetrCommand(String data51)
    
    /**
    *  处理上传文件命令。
    */
    private void processStorCommand(String data51)
    {
      String replyString="150 "; // 回复内容。

      boolean storStartResult = startStor(data51, currentWorkingDirectory); // Start stor process.
      
      if (storStartResult) // Start stor successfully
      {
      } // if (storStartResult) // Start stor successfully
      else // Failed to start stor
      {
        // replyString="150 "; // 回复内容。
        replyString="550 it is a directory: " + data51; // The reply content. Do not allow to replace a directory with a normal file.
      } // else // Failed to start stor

      binaryStringSender.sendStringInBinaryMode(replyString);
    } // private void processStorCommand(String data51)

    /**
    * 上传文件内容。
    */
    private boolean startStor(String data51, String currentWorkingDirectory) 
    {
      boolean result = true; // Stor start result.
      
      DocumentFile photoDirecotry = filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // Resolve file path.

      // writingFile = photoDirecotry; // 先不赋值，等 createFile 后再赋
      isUploading = true; // 记录，处于上传状态。
      Log.d(TAG, CodePosition.newInstance().toString() +  ", startStor: target path=" + data51); // Debug.

      if (photoDirecotry != null && photoDirecotry.exists()) // The file exists
      {
        if (photoDirecotry.isDirectory()) // It is an existing directory
        {
          result = false;
          Log.d(TAG, CodePosition.newInstance().toString() +  ", STOR failed: target is a directory: " + data51); // Debug.
        } //  if (photoDirecotry.isDirectory()) // It is an existing directory
        else // It is a normal file.
        {
          photoDirecotry.delete();
          Log.d(TAG, CodePosition.newInstance().toString() +  ", Deleted existing file: " + photoDirecotry.getUri().toString() ); // Debug.
        } // else // It is a normal file.
      } // if (photoDirecotry.exists()) // The file exists

      if (result) // We can proceed so far
      {
        try // Create the file.
        {
          Log.d(TAG, CodePosition.newInstance().toString() +  ", Creating new file for STOR: " + data51 ); // Debug.
          File virtualFile = new File(data51);
          File parentVirtualFile = virtualFile.getParentFile();
          String currentTryingPath = parentVirtualFile.getPath();
          DocumentFile parentDocumentFile = filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, currentTryingPath);
          String fileNameOnly = virtualFile.getName();

          writingFile = parentDocumentFile.createFile("", fileNameOnly); // Creat eh file.
          Log.d(TAG, CodePosition.newInstance().toString() +  ", Created new file: " + writingFile.getUri().toString() ); // Debug.

          if (writingFile == null) {
            Log.d(TAG, CodePosition.newInstance().toString() +  ", ❌ createFile returned null!"); // Debug.
            result = false;
          } else {
            // ✅ 打开文件句柄
            Uri uri = writingFile.getUri();
            try {
              pfd = context.getContentResolver().openFileDescriptor(uri, "w");
              if (pfd != null) {
                fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                totalWritten = 0;
                lastLogTime = System.currentTimeMillis();
                Log.d(TAG, CodePosition.newInstance().toString() +  ", ✅ File opened for write: " + writingFile.getUri().toString() ); // Debug.
              } else {
                Log.d(TAG, CodePosition.newInstance().toString() +  ", ❌ openFileDescriptor returned null!"); // Debug.
                result = false;
              }
            } catch (Exception e) {
              Log.d(TAG, CodePosition.newInstance().toString() +  ", ❌ Exception opening file descriptor: " + e.getMessage() ); // Debug.
              e.printStackTrace();
              result = false;
            }
          }
        } // try // Create the file.
        catch (Exception e) // Catch any exception.
        {
          e.printStackTrace();
          Log.d(TAG, CodePosition.newInstance().toString() +  ", ❌ Exception during startStor: " + e.getMessage() ); // Debug.
          result = false;
        } // catch (Exception e) // Catch any exception.
      } // if (result) // We can proceed so far
      
      Log.d(TAG, CodePosition.newInstance().toString() +  ", startStor result: " + result + ", writingFile=" + (writingFile != null ? writingFile.getUri().toString() : "null") ); // Debug.
      return result;
    } // private boolean startStor
    
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
    * Process feat command.
    */
    private void processFeatCommand()
    {
      binaryStringSender.sendStringInBinaryMode("211-Feature list"); //  Start feature list.
      binaryStringSender.sendStringInBinaryMode(" UTF8"); //  support utf8
      binaryStringSender.sendStringInBinaryMode(" AVBL"); //  support avbl. available space.
      binaryStringSender.sendStringInBinaryMode(" THMB JPEG|PNG"); //  support thmb. thumbnail
      binaryStringSender.sendStringInBinaryMode("211 end"); //  end feature list
    } // private void processFeatCommand()
    
    /**
    * Process user command.
    */
    private void processUserCommand(String userName)
    {
      this.userName=userName; // Remember user name.
    
      binaryStringSender.sendStringInBinaryMode("331 Send password"); //  发送回复。
    } // private void processUserCommand(String userName)

    /**
    * 处理改变目录命令。
    */
    private void processCwdCommand(String targetWorkingDirectory) 
    {
//       FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, targetWorkingDirectory); // 照片目录。
//       File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, targetWorkingDirectory); // 照片目录。

      String replyString="" ; // 回复内容。
//       String fullPath="";
      String fullPath=filePathInterpreter.resolveWholeDirectoryPath( rootDirectory, currentWorkingDirectory, targetWorkingDirectory); // resolve 完整路径。

      if (photoDirecotry!=null) // The object exists
      {
        if (photoDirecotry.isDirectory()) // It is a directory. 07-07 09:51:11.419 21116 21153 E AndroidRuntime: java.lang.NullPointerException: Attempt to invoke virtual method 'boolean androidx.documentfile.provider.DocumentFile.isDirectory()' on a null object reference
        {
          String rootPath=rootDirectory.getPath(); // 获取根目录的完整路径。
          
          currentWorkingDirectory=fullPath.substring(rootPath.length()); // 去掉开头的根目录路径。
          
          if (currentWorkingDirectory.isEmpty()) // 是空白的了
          {
            currentWorkingDirectory="/"; // 当前工作目录是根目录。
          } // if (currentWorkingDirectory.isEmpty()) // 是空白的了
          
          Log.d(TAG, CodePosition.newInstance().toString()+  ", fullPath: " + fullPath ); // Debug.
          Log.d(TAG, "processCwdCommand, rootPath: " + rootPath ); // Debug.
          Log.d(TAG, "processCwdCommand, currentWorkingDirectory: " + currentWorkingDirectory ); // Debug.

          replyString="250 cwd succeed" ; // 回复内容。
        } //if (photoDirecotry.isDirectory()) // 是个目录
        else //不是个目录
        {
          replyString="550 not a directory: " + targetWorkingDirectory; // 回复内容。
        }
      } // if (photoDirecotry!=null) // The object exists
      else // The object does not exist
      {
        replyString="550 File not exist " + targetWorkingDirectory; // File does not exist.
      } // else // The object does not exist

      Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString); //Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
      
      if (filePathInterpreter.isSamePath (fullPath, Constants.FilePath.AndroidData)) // It is /Android/data, same path.
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", full path : " + fullPath + ", other path: " + Constants.FilePath.AndroidData + ", checking /Android/data permission"); // Debug.
        CheckAndroidDataPermission(); // Check /Android/data permission.
      } // if (currentWorkingDirectory.equals(Constants.FilePath.AndroidData)) // It is /Android/data
    } // private void processCwdCommand(String targetWorkingDirectory)
    
    /**
    * Process the avbl command.
    */
    private void processAvblCommand()
    {
      // Chen xin.
            Log.d(TAG, "processAvblCommand: filesdir: " + rootDirectory.getPath()); // Debug.

      // Log.d(TAG, "processAvblCommand: data51: " + data51); // Debug.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
    
      // DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve file path.

      String replyString=""; // 回复字符串。
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.

      // if  ((photoDirecotry!=null) && (photoDirecotry.exists() && (photoDirecotry.isFile()))) // The path exists. And it is a file.
      {
        // long fileSize= photoDirecotry.length(); //文件尺寸。 陈欣
        
        AvblManager avblManager = new AvblManager(context); // Create the avbl manager.
        long fileSize= avblManager.getAvbl(); // Get the avbl information.
        
        
        
        // Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
            
        replyString="213 " + fileSize + " "; // 文件尺寸。
      } //if (photoDirecotry.exists()) // 文件存在
      // else // Not an existing file
      // {
      //   Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51 + ", file object: " + photoDirecotry); // Debug.
      //   if ((photoDirecotry==null) || (!photoDirecotry.exists())) // not exist
      //   {
      //     // Chen xin.
      //     replyString="550 File not exist " + data51; // File does not exist.
      //     Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
      //     // replyString="550 No directory traversal allowed in SIZE param"; // File does not exist.
      //   } // if ((photoDirecotry==null) || (!photoDirecotry.exists())) // not exist
      //   else // Directory
      //   {
      //     Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
      //     replyString="550 No directory traversal allowed in SIZE param"; // File does not exist.
      //   } // else // Directory
      // } //else // 文件不 存在

      // Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51 + ", reply content: " + replyString); // Debug.
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

    } // private void processAvblCommand()

    /**
    * 处理尺寸查询命令。
    */
    private void processSizeCommand(String data51)
    {
      Log.d(TAG, "processSizeCommand: filesdir: " + rootDirectory.getPath()); // Debug.

      Log.d(TAG, "processSizeCommand: data51: " + data51); // Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
    
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve file path.

      String replyString=""; // 回复字符串。
      Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.

      if  ((photoDirecotry!=null) && (photoDirecotry.exists() && (photoDirecotry.isFile()))) // The path exists. And it is a file.
      {
        long fileSize= photoDirecotry.length(); //文件尺寸。 陈欣
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
            
        replyString="213 " + fileSize + " "; // 文件尺寸。
      } //if (photoDirecotry.exists()) // 文件存在
      else // Not an existing file
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51 + ", file object: " + photoDirecotry); // Debug.
        if ((photoDirecotry==null) || (!photoDirecotry.exists())) // not exist
        {
          // Chen xin.
          replyString="550 File not exist " + data51; // File does not exist.
          Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
          // replyString="550 No directory traversal allowed in SIZE param"; // File does not exist.
        } // if ((photoDirecotry==null) || (!photoDirecotry.exists())) // not exist
        else // Directory
        {
          Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51); // Debug.
          replyString="550 No directory traversal allowed in SIZE param"; // File does not exist.
        } // else // Directory
      } //else // 文件不 存在

      Log.d(TAG, CodePosition.newInstance().toString()+  ", file name: " + data51 + ", reply content: " + replyString); // Debug.
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
    } //private void processSizeCommand(String data51)
    
    /**
    * Procee the rnto command
    */
    private void processRntoCommand(String data51)
    {
      DocumentFile photoDirecotry = renamingFile; // resolve file
        
      String replyString="250 "; // 回复内容。

      if (photoDirecotry!=null) // The documentfile object exists
      {
        String originalName = photoDirecotry.getName(); // Get the original name.
        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory + originalName; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                  
        {
          File virtualFile=new File(data51);
          

          String fileNameOnly=virtualFile.getName(); // Get the file name.


          boolean renameResult = photoDirecotry.renameTo(fileNameOnly); // Try to rename.
          Log.d(TAG, CodePosition.newInstance().toString()+  ", target file name to rename: " + data51 + ", lnegth: " + data51.length() + ", rename result: " + renameResult); // Debug.
          
          if (renameResult) // Success
          {
            // notifyEvent(EventListener.DELETE); // 报告事件，删除文件。
            // notifyEvent(EventListener.RENAME, (Object)(photoDirecotry)); // Notify event, rename file.
            RenameInformationObject renameInformationObjecttry = new RenameInformationObject(); // Creathe the reuname information object.
            renameInformationObjecttry.setFile(photoDirecotry); // SEt the file object.
            renameInformationObjecttry.setOriginalName(originalName); // SEt the original name.
            
            notifyEvent(EventListener.RENAME, (Object)(renameInformationObjecttry)); // Notify event, rename file.
          
            replyString="250 Requested file action okay, completed. " + data51; // Reply, delete success.
            
            PathDocumentFileCacheManager pathDocumentFileCacheManager = filePathInterpreter.getPathDocumentFileCacheManager(); // Get the path documetnfile cache manager.

            String effectiveVirtualPathForCurrentSegment=wholeDirecotoryPath; // Remember effective virtual path.
            effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment.replace("//", "/"); // Remove consecutive /
          
            pathDocumentFileCacheManager.remove(effectiveVirtualPathForCurrentSegment); // Remove it from the cache.
          } // if (renameResult) // Success
          else // rename failed
          {
            replyString="550 File rename failed " + data51; // File delete failed.
          } // else // rename failed
          
          
          // Chen xin. remove cache DocumentFile.
          
          // Chen xin
          
          // renamingFile = photoDirecotry; // Remember the renaming file.
          
        } // if (deleteResult) // Delete success
      } // if (photoDirecotry!=null) // The documentfile object exists
      else // The doucmentfile object does not exist
      {
        replyString="550 File rename failed " + data51; // File delete failed.
      } // else // The doucmentfile object does not exist

      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
    } // private void processRntoCommand(String data51)
    
    /**
    * Procee the rnfr command
    */
    private void processRnfrCommand(String data51)
    {
      String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                  
      wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                  
      DocumentFile photoDirecotry = filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve file
        
      String replyString="350 "; // 回复内容。

      if (photoDirecotry!=null) // The documentfile object exists
      {
        {
          // notifyEvent(EventListener.DELETE); // 报告事件，删除文件。
          // notifyEvent(EventListener.DELETE, (Object)(photoDirecotry)); // Notify event, delete file.

          replyString="350 Requested file action pending further information. " + data51; // Reply, delete success.
          
          // Chen xin. remove cache DocumentFile.
          
          // Chen xin
          
          renamingFile = photoDirecotry; // Remember the renaming file.
          
//           PathDocumentFileCacheManager pathDocumentFileCacheManager = filePathInterpreter.getPathDocumentFileCacheManager(); // Get the path documetnfile cache manager.
// 
//           String effectiveVirtualPathForCurrentSegment=wholeDirecotoryPath; // Remember effective virtual path.
//           effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment.replace("//", "/"); // Remove consecutive /
//           
//           pathDocumentFileCacheManager.remove(effectiveVirtualPathForCurrentSegment); // Remove it from the cache.
        } // if (deleteResult) // Delete success
      } // if (photoDirecotry!=null) // The documentfile object exists
      else // The doucmentfile object does not exist
      {
        replyString="550 File rename failed " + data51; // File delete failed.
      } // else // The doucmentfile object does not exist

      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
    } // private void processRnfrCommand(String data51)
    
    /**
    *  Process the dele command
    */
    private void processDeleCommand(String data51)
    {
      String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                  
      wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                  
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve file
        
      String replyString="250 "; // 回复内容。

      if (photoDirecotry!=null) // The documentfile object exists
      {
        boolean deleteResult= photoDirecotry.delete();
            
        if (deleteResult) // Delete success
        {
          // notifyEvent(EventListener.DELETE); // 报告事件，删除文件。
          notifyEvent(EventListener.DELETE, (Object)(photoDirecotry)); // Notify event, delete file.

          replyString="250 Delete success " + data51; // Reply, delete success.
          
          // Chen xin. remove cache DocumentFile.
          
          PathDocumentFileCacheManager pathDocumentFileCacheManager = filePathInterpreter.getPathDocumentFileCacheManager(); // Get the path documetnfile cache manager.

          String effectiveVirtualPathForCurrentSegment=wholeDirecotoryPath; // Remember effective virtual path.
          effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment.replace("//", "/"); // Remove consecutive /
          
          pathDocumentFileCacheManager.remove(effectiveVirtualPathForCurrentSegment); // Remove it from the cache.
        } // if (deleteResult) // Delete success
        else // Delete fail
        {
          replyString="550 File delete failed"; // File delete failed.

          checkFileManagerPermission(Constants.Permission.Write, photoDirecotry); // Check permission of write.
        } // else // Delete fail
      } // if (photoDirecotry!=null) // The documentfile object exists
      else // The doucmentfile object does not exist
      {
        replyString="550 File delete failed " + data51; // File delete failed.
      } // else // The doucmentfile object does not exist

      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
    } // private void processDeleCommand(String data51)
    
    /**
    *  process pasv command.
    */
    private void processPasvCommand()
    {
        data_socket=null; // Forget the used data socket.
        setupDataServer(); // 初始化数据服务器。

        String ipAddress = ip;


        if (ipAddress==null) // Have not set ip.
        {
          WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
          ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        } // else // Not set ip.

        String ipString = ipAddress.replace(".", ",");

        int port256=data_port/256;
        int portModule=data_port-port256*256;

        String replyString="227 Entering Passive Mode ("+ipString+","+port256+","+portModule+") "; // 回复内容。

        Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString); // Debug.

        binaryStringSender.sendStringInBinaryMode(replyString); // 回复内容。
    } // private void processPasvCommand()

    /**
     * 处理命令。
     * @param command 命令关键字
     * @param content 整个消息内容。
     */
    public void processCommand(String command, String content, boolean hasFolloingCommand)
    {
      cancelDisconnectTimer(); // Cancelt he disconnect timer.
      disconnectIntervalManager.markNewCommand(); // mark new command.

      Log.d(TAG, CodePosition.newInstance().toString()+  ", command: " + command + ", content: " + content); // Debug.

      if (command.equals("SYST")) // 系统信息
      {
        binaryStringSender.sendStringInBinaryMode("215 UNIX Type: L8"); //  发送回复。
      } //else if (command.equals("SYST")) // 系统信息
      else if (command.equals("PWD")) // 查询当前工作目录
      {
        String replyString="257 \"" + currentWorkingDirectory + "\""; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
          
        binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复内容。
      } //else if (command.equals("PWD")) // 查询当前工作目录
      else if (command.equals("TYPE")) // 传输类型
      {
        String replyString="200 binary type set"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
            
        binaryStringSender.sendStringInBinaryMode(replyString); // 回复内容。
      } //else if (command.equals("TYPE")) // 传输类型
      else if (command.equalsIgnoreCase("PASV")) // passive transmission.
      {
        processPasvCommand(); // process pasv command.
      } // else if (command.equals("PASV")) // 被动传输
      else if (command.equals("EPSV")) // 扩展被动模式
      {
        String replyString="202 "; // 回复内容。
          
        if (hasFolloingCommand) // 还有后续命令。
        {
        } // if (hasFolloingCommand) // 还有后续命令。
        else // if (hasFolloingCommand) // 还有后续命令。
        {
          Log.d(TAG, "reply string: " + replyString); //Debug.
            
          binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
        } // else // if (hasFolloingCommand) // 还有后续命令。
      } //else if (command.equals("EPSV")) // 扩展被动模式
      else if (command.equals("PORT")) // 要求服务器主动连接客户端的端口
      {
        String replyString="150 "; // 回复内容。正在打开数据连接
          
        boolean shouldSend=true; // 是否应当发送回复。

        if (allowActiveMode) // 允许主动模式
        {
          data_socket=null; // Forget the used data socket.
          openDataConnectionToClient(content); // 打开指向客户端特定端口的连接。

          replyString="150 "; // 回复内容。正在打开数据连接
        } //if (allowActiveMode) // 允许主动模式
        else // 不允许主动模式。
        {
          replyString="202 "; // 回复内容。未实现。
            
          if (hasFolloingCommand) // 还有后续命令。
          {
            shouldSend=false; // 不应当发送回复。
          } // if (hasFolloingCommand) // 还有后续命令。
        } //else // 不允许主动模式。

        if (shouldSend) // 应当发送回复。
        {
          Log.d(TAG, "reply string: " + replyString); //Debug.
            
          binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
        } // if (shouldSend) // 应当发送回复。
      } //else if (command.equals("EPSV")) // Extended passive mode.
      else if (command.toLowerCase().equals("list")) // 列出目录 陈欣
      {
        processListCommand(content); // 处理目录列表命令。
      } //else if (command.equals("list")) // 列出目录
      else if (command.toLowerCase().equals("nlst")) // List directory with file name only.
      {
        processNlstCommand(); // Process the command of nlst.
      } //else if (command.equals("list")) // 列出目录
      else if (command.toLowerCase().equals("retr")) // 获取文件
      {
        String data51= content.substring(5);

        data51=data51.trim(); // 去掉末尾换行
        
        processRetrCommand(data51); // Process the retr command.

      } //else if (command.equals("list")) // 列出目录
      else if (command.toLowerCase().equals("rest")) // 设置断点续传位置。
      {
        String data51= content.substring(5); // 跳过的长度。

        data51=data51.trim(); // 去掉末尾换行

        String replyString="350 Restart position accepted (" + data51 + ")"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
          
        binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
          
        Long restartPosition=Long.valueOf(data51);
          
        fileContentSender.setRestartPosition(restartPosition); // 设置重启位置。
      } //else if (command.equals("list")) // 列出目录
      else if (command.equalsIgnoreCase("USER")) // 用户登录
      {
        String targetWorkingDirectory=content.substring(5).trim(); // 获取新的工作目录。
        
        processUserCommand(targetWorkingDirectory); // Process user command.
      } // if (command.equals("USER")) // 用户登录
      else if (command.equalsIgnoreCase("feat")) // FEAT command
      {
        processFeatCommand(); // Process feat command.
      } // if (command.equals("USER")) // 用户登录
      else if (command.equalsIgnoreCase("PASS")) // 密码
      {
        String targetWorkingDirectory=content.substring(5).trim(); // 获取新的工作目录。
        
        processPassCommand(targetWorkingDirectory); // Process pass command.

      } //else if (command.equals("PASS")) // 密码
      else if (command.equalsIgnoreCase("cwd")) // 切换工作目录
      {
        String targetWorkingDirectory=content.substring(4).trim(); // 获取新的工作目录。
        
        processCwdCommand(targetWorkingDirectory); // 处理改变目录命令。
      } //else if (command.equals("cwd")) // 切换工作目录
      else if (command.equalsIgnoreCase("stor")) // 上传文件
      {
        String data51= content.substring(5);

        data51=data51.trim(); // 去掉末尾换行
        
        processStorCommand(data51); // 处理上传文件命令。
      } //else if (command.equals("stor")) // 上传文件
      else if (command.equalsIgnoreCase("thmb")) // Get a thumbnail
      {
        String data51= content.substring(5);

        data51=data51.trim(); // 去掉末尾换行
        
        processThmbCommand(data51); // Handle the command thmb.
      } //else if (command.equals("stor")) // 上传文件
      else if (command.equalsIgnoreCase("quit")) // Quit
      {
        // String data51= content.substring(5);

        // data51=data51.trim(); // 去掉末尾换行
        
        processQuitCommand(); // Process quit command.
      } //else if (command.equals("stor")) // 上传文件
      else if (command.equals("SIZE")) // 文件尺寸
      {
        String data51 = content.substring(5);

        data51=data51.trim(); // 去掉末尾换行

        processSizeCommand(data51); // 处理尺寸 命令。
      } //else if (command.equals("SIZE")) // 文件尺寸
      else if (command.equalsIgnoreCase("AVBL")) // Available space
      {
        // String data51 = content.substring(5);

        // data51=data51.trim(); // 去掉末尾换行

        // processSizeCommand(data51); // 处理尺寸 命令。
        processAvblCommand(); // Process the avbl command.
      } //else if (command.equals("SIZE")) // 文件尺寸
      else if (command.equals("DELE")) // 删除文件
      {
        String data51= content.substring(5);
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file name to delete: " + data51 + ", lnegth: " + data51.length()); // Debug.

        data51=data51.trim(); // 去掉末尾换行
        
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file name to delete: " + data51 + ", lnegth: " + data51.length()); // Debug.

        processDeleCommand(data51); // Procee the dele command
      } //else if (command.equals("DELE")) // 删除文件
      else if (command.equals("RNFR")) // Source file name of the inplace rename operation.
      {
        String data51= content.substring(5);
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file name to rename: " + data51 + ", lnegth: " + data51.length()); // Debug.

        data51=data51.trim(); // 去掉末尾换行
        
        Log.d(TAG, CodePosition.newInstance().toString()+  ", file name to rename: " + data51 + ", lnegth: " + data51.length()); // Debug.

        processRnfrCommand(data51); // Procee the rnfr command
      } //else if (command.equals("DELE")) // 删除文件
      else if (command.equals("RNTO")) // Destination file name of the inplace rename operation.
      {
        String data51= content.substring(5);
        Log.d(TAG, CodePosition.newInstance().toString()+  ", target file name to rename: " + data51 + ", lnegth: " + data51.length()); // Debug.

        data51=data51.trim(); // 去掉末尾换行
        
        Log.d(TAG, CodePosition.newInstance().toString()+  ", target file name to rename: " + data51 + ", lnegth: " + data51.length()); // Debug.

        processRntoCommand(data51); // Procee the rnto command
      } //else if (command.equals("DELE")) // 删除文件
      else if (command.equals("RMD")) // 删除目录
      {
        String data51= content.substring(4);

        data51=data51.trim(); // 去掉末尾换行

        // 删除文件。陈欣

        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
//         File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。
        DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve 目录。

        boolean deleteResult= photoDirecotry.delete();
            
        Log.d(TAG, "delete result: " + deleteResult); // Debug.
            
        notifyEvent(EventListener.DELETE); // 报告事件，删除文件。
            
        String replyString="250 Delete success "+ data51; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
          
        binaryStringSender.sendStringInBinaryMode(replyString); // 回复内容。
      } //else if (command.equals("DELE")) // 删除文件
      else if (command.equalsIgnoreCase("MKD")) // 创建目录
      {
        String dirName = content.substring(4).trim(); // 提取目录名
        processMkdCommand(dirName); // 使用统一处理函数
      }
      else  // 其它命令
      {
        String replyString="502 " + content.trim()  +  " not implemented"; // 回复内容。未实现。

        Log.d(TAG, "reply string: " + replyString); //Debug.
          
        binaryStringSender.sendStringInBinaryMode(replyString); // 回复。
      } //else if (command.equals("EPSV")) // Extended passive mode.
    } // private void processCommand(String command, String content)

    /**
    * Report event.
    */
    private void notifyEvent(final String eventCode, final Object extraContent)
    {   
      if (eventListener!=null) // 有事件监听器。
      {
        Handler uiHandler = new Handler(Looper.getMainLooper());

        Runnable runnable= new Runnable()
        {
          /**
            * 具体执行的代码
          */
          public void run()
          {
            eventListener.onEvent(eventCode); // report event.
            eventListener.onEvent(eventCode, extraContent); // report event.
          } //public void run()
        };

        uiHandler.post(runnable);
      } //if (eventListener!=null) // 有事件监听器。
    } // private void notifyEvent(String eventCode)

    /**
    * Report event.
    */
    private void notifyError(Integer eventCode)
    {   
      if (errorListener!=null) // The error listener exists.
      {
        Handler uiHandler = new Handler(Looper.getMainLooper());

        Runnable runnable= new Runnable()
        {
          /**
            * 具体执行的代码
          */
          public void run()
          {
            errorListener.onError(eventCode); // report error.
          } //public void run()
        };

        uiHandler.post(runnable);
      } //if (eventListener!=null) // 有事件监听器。
    } // private void notifyEvent(String eventCode)

    /**
    * Report event.
    */
    private void notifyEvent(final String eventCode)
    {   
      notifyEvent(eventCode, null);
    } //private void notifyEvent(String eventCode)

    /**
    *  Check the permission of file manager.
    */
    public void checkFileManagerPermission(int permissinTypeCode, DocumentFile targetFile)
    {
      Log.d(TAG, "checkFileManagerPermission " ); //Debug.
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) // Android 11. isExternalStorageManager
      {
        boolean isFileManager=Environment.isExternalStorageManager();

        Log.d(TAG, "checkFileManagerPermission, is file manager: " + isFileManager ); //Debug.
        if (isFileManager) // Is file manager
        {
        } // if (isFileManager) // Is file manager
        else // Not file manager
        {
          if (permissinTypeCode==Constants.Permission.Read) // Read permission
          {
            File photoDirecotry=Environment.getExternalStorageDirectory(); // Get the file object.
            //           public static final String AndroidData = Environment.getExternalStorageDirectory().getPath() + "/Android/data/"; //!< /Android/data directory.

            File[] paths = photoDirecotry.listFiles();
        
            if (paths==null) // Unable to list files
            {
              notifyEvent(EventListener.NEED_EXTERNAL_STORAGE_MANAGER_PERMISSION, null); // Notify event, need external storage manager permission.
              //         if (filePathInterpreter.virtualPathExists(Constants.FilePath.AndroidData)) // Does virtual path exist
              //         {
              //         } // if (filePathInterpreter.virtualPathExists(Constants.FilePath.AndroidData)) // Does virtual path exist
              //         else // Virtual path does not exist
              //         {
              //           requestAndroidDataPermission(); // Request /Android/data permisson.
              //         } // else // Virtual path does not exist
            } // if (paths.length==0) // Unable to list files
          } // if (permissinTypeCode==Constants.Permission.Read) // Read permission
          else // Write permisison
          {
            boolean canDelete=targetFile.canWrite(); // Test whether we can dlete it.
            
            if (canDelete) // Can delete
            {
            } // if (canDelete) // Can delete
            else // Cannot delete
            {
              notifyEvent(EventListener.NEED_EXTERNAL_STORAGE_MANAGER_PERMISSION, null); // Notify event, need external storage manager permission.
            } // else // Cannot delete
          } // else // Write permisison

        
          // Chen xin
          //           gotoFileManagerSettingsPage(); // Goto file manager settings page.
          //           notifyEvent(EventListener.NEED_EXTERNAL_STORAGE_MANAGER_PERMISSION, null); // Notify event, need external storage manager permission.
        } // else // Not file manager
      } // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) // Android 11. isExternalStorageManager
    } // private void checkFileManagerPermission()

    /**
    *   Goto file manager settings page.
    */
    private void gotoFileManagerSettingsPage()
    {
      Log.d(TAG, "gotoFileManagerSettingsPage"); //Debug.

      Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);  // 跳转语言和输入设备

      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      String packageNmae=context.getPackageName();
      Log.d(TAG, "gotoFileManagerSettingsPage, package name: " + packageNmae); //Debug.

      String url = "package:"+packageNmae;

      Log.d(TAG, "gotoFileManagerSettingsPage, url: " + url); //Debug.

      intent.setData(Uri.parse(url));

      context.startActivity(intent);
    } // private void gotoFileManagerSettingsPage()
    
    /**
    * Request /Android/data permisson.
    */
    private void requestAndroidDataPermission()
    {
//       @TargetApi(26)    
//       private void requestAccessAndroidData(Activity activity)
//       {        
//         try 
//         {            
//           Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata");            
    
      File androidDataFile=new File(Constants.FilePath.AndroidData); // Get the file object.
      
      Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata");            
//       Uri androidDataUri=Uri.fromFile(androidDataFile); // Create Uri.
    
      openDirectory(uri); // Open directory.
    } // private void requestAndroidDataPermission()
    
    /**
    * Request to open directory
    */
    public void openDirectory(Uri uriToLoad) 
    {
      // Choose a directory using the system's file picker.
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

//       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);            
      
      // Optionally, specify a URI for the directory that should be opened in
      // the system file picker when it loads.
      intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

      String packageNmae=context.getPackageName();
      Log.d(TAG, "gotoFileManagerSettingsPage, package name: " + packageNmae); //Debug.

      String url = "package:"+packageNmae;

      Log.d(TAG, "gotoFileManagerSettingsPage, url: " + url); //Debug.

//       intent.setData(Uri.parse(url));

      int yourrequestcode=Constants.RequestCode.AndroidDataPermissionRequestCode;
      
//       context.startActivityForResult(intent, yourrequestcode);
//       context.startActivity(intent);
      
//       Chen xin
      
      DocumentTreeBrowseRequest browseRequest=new DocumentTreeBrowseRequest(); // Create the browse request.
      browseRequest.setRequestCode(yourrequestcode);
      browseRequest.setIntent(intent); // SEt intent.

      notifyEvent(EventListener.NEED_BROWSE_DOCUMENT_TREE, (Object)(browseRequest)); // Notify event, uplaod finished.
    } // public void openDirectory(Uri uriToLoad) 
    
    /**
    * Check /Android/data permission.
    */
    private void CheckAndroidDataPermission() 
    {
      File photoDirecotry=new File(Constants.FilePath.AndroidData); // Get the file object.
      
      File[] paths = photoDirecotry.listFiles();
      
      if (paths==null) // Unable to list files
      {
        if (filePathInterpreter.virtualPathExists(Constants.FilePath.AndroidData)) // Does virtual path exist
        {
        } // if (filePathInterpreter.virtualPathExists(Constants.FilePath.AndroidData)) // Does virtual path exist
        else // Virtual path does not exist
        {
          requestAndroidDataPermission(); // Request /Android/data permisson.
        } // else // Virtual path does not exist
      } // if (paths.length==0) // Unable to list files
    } // private void CheckAndroidDataPermission()

    /**
    * Process the command of nlst.
    */
    private void processNlstCommand()
    {
      String replyString="150 Opening BINARY mode data connection for file list, Ch"; // 回复内容。
      
      String content = ""; // Target directory.

      Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString + ", list command content: " + content); // Debug.

      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

      boolean extraFileInformation = false; // Do not send extra file information.

      sendListContentBySender(content, currentWorkingDirectory, extraFileInformation); // 发送目录列表数据。
    } // private void processNlstCommand()

  private void processMkdCommand(String fullPath)
  {
    // 拆分路径
    String[] parts = splitPath(fullPath);
    String parentPath = parts[0]; // 父路径
    String dirName = parts[1];    // 要创建的目录名

    Log.d(TAG, "Parent path: " + parentPath + ", Dir name: " + dirName); // Debug.

    if (dirName.isEmpty()) {
        String replyString = "550 Invalid directory name";
        Log.d(TAG, "reply string: " + replyString);
        binaryStringSender.sendStringInBinaryMode(replyString);
        return;
    }

    // 如果 parentPath 为空，则使用当前工作目录
    String effectiveParentPath = parentPath.isEmpty() ? currentWorkingDirectory : parentPath;

    // 获取父目录所对应的 DocumentFile
    DocumentFile parentDir = filePathInterpreter.getFile(rootDirectory, effectiveParentPath, "");

    if (parentDir == null || !parentDir.exists() || !parentDir.isDirectory()) {
        String replyString = "550 Failed to resolve parent directory: " + effectiveParentPath;
        Log.d(TAG, "reply string: " + replyString);
        binaryStringSender.sendStringInBinaryMode(replyString);
        return;
    }

    // 创建子目录
    DocumentFile newDir = parentDir.createDirectory(dirName);

    if (newDir != null && newDir.exists()) {
        String fullCreatedPath = effectiveParentPath + "/" + dirName;
        String replyString = "257 \"" + fullCreatedPath + "\" created";
        Log.d(TAG, "reply string: " + replyString);
        binaryStringSender.sendStringInBinaryMode(replyString);
    } else {
        String replyString = "550 Can't create directory: " + fullPath;
        Log.d(TAG, "reply string: " + replyString);
        binaryStringSender.sendStringInBinaryMode(replyString);
    }
  }
    
    /**
    * 处理目录列表命令。
    */
    private void processListCommand(String content) 
    {
      String replyString="150 Opening BINARY mode data connection for file list, Ch"; // 回复内容。

      Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString + ", list command content: " + content); // Debug.

      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

      sendListContentBySender(content, currentWorkingDirectory); // 发送目录列表数据。
    } //private void processListCommand(String content)
    
    /**
    * 安全关闭上传文件句柄，释放资源。
    * 必须在数据连接关闭或出错时调用。
    * 统一处理 Passive/Active 模式下的资源清理。
    */
    private void finishFileWrite() {
      Log.d(TAG, CodePosition.newInstance().toString() + 
            ", 📁 finishFileWrite() called. isUploading=" + isUploading + 
            ", writingFile=" + (writingFile != null ? writingFile.getUri().toString() : "null") ); // Debug.

      // ✅ 关闭 FileOutputStream
      if (fileOutputStream != null) {
        try {
          fileOutputStream.flush();
          fileOutputStream.close();
          Log.d(TAG, CodePosition.newInstance().toString() +  ", ✅ FileOutputStream closed" ); // Debug.
        } catch (IOException e) {
          Log.d(TAG, CodePosition.newInstance().toString() +  ", ❌ Error closing FileOutputStream: " + e.getMessage() ); // Debug.
        } finally {
          fileOutputStream = null;
        }
      }

      // ✅ 关闭 ParcelFileDescriptor
      if (pfd != null) {
        try {
          pfd.close();
          Log.d(TAG, CodePosition.newInstance().toString() +  ", ✅ ParcelFileDescriptor closed" ); // Debug.
        } catch (IOException e) {
          Log.d(TAG, CodePosition.newInstance().toString() +  ", ❌ Error closing PFD: " + e.getMessage() ); // Debug.
        } finally {
          pfd = null;
        }
      }

      // ✅ 清理状态
      writingFile = null;
      isUploading = false;

      Log.d(TAG, CodePosition.newInstance().toString() +  ", 📁 File write session ended" ); // Debug.
    }

    /**
    * Handle connect completed. Connect to port specified by the client.
    */
    private void handleConnectCompleted(Exception ex, final AsyncSocket socket) 
    {
      if(ex != null) // There was a problem.
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", error connecting to port specified by client, this: " + this); // Debug.

        // ex.printStackTrace(); // Report the error.
        
        
        if (retryConnectClientDataPortAmount>=10) // limit the retry times
        {
        } // if (retryConnectClientDataPortAmount>=10) // limit the retry times
        else // Still retry
        {
          Log.d(TAG, CodePosition.newInstance().toString()+  ", connecting to port specified by client: " + clientDataPort + ", this: " + this); // Debug.
          
          
          connectToClientDataPort(); // Connect to client data port.
          
          retryConnectClientDataPortAmount++; // Count the times.
          
          
        } // else // Still retry
        
      } // if(ex != null) // There was a problem.
      else // 无异常。
      {
        this.data_socket=socket; // Remember the data connection.
        Log.d(TAG, CodePosition.newInstance().toString()+  ", connected to port specified by client, this: " + this + ", datas socket: " + socket); // Debug.
        fileContentSender.setDataSocket(socket); // 设置数据连接套接字。
        Log.d(TAG, CodePosition.newInstance().toString()+  ", setting data socket: " + socket ); // Debug.
        directoryListSender.setDataSocket(socket); // 设置数据连接套接字。

        socket.setDataCallback(new DataCallback() 
        {
          @Override
          public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
          {
            receiveDataSocket(bb);
          } //public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
        }); //socket.setDataCallback(new DataCallback() {

        socket.setClosedCallback(new CompletedCallback() 
        {
          @Override
          public void onCompleted(Exception ex) 
          {
            if (ex != null) {
              Log.d(TAG, CodePosition.newInstance().toString() + 
                    ", ⚠️ Active mode data socket error: " + ex.getMessage() ); // Debug.
              ex.printStackTrace();
            } else {
              Log.d(TAG, CodePosition.newInstance().toString() + 
                    ", 🔌 Active mode data socket closed gracefully" ); // Debug.
            }

            // ✅ 统一关闭文件资源
            finishFileWrite();

            // ✅ 只有 writingFile 存在时才通知完成（兼容旧逻辑）
            if (writingFile == null) {
              notifyStorCompleted();
              Log.d(TAG, CodePosition.newInstance().toString() + 
                    ", ✅ STOR completed in active mode" ); // Debug.
            }

            // ✅ 清理 socket
            data_socket = null;
            fileContentSender.setDataSocket(null);
            directoryListSender.setDataSocket(null);
          }
        });

        socket.setEndCallback(new CompletedCallback() 
        {
          @Override
          public void onCompleted(Exception ex) 
          {
            if(ex != null) // There is some exception.
            {
              // throw new RuntimeException(ex);
              ex.printStackTrace(); // Report error.
            } // if(ex != null) 
          } // public void onCompleted(Exception ex) 
        }); // socket.setEndCallback(new CompletedCallback() 
      } //else // 无异常。
    }

    @Override
    /**
     * Accept data connection.
     * @param socket 连接对象。
     */
    public void handleDataAccept(final AsyncSocket socket)
    {
      Log.d(TAG, CodePosition.newInstance().toString() + ", handleDataAccept, [Server] data New Connection " + socket.toString());
      this.data_socket=socket;
      fileContentSender.setDataSocket(socket); // 设置数据连接套接字。
      Log.d(TAG, CodePosition.newInstance().toString()+  ", setting data socket: " + socket ); // Debug.
      directoryListSender.setDataSocket(socket); // 设置数据连接套接字。

      // Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry ); // Debug.
        
      socket.setDataCallback(
        new DataCallback()
        {
          @Override
          public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
          {
            receiveDataSocket(bb);
          }
        }); // socket.setDataCallback(

      socket.setClosedCallback(new CompletedCallback() 
      {
        @Override
        public void onCompleted(Exception ex) 
        {
          if (ex != null) {
            if (ex instanceof IOException) {
              Log.d(TAG, CodePosition.newInstance().toString() + 
                    ", ⚠️ Data socket closed with IOException: " + ex.getMessage() ); // Debug.
              ex.printStackTrace();
            } else {
              Log.e(TAG, CodePosition.newInstance().toString() + 
                    ", ❌ Unexpected exception in data socket", ex ); // Error.
              throw new RuntimeException(ex);
            }
          } else {
            Log.d(TAG, CodePosition.newInstance().toString() + 
                  ", 🔌 Data socket closed gracefully" ); // Debug.
          }

          // ✅ 统一关闭文件资源
          finishFileWrite();

          // ✅ 通知上传完成（仅当 isUploading 为 true）
          if (isUploading) {
            notifyStorCompleted();
            Log.d(TAG, CodePosition.newInstance().toString() + 
                  ", ✅ STOR completed successfully" ); // Debug.
          }

          // ✅ 清理 socket 引用
          data_socket = null;
          fileContentSender.setDataSocket(null);
          directoryListSender.setDataSocket(null);
          Log.d(TAG, CodePosition.newInstance().toString() + 
                ", setting data socket: null" ); // Debug.
        }
      });
    } //private void handleDataAccept(final AsyncSocket socket)

    /**
     * 接受新连接
     * @param socket 新连接的套接字对象
     */
    public void handleAccept(final AsyncSocket socket)
    {
      this.socket = socket;
      binaryStringSender.setSocket(socket); // set the socket object.
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", [Server] New Connection " + socket.toString() +  ", this: " + this); // Debug.
      
      ControlConnectionDataCallback dataCallback = new ControlConnectionDataCallback(this); // Creat e the control connection data callback.
      
      socket.setDataCallback(dataCallback); // SEt the data call back.

        socket.setClosedCallback(new CompletedCallback() 
        {
          @Override
          public void onCompleted(Exception ex) 
          {
            if (ex != null) 
            {
              ex.printStackTrace(); // 报告错误。
            }
            else
            {
              System.out.println("[Server] Successfully closed connection");
            }
          }
        });

        socket.setEndCallback(new CompletedCallback() 
        {
          @Override
          public void onCompleted(Exception ex) 
          {
            if (ex != null) // There was an exception
            {
              Log.d(TAG, CodePosition.newInstance().toString()+  ", control connection ended unexpected: " + this + ", chance to clean up"); // Debug.
              
              // Chenx in
              notifyError(Constants.ErrorCode.ControlConnectionEndedUnexpectedly); // Notify error. Control connection ended unexpectedly.
              
              ex.printStackTrace(); // Report the exeception.
            } // if (ex != null) // There was an exception
            else // 无异常
            {
              // Log.d(TAG, "ftpmodule [Server] Successfully end connection");
              Log.d(TAG, CodePosition.newInstance().toString()+  ", ftpmodule [Server] Successfully end connection: " + this + ", chance to clean up"); // Debug.
              
              dataServerManager.stopServerSockets(); // Stop server sockets.
            } //else // 无异常
          } // public void onCompleted(Exception ex) 
        });

        binaryStringSender.sendStringInBinaryMode("220 StupidBeauty FtpServer"); // 发送回复内容。
    } //private void handleAccept(final AsyncSocket socket)
    
    /**
    * Stop the control connectin.
    */
    public void stop()
    {
      socket.close(); // Stop the control connectin.
      
      if (data_socket!=null) // The data socket exists
      {
        data_socket.close(); // Stop the running data socket.
        data_socket = null; // Forget it.
      } // if (data_socket!=null) // The data socket exists
      
      dataServerManager.stopServerSockets(); // Stop server sockets.
    } //  public void stop()

    @Override
    /**
     * 启动数据传输服务器。
     */
    public void setupDataServer()
    {
      setupDataServerByManager(); // Set up data server by manager.
    } //private void setupDataServer()
    
    /**
    * Set up data server by manager.
    */
    private void setupDataServerByManager()
    {
      data_port = dataServerManager.setupDataServer(this); // Set up data server.
    } // private void setupDataServerByManager()

    /**
     * 启动数据传输服务器。
     */
    private void setupDataServerListen()
    {
      Random random=new Random(); //随机数生成器。

      int randomIndex=random.nextInt(65535-1025)+1025; //随机选择一个端口。

      data_port=randomIndex; 

      AsyncServer.getDefault().listen(host, data_port, new ListenCallback() 
      {
        @Override
        public void onAccepted(final AsyncSocket socket)
        {
          handleDataAccept(socket);
        } //public void onAccepted(final AsyncSocket socket)

        @Override
        public void onListening(AsyncServerSocket socket)
        {
          System.out.println("[Server] Server started listening for data connections");
        }

        @Override
        public void onCompleted(Exception ex) 
        {
          if(ex != null) 
          {
            ex.printStackTrace();

            setupDataServer(); // 重新初始化。
          }
          else
          {
            System.out.println("[Server] Successfully shutdown server");
          }
        } // public void onCompleted(Exception ex) 
      });
    } //private void setupDataServer()
}
