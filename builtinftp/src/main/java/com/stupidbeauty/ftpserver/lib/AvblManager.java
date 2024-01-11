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
public class AvblManager
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
  private static final String TAG = "AvblManager"; //!<  输出调试信息时使用的标记。
  private Context context; //!< 执行时使用的上下文。
  private AsyncSocket data_socket; //!< 当前的数据连接。
  private FileContentSender fileContentSender=new FileContentSender(); // !< 文件内容发送器。
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
  private DocumentFile renamingFile; //!< The file being renamed.
  private boolean isUploading=false; //!< 是否正在上传。陈欣
  private InetAddress host;
  private File rootDirectory=null; //!< 根目录。
  
  AvblManager(Context context)
  {
    this.context = context;
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
  */
  private void receiveDataSocket( ByteBufferList bb)
  {
    byte[] content=bb.getAllByteArray(); // 读取全部内容。

    boolean appendTrue=true;

    try // Write the file
    {
      //       FileUtils.writeByteArrayToFile(writingFile, content, appendTrue); // 写入。

      Uri uri=writingFile.getUri();
      ParcelFileDescriptor pfd = context.getContentResolver(). openFileDescriptor(uri, "wa");
      FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

      fileOutputStream.write( content );
      fileOutputStream.close();
      pfd.close();
    } // try // Write the file
    catch (Exception e) // Catch exception.
    {
      e.printStackTrace();
    } // catch (Exception e) // Catch exception.
  } // private void                         receiveDataSocket( ByteBufferList bb)

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
      
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // Resolve file path.

      writingFile=photoDirecotry; // 记录文件。
      isUploading=true; // 记录，处于上传状态。
      Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry ); // Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() ); // Debug.

      if (photoDirecotry!=null && photoDirecotry.exists()) // The file exists
      {
        if (photoDirecotry.isDirectory()) // It is an existing directory
        {
          result = false;
        } //  if (photoDirecotry.isDirectory()) // It is an existing directory
        else // It is a normal file.
        {
          // result = true;
          photoDirecotry.delete();
          Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() ); // Debug.
        } // else // It is a normal file.
        Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() ); // Debug.
      } // if (photoDirecotry.exists()) // The file exists

      if (result) // We can proceed so far
      {
        try // Create the file.
        {
          Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() ); // Debug.
          File virtualFile=new File(data51);
          
          File parentVirtualFile=virtualFile.getParentFile();
          
          Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() + ", parent virtual file: " + parentVirtualFile.getName() ); // Debug.
          String currentTryingPath=parentVirtualFile.getPath();

          DocumentFile parentDocuemntFile=filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, currentTryingPath); // Resolve parent path.
  //         FileUtils.touch(photoDirecotry); //创建文件。
          Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() + ", parent document file : " + parentDocuemntFile.getUri().toString()); // Debug.

          String fileNameOnly=virtualFile.getName(); // Get the file name.

          writingFile=parentDocuemntFile.createFile("", fileNameOnly); // Creat eh file.
          Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() + ", writing fiel: " + writingFile.getUri().toString()); // Debug.
        } // try // Create the file.
        catch (Exception e) // Catch any exception.
        {
          e.printStackTrace();
          Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() ); // Debug.
        } // catch (Exception e) // Catch any exception.
      } // if (result) // We can proceed so far
      Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry.getUri().toString() ); // Debug.
      
      return result;
    } // private void startStor(String data51, String currentWorkingDirectory) // 上传文件内容。
    
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
    * Get the avbl information.
    */
    public long getAvbl()
    {
      long result = 0;
      
      // chen xin.
      File file = context.getFilesDir(); // The files dir.

      long usableSpaceBytes=file.getUsableSpace(); //获取可用的字节数。

      result = usableSpaceBytes;
      
      return result;
    } // public long getAvbl()
    
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
            if(ex != null) // There is some exception
            {
              // throw new RuntimeException(ex);
              ex.printStackTrace();
            } // if(ex != null) // There is some exception

            System.out.println("[Client] Successfully closed connection");
              
            data_socket=null;

            if (writingFile!=null) // The writing file exists
            {
              notifyStorCompleted(); // 告知上传完成。
            } // if (writingFile!=null) // The writing file exists
          } // public void onCompleted(Exception ex) 
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

    /**
     * 启动数据传输服务器。
     */
    private void setupDataServerListen()
    {
      Random random=new Random(); //随机数生成器。

      int randomIndex=random.nextInt(65535-1025)+1025; //随机选择一个端口。

      data_port=randomIndex; 
    } //private void setupDataServer()
}
