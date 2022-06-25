package com.stupidbeauty.ftpserver.lib;

import 	android.provider.DocumentsContract;
import java.util.Locale;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.io.IOException;
import com.koushikdutta.async.*;
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
import android.net.wifi.WifiManager;
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.os.Environment;

class ControlConnectHandler
{
  private String passWord=null; //!< Pass word provided.
  private boolean authenticated=true; //!< Is Login correct?
  private String userName=null; //!< User name provided.
  private UserManager userManager=null; //!< user manager.
  private BinaryStringSender binaryStringSender=new BinaryStringSender(); //!< 以二进制方式发送字符串的工具。
  private EventListener eventListener=null; //!< 事件监听器。
  private AsyncSocket socket; //!< 当前的客户端连接。
  private static final String TAG ="ControlConnectHandler"; //!<  输出调试信息时使用的标记。
  private Context context; //!< 执行时使用的上下文。
  private AsyncSocket data_socket; //!< 当前的数据连接。
  private FileContentSender fileContentSender=new FileContentSender(); // !< 文件内容发送器。
  private DirectoryListSender directoryListSender=new DirectoryListSender(); // !< 目录列表发送器。
  private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
  private String currentWorkingDirectory="/"; //!< 当前工作目录
  private int data_port=1544; //!< 数据连接端口。
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
  } //private void                         receiveDataSocket( ByteBufferList bb)

  public ControlConnectHandler(Context context, boolean allowActiveMode, InetAddress host)
  {
    this.context=context;
    this.allowActiveMode=allowActiveMode;
    this.host=host;

            setupDataServer(); // 启动数据传输服务器。
    }
    
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
        }
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
    * 发送目录列表数据。
    */
    private void sendListContentBySender(String fileName, String currentWorkingDirectory) 
    {
      directoryListSender.setControlConnectHandler(this); // 设置控制连接处理器。
      directoryListSender.setDataSocket(data_socket); // 设置数据连接套接字。
      directoryListSender.sendDirectoryList(fileName, currentWorkingDirectory); // 让目录列表发送器来发送。
    } // private void sendListContentBySender(String fileName, String currentWorkingDirectory)

    /**
    * 告知上传完成。
    */
    private void notifyStorCompleted() 
    {
      String replyString="226 Stor completed."; // 回复内容。

      Log.d(TAG, "reply string: " + replyString); //Debug.

      binaryStringSender.sendStringInBinaryMode(replyString);
    } //private void notifyStorCompleted()
    
    /**
     * 告知已经发送目录数据。
     */
    public void notifyLsCompleted()
    {
//        send_data "216 \n"

      String replyString="226 Data transmission OK. ChenXin"; // 回复内容。
      
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

      Log.d(TAG, "reply string: " + replyString); //Debug.
    } //private void notifyLsCompleted()
    
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
      FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。

      writingFile=photoDirecotry; // 记录文件。
      isUploading=true; // 记录，处于上传状态。

      if (photoDirecotry.exists())
      {
        photoDirecotry.delete();
      }
        
      try //尝试构造请求对象，并且捕获可能的异常。
      {
        FileUtils.touch(photoDirecotry); //创建文件。
      } //try //尝试构造请求对象，并且捕获可能的异常。
      catch (Exception e)
      {
        e.printStackTrace();
      }
    } //private void startStor(String data51, String currentWorkingDirectory) // 上传文件内容。
    
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
      FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, targetWorkingDirectory); //照片目录。

      String replyString="" ; // 回复内容。
      String fullPath="";

      if (photoDirecotry.isDirectory()) // 是个目录
      {
        fullPath=photoDirecotry.getPath(); // 获取当前工作目录的完整路径。
        String rootPath=rootDirectory.getPath(); // 获取根目录的完整路径。
        
        currentWorkingDirectory=fullPath.substring(rootPath.length()); // 去掉开头的根目录路径。
        
        if (currentWorkingDirectory.isEmpty()) // 是空白的了
        {
          currentWorkingDirectory="/"; // 当前工作目录是根目录。
        } // if (currentWorkingDirectory.isEmpty()) // 是空白的了
        
        Log.d(TAG, "processCwdCommand, fullPath: " + fullPath ); // Debug.
        Log.d(TAG, "processCwdCommand, rootPath: " + rootPath ); // Debug.
        Log.d(TAG, "processCwdCommand, currentWorkingDirectory: " + currentWorkingDirectory ); // Debug.

        replyString="250 cwd succeed" ; // 回复内容。
      } //if (photoDirecotry.isDirectory()) // 是个目录
      else //不是个目录
      {
        replyString="550 not a directory: " + targetWorkingDirectory; // 回复内容。
      }

      Log.d(TAG, "reply string: " + replyString); //Debug.
        
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
      
      if (fullPath.equals(Constants.FilePath.AndroidData)) // It is /Android/data
      {
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
    
      FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
      File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。

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
     * 处理命令。
     * @param command 命令关键字
     * @param content 整个消息内容。
     */
    private void processCommand(String command, String content, boolean hasFolloingCommand)
    {
      Log.d(TAG, "command: " + command + ", content: " + content); //Debug.

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
      else if (command.equals("PASV")) // 被动传输
      {
        setupDataServer(); // 初始化数据服务器。
        
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        String ip = ipAddress.replace(".", ",");
        
        int port256=data_port/256;
        int portModule=data_port-port256*256;

        String replyString="227 Entering Passive Mode ("+ip+","+port256+","+portModule+") "; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        binaryStringSender.sendStringInBinaryMode(replyString); // 回复内容。
      } //else if (command.equals("PASV")) // 被动传输
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
      else if (command.toLowerCase().equals("retr")) // 获取文件
      {
        String data51= content.substring(5);

        data51=data51.trim(); // 去掉末尾换行

        String replyString="150 start send content: " + data51 ; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
          
        binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

        sendFileContent(data51, currentWorkingDirectory); // 发送文件内容。
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

      } //if (command.equals("USER")) // 用户登录
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
      else if (command.equals("SIZE")) // 文件尺寸
      {
        String data51 = content.substring(5);

        data51=data51.trim(); // 去掉末尾换行

        processSizeCommand(data51); // 处理尺寸 命令。
      } //else if (command.equals("SIZE")) // 文件尺寸
      else if (command.equals("DELE")) // 删除文件
      {
        String data51= content.substring(5);

        data51=data51.trim(); // 去掉末尾换行

        // 删除文件

        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
        FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
        File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。

        boolean deleteResult= photoDirecotry.delete();
            
        Log.d(TAG, "delete result: " + deleteResult); // Debug.
            
        notifyEvent(EventListener.DELETE); // 报告事件，删除文件。
            
        String replyString="250 "; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
          
        binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。
      } //else if (command.equals("DELE")) // 删除文件
      else if (command.equals("RMD")) // 删除目录
      {
        String data51= content.substring(4);

        data51=data51.trim(); // 去掉末尾换行

        // 删除文件。陈欣

        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
        FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
        File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。

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
    } //private void processCommand(String command, String content)

    /**
    * 报告事件，删除文件。
    */
    private void notifyEvent(final String eventCode)
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
            eventListener.onEvent(eventCode); // 报告事件。
          } //public void run()
        };

        uiHandler.post(runnable);
      } //if (eventListener!=null) // 有事件监听器。
    } //private void notifyEvent(String eventCode)

    /**
    *  CheCK THE permission of file manager.
    */
    private void checkFileManagerPermission()
    {
      Log.d(TAG, "checkFileManagerPermission " ); //Debug.

      boolean isFileManager=Environment.isExternalStorageManager();

      Log.d(TAG, "checkFileManagerPermission, is file manager: " + isFileManager ); //Debug.
      if (isFileManager) // Is file manager
      {
      } // if (isFileManager) // Is file manager
      else // Not file manager
      {
        // Chen xin
        gotoFileManagerSettingsPage(); // Goto file manager settings page.
      } // else // Not file manager
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
//           Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);            
//           intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);            
//           flag看实际业务需要可再补充            
//           intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);            
//           activity.startActivityForResult(intent, 6666);        
//         } 
//         catch (Exception e) 
//         {            
//           e.printStackTrace();        
//         }    
//       } 

    
      File androidDataFile=new File(Constants.FilePath.AndroidData); // Get the file object.
      
      Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata");            
//       Uri androidDataUri=Uri.fromFile(androidDataFile); // Create Uri.
    
      openDirectory(uri); // Open directory.
    } // private void requestAndroidDataPermission()
    
    public void openDirectory(Uri uriToLoad) 
    {
      // Choose a directory using the system's file picker.
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);            
      
      // Optionally, specify a URI for the directory that should be opened in
      // the system file picker when it loads.
      intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

      int yourrequestcode=Constants.RequestCode.AndroidDataPermissionRequestCode;
      
//       context.startActivityForResult(intent, yourrequestcode);
      context.startActivity(intent);
    }
    
    /**
    * Check /Android/data permission.
    */
    private void CheckAndroidDataPermission() 
    {
      File photoDirecotry=new File(Constants.FilePath.AndroidData); // Get the file object.
      
      File[] paths = photoDirecotry.listFiles();
      
      if (paths==null) // Unable to list files
      {
        requestAndroidDataPermission(); // Request /Android/data permisson.
      } // if (paths.length==0) // Unable to list files
    } // private void CheckAndroidDataPermission()
    
    /**
    * 处理目录列表命令。
    */
    private void processListCommand(String content) 
    {
      //陈欣
      String replyString="150 Opening BINARY mode data connection for file list, ChenXin"; // 回复内容。

      Log.d(TAG, "reply string: " + replyString); //Debug.
      
      binaryStringSender.sendStringInBinaryMode(replyString); // 发送回复。

      sendListContentBySender(content, currentWorkingDirectory); // 发送目录列表数据。

      checkFileManagerPermission(); // CheCK THE permission of file manager.
    } //private void processListCommand(String content)

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
//             }
//         });

        socket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
            {
                receiveDataSocket(bb);
            } //public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
        }); //socket.setDataCallback(new DataCallback() {

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
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
            System.out.println("[Client] Successfully end connection");
          }
        });
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

      Log.d(TAG, "handleDataAccept, [Server] data New Connection " + socket.toString());
        
      socket.setDataCallback(
        new DataCallback()
        {
          @Override
          public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
          {
            receiveDataSocket(bb);
          }
        });

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
                
            System.out.println("[Server] data Successfully end connection");
          }
        });
    } //private void handleDataAccept(final AsyncSocket socket)


    /**
     * 接受新连接
     * @param socket 新连接的套接字对象
     */
    public void handleAccept(final AsyncSocket socket)
    {
      this.socket=socket;
      binaryStringSender.setSocket(socket); // 设置套接字。
      
      System.out.println("[Server] New Connection " + socket.toString());

      socket.setDataCallback
      (
        new DataCallback()
        {
          @Override
          public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
          {
            String content = new String(bb.getAllByteArray());
            Log.d(TAG, "[Server] Received Message " + content); // Debug
                
            String[] lines=content.split("\r\n"); // 分割成一行行的命令。
                
            int lineAmount=lines.length; // 获取行数

            Log.d(TAG, "[Server] line amount: " + lineAmount); // Debug

            for(int lineCounter=0; lineCounter< lineAmount; lineCounter++)
            {
              String currentLine=lines[lineCounter]; // 获取当前命令。
                  
              String command = currentLine.split(" ")[0]; // Get the command.

              command=command.trim();
              
              boolean hasFolloingCommand=true; // 是否还有后续命令。
              
              if ((lineCounter+1)==(lineAmount)) // 是最后一条命令了。
              {
                hasFolloingCommand=false; // 没有后续命令。
              }

              processCommand(command, currentLine, hasFolloingCommand); // 处理命令。
            } // for(int lineCounter=0; lineCounter< lineAmount; lineCounter++)
          }
        });

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
            if (ex != null) // 有异常出现
            {
//                 throw new RuntimeException(ex);
              ex.printStackTrace(); // 报告。
            }
            else // 无异常
            {
              Log.d(TAG, "ftpmodule [Server] Successfully end connection");
            } //else // 无异常
          }
        });

        //发送初始命令：
//        send_data "220 \n"

        binaryStringSender.sendStringInBinaryMode("220 StupidBeauty FtpServer"); // 发送回复内容。
    } //private void handleAccept(final AsyncSocket socket)

    /**
     * 启动数据传输服务器。
     */
    private void setupDataServer()
    {
      Random random=new Random(); //随机数生成器。

      int randomIndex=random.nextInt(65535-1025)+1025; //随机选择一个端口。

      data_port=randomIndex; 

//         try // 绑定端口。
//         {
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
//                 09-07 07:57:47.473 18998 19023 W System.err: java.lang.RuntimeException: java.net.BindException: Address already in use

//                 throw new RuntimeException(ex);
            ex.printStackTrace();

            setupDataServer(); // 重新初始化。
          }
          else
          {
            System.out.println("[Server] Successfully shutdown server");
          }
        }
      });
    } //private void setupDataServer()
}
