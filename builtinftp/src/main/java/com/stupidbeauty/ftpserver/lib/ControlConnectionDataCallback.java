package com.stupidbeauty.ftpserver.lib;

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
public class ControlConnectionDataCallback implements DataCallback
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
  private static final String TAG ="ControlConnectionDataCallback"; //!<  The tag used in debug code.
  private ControlConnectHandler context; //!< The control connection handler.
  // private AsyncSocket data_socket; //!< 当前的数据连接。
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
  // private DataServerManager dataServerManager=null; //!< Data server manager
  private DataServerManager dataServerManager=new DataServerManager(); //!< The data server manager.

//   private File writingFile; //!< 当前正在写入的文件。
  private DocumentFile writingFile; //!< 当前正在写入的文件。

  private boolean isUploading=false; //!< 是否正在上传。陈欣
  private InetAddress host;
  private File rootDirectory=null; //!< 根目录。
  
  /**
  * Set the user manager.
  */
  public void setUserManager(UserManager userManager)
  { 
    this.userManager=userManager;
  } // public void setUserManager(UserManager userManager)
  
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
  
  public ControlConnectionDataCallback(ControlConnectHandler context)
  {
    Log.d(TAG, CodePosition.newInstance().toString()+  ", constructing new ControlConnectHandler: " + this); // Debug.
    this.context=context;
    this.allowActiveMode=allowActiveMode;
    this.host=host;
    this.ip=ip; // Remember ip for data server.
  } // public ControlConnectHandler(Context context, boolean allowActiveMode, InetAddress host, String ip)
  
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

      binaryStringSender.sendStringInBinaryMode(replyString);

      startStor(data51, currentWorkingDirectory); // 发送文件内容。
    } // private void processStorCommand(String data51)

    /**
    * 上传文件内容。
    */
    private void startStor(String data51, String currentWorkingDirectory) 
    {
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // Resolve file path.

      writingFile=photoDirecotry; // 记录文件。
      isUploading=true; // 记录，处于上传状态。
      Log.d(TAG, CodePosition.newInstance().toString()+  ", photoDirecotry: " + photoDirecotry ); // Debug.

      if (photoDirecotry!=null && photoDirecotry.exists()) // The file exists
      {
        photoDirecotry.delete();
      } // if (photoDirecotry.exists()) // The file exists
        
      try // Create the file.
      {
        File virtualFile=new File(data51);
        
        File parentVirtualFile=virtualFile.getParentFile();
        
        String currentTryingPath=parentVirtualFile.getPath();

        DocumentFile parentDocuemntFile=filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, currentTryingPath); // Resolve parent path.
//         FileUtils.touch(photoDirecotry); //创建文件。

        String fileNameOnly=virtualFile.getName(); // Get the file name.

        writingFile=parentDocuemntFile.createFile("", fileNameOnly); // Creat eh file.
      } // try // Create the file.
      catch (Exception e) // Catch any exception.
      {
        e.printStackTrace();
      } // catch (Exception e) // Catch any exception.
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
    private void processSizeCommand(String data51)
    {
      Log.d(TAG, "processSizeCommand: filesdir: " + rootDirectory.getPath()); // Debug.
      // Log.d(TAG, "processSizeCommand: workding directory: " + currentWorkingDirectory); // Debug.
      Log.d(TAG, "processSizeCommand: data51: " + data51); // Debug.
    
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve file path.

      String replyString=""; // 回复字符串。

      if  ((photoDirecotry!=null) && (photoDirecotry.exists() && (photoDirecotry.isFile()))) // The path exists. And it is a file.
      {
        long fileSize= photoDirecotry.length(); //文件尺寸。 陈欣
            
        replyString="213 " + fileSize + " "; // 文件尺寸。
      } //if (photoDirecotry.exists()) // 文件存在
      else // Not an existing file
      {
        if ((photoDirecotry==null) || (!photoDirecotry.exists())) // not exist
        {
          // Chen xin.
          replyString="550 File not exist " + data51; // File does not exist.
          // replyString="550 No directory traversal allowed in SIZE param"; // File does not exist.
        } // if ((photoDirecotry==null) || (!photoDirecotry.exists())) // not exist
        else // Directory
        {
          replyString="550 No directory traversal allowed in SIZE param"; // File does not exist.
        } // else // Directory
      } //else // 文件不 存在

      Log.d(TAG, "reply string: " + replyString); //Debug.
      
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
    } //private void processSizeCommand(String data51)
    
    /**
    *  Process the dele command
    */
    private void processDeleCommand(String data51)
    {
      // 删除文件

      String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                  
      wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                  
      //         FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve file

        
      String replyString="250 "; // 回复内容。

      if (photoDirecotry!=null) // The documentfile object exists
      {
        boolean deleteResult= photoDirecotry.delete();
            
        Log.d(TAG, "delete result: " + deleteResult); // Debug.
        

        if (deleteResult) // Delete success
        {
          notifyEvent(EventListener.DELETE); // 报告事件，删除文件。
          replyString="250 Delete success " + data51; // Reply, delete success.
          
          
          // Chen xin. remove cache DocumentFile.
          
          PathDocumentFileCacheManager pathDocumentFileCacheManager = filePathInterpreter.getPathDocumentFileCacheManager(); // Get the path documetnfile cache manager.
            // for(DocumentFile path:paths) // reply files one by one
            {
              // String currentLine=construct1LineListFile(path); // 构造针对这个文件的一行输出。

              // String fileName=path.getName(); // 获取文件名。
              
              String effectiveVirtualPathForCurrentSegment=wholeDirecotoryPath; // Remember effective virtual path.
              effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment.replace("//", "/"); // Remove consecutive /
              
              Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath  + ", effective virtual path: " + effectiveVirtualPathForCurrentSegment); // Debug.

              pathDocumentFileCacheManager.remove(effectiveVirtualPathForCurrentSegment); // Remove it from the cache.

            } // for(DocumentFile path:paths) // reply files one by one

        } // if (deleteResult) // Delete success
        else // Delete fail
        {
          replyString="550 File delete failed"; // File delete failed.
  //         replyString="250 "; // 回复内容。

          checkFileManagerPermission(Constants.Permission.Write, photoDirecotry); // Check permission of write.
        } // else // Delete fail
      } // if (photoDirecotry!=null) // The documentfile object exists
      else // The doucmentfile object does not exist
      {
        replyString="550 File delete failed " + data51; // File delete failed.
      } // else // The doucmentfile object does not exist
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString); // Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
    } // private void processDeleCommand(String data51)

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

    @Override
    /**
     * Read data
     */
          public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
          {
            String content = new String(bb.getAllByteArray());

            Log.d(TAG, CodePosition.newInstance().toString()+  ", [Server] Received Message " + content + ", this: " + this + ", control connection handler: " + context); // Debug.

            String[] lines=content.split("\r\n"); // 分割成一行行的命令。
                
            int lineAmount=lines.length; // 获取行数

            for(int lineCounter=0; lineCounter< lineAmount; lineCounter++)
            {
              String currentLine=lines[lineCounter]; // 获取当前命令。
                  
              String command = currentLine.split(" ")[0]; // Get the command.

              command=command.trim();
              
              boolean hasFolloingCommand=true; // 是否还有后续命令。
              
              if ((lineCounter+1)==(lineAmount)) // 是最后一条命令了。
              {
                hasFolloingCommand=false; // 没有后续命令。
              } // if ((lineCounter+1)==(lineAmount)) // 是最后一条命令了。

              context.processCommand(command, currentLine, hasFolloingCommand); // proces the command
            } // for(int lineCounter=0; lineCounter< lineAmount; lineCounter++)
          } // public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 

    
}
