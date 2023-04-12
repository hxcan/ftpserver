package com.stupidbeauty.ftpserver.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.stupidbeauty.hxlauncher.interfaces.LocalServerListLoadListener;
import java.util.Map;
import java.util.List;
import android.os.Process;
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
public class DataServerManager
{
  private Map<Integer, Boolean> dataPortUsageMap=new HashMap<>(); //!< The map of data port usage mark.
  private List<Integer> dataPortPool=new ArrayList<>(); //!< Data port pool.
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
  private AsyncServerSocket listeningServerSocket = null; //!< Remembered listening server socket.
  private String ip; //!< ip
  private boolean allowActiveMode=true; //!< 是否允许主动模式。
  private DataServerManager dataServerManager=null; //!< Data server manager

//   private File writingFile; //!< 当前正在写入的文件。
  private DocumentFile writingFile; //!< 当前正在写入的文件。

  private boolean isUploading=false; //!< 是否正在上传。陈欣
  private InetAddress host;
  private File rootDirectory=null; //!< 根目录。

  /**
  * Stop server sockets.
  */
  public void stopServerSockets()
  {
    listeningServerSocket.stop(); // Stop.
  } // public void stopServerSockets()
  
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
    } //private void setupDataServer()
    
    /**
     * 启动数据传输服务器。
     */
    private int setupDataServerListen(DataServerManagerInterface dataServerManagerInterface)
    {
      int result=0;  // The listening data port.
      // int data_port=0;  // The listening data port.
      boolean foundExistingPort=false; // Found existing port
      for(int currentPortInPool: dataPortPool) // Check exisintg port pool
      {
        boolean ocupied=dataPortUsageMap.get(currentPortInPool); // Get copucied status.
        
        // if (!ocupied) // not ocuupied
        {
          foundExistingPort=true;
          result=currentPortInPool; // use existing port.
          break;
        } // if (!ocupied) // not ocuupied
      } // for(int currentPortInPool: dataPortPool) // Check exisintg port pool
      
      if (foundExistingPort) // Found existing port
      {
        Log.d(TAG, CodePosition.newInstance().toString() + ", use existing port: " + result);
        // data_port=
      } // if (foundExistingPort) // Found existing port
      else // not found existing port
      {
        Random random=new Random(); //随机数生成器。

        int randomIndex=random.nextInt(65535-1025)+1025; //随机选择一个端口。

        final int data_port=randomIndex;  // The listening data port.
        result=randomIndex;
        Log.d(TAG, CodePosition.newInstance().toString() + ", use new port: " + result);

        AsyncServer.getDefault().listen(host, data_port, new ListenCallback() 
        {
          @Override
          public void onAccepted(final AsyncSocket socket)
          {
            dataServerManagerInterface.handleDataAccept(socket);
            
            boolean dataPortUsageCounter=true; // Get the counter.
            // dataPortUsageCounter++;
            dataPortUsageMap.put(data_port, dataPortUsageCounter); // put back.
            
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
                      
                Log.d(TAG, CodePosition.newInstance().toString() + ", [Server] data Successfully end connection " + socket.toString() + ", port: " + data_port);
                
                boolean dataPortUsageCounter=false; // Get the counter.
                dataPortUsageMap.put(data_port, dataPortUsageCounter); // put back.
              }
            });
          } //public void onAccepted(final AsyncSocket socket)

          @Override
          public void onListening(AsyncServerSocket socket)
          {
            listeningServerSocket = socket; // Remember listening server socket.
            // System.out.println("[Server] Server started listening for data connections");
            // Log.d(TAG, CodePosition.newInstance().toString()+  ", [Server] Server started listening for data connections, port: " + data_port); // Debug.
            
            dataPortPool.add(data_port); // Add to data port pool.
            
            boolean dataPortUsageCounter=false; // Not used.
            
            dataPortUsageMap.put(data_port, dataPortUsageCounter); // put back.
            Log.d(TAG, CodePosition.newInstance().toString()+  ", [Server] Server started listening for data connections, port: " + data_port + ", data port pool size: " + dataPortPool.size()); // Debug.
          } // public void onListening(AsyncServerSocket socket)

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
            
            dataPortPool.remove(data_port); // Remove from data port pool.
          } // public void onCompleted(Exception ex) 
        });
      } // else // not found existing port
      
      return result;
    } //private void setupDataServer()
}
