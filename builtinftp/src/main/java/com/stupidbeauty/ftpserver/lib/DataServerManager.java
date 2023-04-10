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
// import android.os.PowerManager;
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
// import android.util.Log;
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
public class DataServerManager
{
  private FilePathInterpreter filePathInterpreter=null; //!< the file path interpreter.
  private String passWord=null; //!< Pass word provided.
  private boolean authenticated=true; //!< Is Login correct?
  private String userName=null; //!< User name provided.
  private UserManager userManager=null; //!< user manager.
  private BinaryStringSender binaryStringSender=new BinaryStringSender(); //!< 以二进制方式发送字符串的工具。
  private EventListener eventListener=null; //!< 事件监听器。
  private AsyncSocket socket; //!< 当前的客户端连接。
  private static final String TAG ="DataServerManager"; //!<  输出调试信息时使用的标记。
  private Context context; //!< 执行时使用的上下文。
  private AsyncSocket data_socket; //!< 当前的数据连接。
  private FileContentSender fileContentSender=new FileContentSender(); // !< 文件内容发送器。
  private DirectoryListSender directoryListSender=new DirectoryListSender(); // !< 目录列表发送器。
  private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
  private String currentWorkingDirectory="/"; //!< 当前工作目录
  private int data_port=1544; //!< 数据连接端口。
  private String ip; //!< ip
  private boolean allowActiveMode=true; //!< 是否允许主动模式。
  private DataServerManager dataServerManager=null; //!< Data server manager

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
    * 打开指向客户端特定端口的连接。
    */
    private void openDataConnectionToClient(String content)
    {
      String portString=content.split(" ")[1].trim(); // 端口字符串。
    
      String[] addressStringList= portString.split(","); //获取地址字符串。
    
      String ip=addressStringList[0]+"."+addressStringList[1]+"."+addressStringList[2]+"."+addressStringList[3]; // 构造IP。陈欣
      int port=Integer.parseInt(addressStringList[4])*256+Integer.parseInt(addressStringList[5]); // 计算出端口号。
    
      //连接：陈欣
    
      AsyncServer.getDefault().connectSocket(new InetSocketAddress(ip, port), new ConnectCallback() 
      {
        @Override
        public void onConnectCompleted(Exception ex, final AsyncSocket socket) 
        {
          handleConnectCompleted(ex, socket);
        } // public void onConnectCompleted(Exception ex, final AsyncSocket socket) 
      });
    } //private void openDataConnectionToClient(String content)

    public void notifyFileNotExist() // 告知文件不存在
    {
      String replyString="550 File not exist"; // File does not exist.

      Log.d(TAG, "reply string: " + replyString); //Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送。
    } //private void notifyFileNotExist()

    /**
    * 告知已经发送文件内容数据。
    */
    public void notifyFileSendCompleted() 
    {
      String replyString="216 File sent. ChenXin"; // 回复内容。

      Log.d(TAG, "reply string: " + replyString); //Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送。
      
      notifyEvent(EventListener.DOWNLOAD_FINISH); // 报告事件，完成下载文件。
    } //private void notifyFileSendCompleted()

    /**
    * 告知上传完成。
    */
    private void notifyStorCompleted() 
    {
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

      if (photoDirecotry.isDirectory()) // 是个目录
      {
//         fullPath=photoDirecotry.getPath(); // 获取当前工作目录的完整路径。
//         Uri directoryUri=photoDirecotry.getUri(); // Get the uri.
//         String directyoryUriPath=directoryUri.getPath(); // Get the string of the uri.
//         String directoryPurePath=directyoryUriPath.replaceAll("file://", ""); // Replace prefix.
//         currentVersionName=currentVersionName.replaceAll("[a-zA-Z]|\\s", "");

//         File directoryFileObject=new File()
        
//         fullPath=directyoryUriPath; // 获取当前工作目录的完整路径。

        
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

      Log.d(TAG, CodePosition.newInstance().toString()+  ", reply string: " + replyString); //Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
      
//       if (fullPath.equals(Constants.FilePath.AndroidData)) // It is /Android/data
      if (filePathInterpreter.isSamePath (fullPath, Constants.FilePath.AndroidData)) // It is /Android/data, same path.
      {
        Log.d(TAG, CodePosition.newInstance().toString()+  ", full path : " + fullPath + ", other path: " + Constants.FilePath.AndroidData + ", checking /Android/data permission"); // Debug.
        CheckAndroidDataPermission(); // Check /Android/data permission.
      } // if (currentWorkingDirectory.equals(Constants.FilePath.AndroidData)) // It is /Android/data
    } // private void processCwdCommand(String targetWorkingDirectory)

    /**
    * 处理尺寸查询命令。
    */
    private void processSizeCommand(String data51)
    {
      Log.d(TAG, "processSizeCommand: filesdir: " + rootDirectory.getPath()); // Debug.
      Log.d(TAG, "processSizeCommand: workding directory: " + currentWorkingDirectory); // Debug.
      Log.d(TAG, "processSizeCommand: data51: " + data51); // Debug.
    
//       FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); // resolve file path.

      String replyString=""; // 回复字符串。

      if (photoDirecotry.exists()) // 文件存在
      {
        long fileSize= photoDirecotry.length(); //文件尺寸。 陈欣
            
        replyString="213 " + fileSize + " "; // 文件尺寸。
      } //if (photoDirecotry.exists()) // 文件存在
      else // 文件不 存在
      {
        replyString="550 No directory traversal allowed in SIZE param"; // File does not exist.
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

      boolean deleteResult= photoDirecotry.delete();
          
      Log.d(TAG, "delete result: " + deleteResult); // Debug.
      
      String replyString="250 "; // 回复内容。

      if (deleteResult) // Delete success
      {
        notifyEvent(EventListener.DELETE); // 报告事件，删除文件。
        replyString="250 Delete success"; // Reply, delete success.
      } // if (deleteResult) // Delete success
      else // Delete fail
      {
        replyString="550 File delete failed"; // File delete failed.
//         replyString="250 "; // 回复内容。

        checkFileManagerPermission(Constants.Permission.Write, photoDirecotry); // Check permission of write.
      } // else // Delete fail

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
    
    private void handleConnectCompleted(Exception ex, final AsyncSocket socket) 
    {
      if(ex != null) 
      {
        ex.printStackTrace(); //报告错误
      }
      else // 无异常。
      {
        this.data_socket=socket; // Remember the data connection.
        fileContentSender.setDataSocket(socket); // 设置数据连接套接字。
        directoryListSender.setDataSocket(socket); // 设置数据连接套接字。

//         Util.writeAll(socket, "Hello Server".getBytes(), new CompletedCallback() {
//             @Override
//             public void onCompleted(Exception ex) {
//                 if (ex != null) throw new RuntimeException(ex);
//                 System.out.println("[Client] Successfully wrote message");

        socket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
            {
                receiveDataSocket(bb);
            } //public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
        }); //socket.setDataCallback(new DataCallback() {

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) 
            {
              if(ex != null) throw new RuntimeException(ex);
              System.out.println("[Client] Successfully closed connection");
                
              data_socket=null;
                
              notifyStorCompleted(); // 告知上传完成。
            }
        });

        socket.setEndCallback(new CompletedCallback() 
        {
          @Override
          public void onCompleted(Exception ex) 
          {
            if(ex != null) throw new RuntimeException(ex);
//             System.out.println("[Client] Successfully end connection");
          } // public void onCompleted(Exception ex) 
        }); // socket.setEndCallback(new CompletedCallback() 
      } //else // 无异常。
    }

        /**
     * Accept data connection.
     * @param socket 连接对象。
     */
    private void handleDataAccept(final AsyncSocket socket)
    {
      this.data_socket=socket;
      fileContentSender.setDataSocket(socket); // 设置数据连接套接字。
      directoryListSender.setDataSocket(socket); // 设置数据连接套接字。

      Log.d(TAG, CodePosition.newInstance().toString() + ", handleDataAccept, [Server] data New Connection " + socket.toString());
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
//             if (ex != null) throw new RuntimeException(ex);
            
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
            
          System.out.println("[Server] data Successfully closed connection");
              
          data_socket=null;
          fileContentSender.setDataSocket(data_socket); // 将数据连接清空
          directoryListSender.setDataSocket(data_socket); // 将数据连接清空。
              
          if (isUploading) // 是处于上传状态。
          {
            notifyStorCompleted(); // 告知上传完成。
            
            
                  
            isUploading=false; // 不再处于上传状态了。
          } //if (isUploading) // 是处于上传状态。
        }
      });

      socket.setEndCallback(new CompletedCallback() 
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
                
          // System.out.println("[Server] data Successfully end connection");
          Log.d(TAG, CodePosition.newInstance().toString() + ", [Server] data Successfully end connection " + socket.toString());
        }
      });
    } //private void handleDataAccept(final AsyncSocket socket)

    /**
     * 启动数据传输服务器。
     */
    public int setupDataServer(DataServerManagerInterface dataServerManagerInterface)
    {
      int result = setupDataServerListen(dataServerManagerInterface); // Set up data server by listening.
      
      return result;

      // setupDataServerByManager(); // Set up data server by manager.
    } //private void setupDataServer()
    
    /**
     * 启动数据传输服务器。
     */
    private int setupDataServerListen(DataServerManagerInterface dataServerManagerInterface)
    {
      Random random=new Random(); //随机数生成器。

      int randomIndex=random.nextInt(65535-1025)+1025; //随机选择一个端口。

      data_port=randomIndex; 

      AsyncServer.getDefault().listen(host, data_port, new ListenCallback() 
      {
        @Override
        public void onAccepted(final AsyncSocket socket)
        {
          dataServerManagerInterface.handleDataAccept(socket);
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

            dataServerManagerInterface.setupDataServer(); // 重新初始化。
          }
          else
          {
            System.out.println("[Server] Successfully shutdown server");
          }
        } // public void onCompleted(Exception ex) 
      });
      
      return data_port;
    } //private void setupDataServer()
}
