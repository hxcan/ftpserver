package com.stupidbeauty.ftpserver.lib;

import java.net.BindException;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.AsyncServer;
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

public class FtpServer 
{
  private UserManager userManager=null; //!< user manager.
  private EventListener eventListener=null; //!< Event listener.
  private ErrorListener errorListener=null; //!< Error listener. Chen xin. 
  private Context context; //!< 执行时使用的上下文。
  private static final String TAG="FtpServer"; //!< 输出调试信息时使用的标记
  private InetAddress host;
  private int port;
  private boolean allowActiveMode=true; //!< 是否允许主动模式。
  private File rootDirectory=null; //!< 根目录。
    
  public void setEventListener(EventListener eventListener)
  {
    this.eventListener=eventListener;
  } //public void setEventListener(eventListener)
    
  public void setRootDirectory(File root)
  {
    rootDirectory=root;
  }
        
  public void setErrorListener(ErrorListener errorListener)    
  {
    this.errorListener = errorListener;
  } //public void setErrorListener(ErrorListener errorListener)    

  public FtpServer(String host, int port, Context context, boolean allowActiveMode) 
  {
    this(host, port, context, allowActiveMode, null);
  } //public FtpServer(String host, int port, Context context, boolean allowActiveMode) 

  public FtpServer(String host, int port, Context context, boolean allowActiveMode, ErrorListener errorListener) 
  {
    this.context=context;
    this.allowActiveMode=allowActiveMode;
    this.errorListener=errorListener; // 记录错误事件监听器。
        
    rootDirectory=context.getFilesDir(); // 默认在家目录下工作。
        
    try 
    {
      this.host = InetAddress.getByName(host);
    }
    catch (UnknownHostException e) 
    {
      throw new RuntimeException(e);
    }

    this.port = port;

    setup();
  } //public FtpServer(String host, int port, Context context, boolean allowActiveMode)
  
  /**
  * Set user manager.
  */
  public void setUserManager(UserManager userManager)
  { 
    this.userManager=userManager;
  } // public void setUserManager(UserManager userManager)

  private void setup()
  {
    AsyncServer.getDefault().listen(host, port, new ListenCallback() 
    {
      @Override
      public void onAccepted(final AsyncSocket socket)
      {
        ControlConnectHandler handler=new ControlConnectHandler(context, allowActiveMode, host); // 创建处理器。
        handler.handleAccept(socket);
        handler.setRootDirectory(rootDirectory); // 设置根目录。
        handler.setEventListener(eventListener); // 设置事件监听器。
        handler.setUserManager(userManager); // set user manager.
      }

      @Override
      public void onListening(AsyncServerSocket socket)
      {
        System.out.println("[Server] Server started listening for connections");
      }

      @Override
      public void onCompleted(Exception ex) 
      {
        if(ex != null) 
        {
          if ( ex instanceof BindException )
          {
            if (errorListener!=null) // 指定的错误监听器。
            {
              errorListener.onError(Constants.ErrorCode.ADDRESS_ALREADY_IN_USE); // Report error. Chen xin.
            } // if (errorListener!=null) // 指定的错误监听器。
            else // No error listener
            {
              Log.d(TAG, "onCompleted, no error listener set, throwing exception."); // Debug.
                
                throw new RuntimeException(ex);
              } //else // No error listener
            }
            else // Other exceptions
            {
              throw new RuntimeException(ex);
            }
          }
          System.out.println("[Server] Successfully shutdown server");
        }
    });
  }
}
