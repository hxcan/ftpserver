package com.stupidbeauty.ftpserver.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.net.SocketException;
import android.net.LinkAddress;
import android.net.LinkProperties;
import java.util.Enumeration;
import java.net.SocketException;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import java.net.SocketException;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.ConnectivityManager;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
import android.os.Environment;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.ConnectivityManager;
import java.nio.ByteOrder;
import java.nio.ByteOrder;
import java.math.BigInteger;
import android.net.wifi.WifiManager;
import java.util.Random;
import java.net.InetAddress;
import java.math.BigInteger;
import android.net.wifi.WifiManager;
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
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
  private FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); //!< Create the file path interpreter.
  private UserManager userManager=null; //!< user manager.
  private EventListener eventListener=null; //!< Event listener.
  private ErrorListener errorListener=null; //!< Error listener. Chen xin. 
  private Context context; //!< 执行时使用的上下文。
  private static final String TAG="FtpServer"; //!< 输出调试信息时使用的标记
  private InetAddress host;
  private int port;
  private String ip; //!< ip
  private boolean allowActiveMode=true; //!< 是否允许主动模式。
  private boolean autoDetectIp=true; //!< Whether we should detect ip automatically.
  private boolean fileNameTolerant=false; //!< Set the file name tolerant mode.
  private File rootDirectory=null; //!< 根目录。
  private WIFIConnectChangeReceiver wifiConnectChangeReceiver=new WIFIConnectChangeReceiver(this); //!< 无线网络改变事件接收器。
  private AsyncServerSocket listeningServerSocket = null; //!< Remembered listening server socket.
  private List<ControlConnectHandler> controlConnectHandlerList = new ArrayList<>(); //!< Control conenct handler list.

  /**
  * Get the actual ip.
  */
  public String getIp()
  {
    return ip;
  } // public String getIp()

  public void setIp(String externalIp)
  {
    this.ip=externalIp; // Remember the external ip.
    autoDetectIp=false; // Do not detect ip automatically if set ip explicitly.
  } //public FtpServer(String host, int port, Context context, boolean allowActiveMode)
  
  private String getIpAddress()
  {
    String ip = "";
    boolean found=false;
    try
    {
      Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (enumNetworkInterfaces.hasMoreElements())
      {
        NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
        Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
        while (enumInetAddress.hasMoreElements())
        {
          InetAddress inetAddress = enumInetAddress.nextElement();

          if (inetAddress.isSiteLocalAddress())
          {
            ip = inetAddress.getHostAddress();
            Log.d(TAG, "164, getIpAddress, ipAddress: " + ip); // Debug.

            if (ip.startsWith("192.168."))
            {
              found=true;
              break;
            }
          }
        }
        if (found)
        {
          break;
        }
      }
    }
    catch (SocketException e)
    {
      e.printStackTrace();
      ip += "Something Wrong! " + e.toString() + "\n";
    }
    return ip;
  }

  private String getByConnectivityManager()
  {
    ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

    Network network=conMgr.getActiveNetwork();
    LinkProperties linkProperties=conMgr.getLinkProperties(network);
    String ipAddressString= null;

    if (linkProperties!=null) // The link properties exist
    {
      List<LinkAddress> linkAddresses= linkProperties.getLinkAddresses ();

      InetAddress inetAddress=linkAddresses.get(0).getAddress();

      ipAddressString= inetAddress.getHostAddress();
    } // if (linkProperties!=null) // The link properties exist

    return ipAddressString;
  }

  private String getHotspotIPAddress()
  {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

//     int ipAddress = wifiManager.getDhcpInfo().serverAddress;
    int ipAddress = wifiManager.getDhcpInfo().gateway;

    Log.d(TAG, "114, getHotspotIPAddress, ipAddress: " + ipAddress); // Debug.

    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
    {
      ipAddress = Integer.reverseBytes(ipAddress);
      Log.d(TAG, "152, getHotspotIPAddress, ipAddress: " + ipAddress); // Debug.
    }

    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

    Log.d(TAG, "157, getHotspotIPAddress, ipByteArray: " + ipByteArray.toString()); // Debug.

    String ipAddressString;
    try
    {
      ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
      Log.d(TAG, "163, getHotspotIPAddress, ipAddressString: " + ipAddressString); // Debug.
    }
    catch (UnknownHostException ex)
    {
      ipAddressString = "";
      Log.d(TAG, "168, getHotspotIPAddress, ipAddressString: " + ipAddressString); // Debug.
    }

    return ipAddressString;
  }

  /**
  * Detect the ip.
  */
  private String detectIp()
  {
    String ipAddress = null;

    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

    Log.d(TAG, "109, detectIp, ipAddress: " + ipAddress); // Debug.

    if (ipAddress.equals("0.0.0.0")) // hotspot
    {
      ipAddress= getHotspotIPAddress(); // Get hotspot ip addrss
      Log.d(TAG, "114, detectIp, ipAddress: " + ipAddress); // Debug.

      ipAddress= getByConnectivityManager(); // Get hotspot ip addrss
      Log.d(TAG, "117, detectIp, ipAddress: " + ipAddress); // Debug.

      ipAddress= getIpAddress(); // Get hotspot ip addrss
      Log.d(TAG, "120, detectIp, ipAddress: " + ipAddress); // Debug.
    } // if (ipAddress.equals("0.0.0.0")) // hotspot

    return ipAddress;
  } // private String detectIp()

  /**
  * 发送广播，连接状态变化。	
  */
  public void noticeConnectChange(String ssidName,  boolean connected, int connect_type)
  {
    if (autoDetectIp)
    {
      String newIp=detectIp(); // Detect ip.
      
      if (newIp.equals(ip)) // Equals
      {
      } // if (newIp.equals(ip)) // Equals
      else // NOt equal
      {
        ip=newIp; // Remember ip.
        notifyEvent(EventListener.IP_CHANGE); // 报告事件， ip changed.
      } // else // NOt equal
    } // if (autoDetectIp)
  } // public void noticeConnectChange(String ssidName,  boolean connected, int connect_type)

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
          eventListener.onEvent(eventCode, null); // 报告事件。
        } //public void run()
      };

      uiHandler.post(runnable);
    } //if (eventListener!=null) // 有事件监听器。
  } //private void notifyEvent(String eventCode)


  /**
  * Set if we should detect ip automatically.
  */
  public void setAutoDetectIp(boolean autoDetectIp)
  {
    this.autoDetectIp = autoDetectIp;
    registerWlanChangeListener(); // Register wlan change listener.
  } // public void setAutoDetectIp(boolean autoDetectIp)
    
  /**
    * 注册广播事件接收器。
    */
  private void registerWlanChangeListener() 
  {
    //注册无线网变化监听器：
    //注册全局广播：
    IntentFilter filterWifiChange = new IntentFilter();
    filterWifiChange.addAction("android.net.conn.CONNECTIVITY_CHANGE"); //监听连接改变事件。

//     陈欣
    context.registerReceiver(wifiConnectChangeReceiver, filterWifiChange); //注册接收器。
  } //private void registerBroadcastReceiver()

  public void setEventListener(EventListener eventListener)
  {
    this.eventListener=eventListener;
  } //public void setEventListener(eventListener)
    
  public void setRootDirectory(File root)
  {
    rootDirectory=root;
  }

  /**
  *  Get the uri of specified virtual path.
  */
  public Uri getVirtualPath(String path)
  {
    String fullPath=Constants.FilePath.ExternalRoot + path; // Construct full path.
    return filePathInterpreter.getVirtualPath(fullPath);
  } // public Uri getVirtualPath(String path)

  /**
  * File name tolerant. For example: /Android/data/com.client.xrxs.com.xrxsapp/files/XrxsSignRecordLog/Zw40VlOyfctCQCiKL_63sg==, with a trailing <LF> (%0A).
  */
  public void setFileNameTolerant(boolean toleranttrue)
  {
    fileNameTolerant=toleranttrue; // Remember.
  } // public void setFileNameTolerant(boolean toleranttrue)
  
  /**
  * Set option. Whether to do external storage perforamnce optimize.
  */
  public void setExternalStoragePerformanceOptimize(boolean isChecked)
  {
    filePathInterpreter.setExternalStoragePerformanceOptimize(isChecked);
  } // public void setExternalStoragePerformanceOptimize(boolean isChecked)
  
  /**
  *  un Mount virtual path.
  */
  public void unmountVirtualPath(String path)
  {
    String fullPath=Constants.FilePath.ExternalRoot + path; // Construct full path.

    filePathInterpreter.unmountVirtualPath(fullPath); // un Mount virtual path.
  } // public void unmountVirtualPath(String path)
  
  /**
  * Mount virtual path.
  */
  public void mountVirtualPath(String path , Uri uri)
  {
    boolean takePermission = true; // Take the permission.
    
    mountVirtualPath(path , uri, takePermission); // mount it.
  } // public void mountVirtualPath(String path , Uri uri)
  
  /**
  * Mount virtual path.
  */
  public void mountVirtualPath(String path , Uri uri, boolean takePermission)
  {
    Log.d(TAG, CodePosition.newInstance().toString()+  ", path: " + path + ", uri to use: " + uri.toString()); // Debug.
//     ftpServer.answerBrowseDocumentTreeReqeust(requestCode, uri);
//     Chen xin

    String fullPath=Constants.FilePath.ExternalRoot + path; // Construct full path.

    filePathInterpreter.mountVirtualPath(fullPath, uri); // Mount virtual path.
    
    if (takePermission) // We shoudl take the permisison.
    {
      //     int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      // Check for the freshest data.
      context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
    } // if (takePermission) // We shoudl take the permisison.
  } // public void mountVirtualPath(String path , Uri uri)
  
  /**
  * Answ4er the browse docuembnt tree reqeust.
  */
  public void answerBrowseDocumentTreeReqeust(int requestCode, Uri uri) 
  {
    Log.d(TAG, CodePosition.newInstance().toString()+  ", request code: " + requestCode + ", uri to use: " + uri.toString()); // Debug.
//     ftpServer.answerBrowseDocumentTreeReqeust(requestCode, uri);
//     Chen xin

    String fullPath=Constants.FilePath.AndroidData; // /Android/data

    filePathInterpreter.mountVirtualPath(fullPath, uri); // Mount virtual path.
    
//     int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    // Check for the freshest data.
    context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
  } // public void answerBrowseDocumentTreeReqeust(int requestCode, Uri uri)

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
    this(host, port, context, allowActiveMode, errorListener, null);
  } //public FtpServer(String host, int port, Context context, boolean allowActiveMode)
  
  public FtpServer(String host, int port, Context context, boolean allowActiveMode, ErrorListener errorListener, String externalIp)
  {
    this.context=context;
    this.allowActiveMode=allowActiveMode;
    this.errorListener=errorListener; // 记录错误事件监听器。
    
    this.ip=externalIp; // Remember the external ip.
    autoDetectIp=false; // Do not detect ip automatically if set ip explicitly.

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

    filePathInterpreter.setContext(context); // SEt the context.
    filePathInterpreter.loadVirtualPathMap(); // Load the virtual path map.
    
    registerWlanChangeListener(); // Register wlan change listener.
  } //public FtpServer(String host, int port, Context context, boolean allowActiveMode)

  /**
  * Set user manager.
  */
  public void setUserManager(UserManager userManager)
  { 
    this.userManager=userManager;
  } // public void setUserManager(UserManager userManager)
  
  /**
  * Stop the ftp server.
  */
  public void stop()
  {
    if (listeningServerSocket!=null) // The socket exists
    {
      listeningServerSocket.stop(); // Stop.
    } // if (listeningServerSocket!=null) // The socket exists
    
    for(ControlConnectHandler currentPortInPool: controlConnectHandlerList) // Close the existing control connections.
    {
      currentPortInPool.stop(); // Stop the control connectin.
    } // for(int currentPortInPool: dataPortPool) // Check exisintg port pool
    
    controlConnectHandlerList.clear(); // forget all of them.
  } // public void stop()

  private void setup()
  {
    AsyncServer.getDefault().listen(host, port, new ListenCallback() 
    {
      @Override
      public void onAccepted(final AsyncSocket socket)
      {
        ControlConnectHandler handler = new ControlConnectHandler(context, allowActiveMode, host, ip); // 创建处理器。
        
        controlConnectHandlerList.add(handler); // Add into the list.
        
        handler.handleAccept(socket);
        handler.setRootDirectory(rootDirectory); // 设置根目录。
        handler.setEventListener(eventListener); // 设置事件监听器。
        handler.setErrorListener(errorListener); // Set error listener.
        handler.setUserManager(userManager); // set user manager.
        handler.setFilePathInterpreter(filePathInterpreter); // Set the file path interpreter.

        handler.setFileNameTolerant(fileNameTolerant); // Set the file name tolerant mode.
        
        notifyEvent(EventListener.CLIENT_CONNECTED); // report event , client connected.
      } // public void onAccepted(final AsyncSocket socket)

      @Override
      public void onListening(AsyncServerSocket socket)
      {
        listeningServerSocket = socket; // Remember listening server socket.
        System.out.println("[Server] Server started listening for connections");
      } // public void onListening(AsyncServerSocket socket)

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
    }); // AsyncServer.getDefault().listen(host, port, new ListenCallback() 
  }
}
