package com.stupidbeauty.ftpserver.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


/**
 * 
 * @author: 蔡火胜
 */
public class WIFIConnectChangeReceiver extends BroadcastReceiver 
{
  public static final String TAG = "WIFIConnectChangeReceiver"; //!<The tag used to output debug info.
  private FtpServer ftpServer=null; //!< The ftpserver object.
  
  public WIFIConnectChangeReceiver(FtpServer ftpServer)
  {
    this.ftpServer=ftpServer;
  } // public WIFIConnectChangeReceiver(FtpServer ftpServer)
	
  /**
  * 无线網连接情况改变。
  * @param context 上下文。
  */
  public void onWifiConnectChange(Context context)
  {
    ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); //获取连接状态管理器。
    NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); //获取无线網状态信息。
		
    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); //获取无线网络管理器。
		
    WifiInfo info = wifi.getConnectionInfo(); //获取无线網连接状态信息。
		
    String ssidName = info.getSSID(); //获取连接到的SSID。
    boolean connected  = false; //是否已连接。
    int connect_type = -1; //连接类型。
		
    // change but connect
    if (wifiInfo != null && wifiInfo.isConnected()) //无线已连接。 
    {
      connect_type = LanImeBaseDef.DATA_CONNECT_STATE_WIFI; //当前连接到无线網。
      connected = true; //已连接。
    } //if (wifiInfo != null && wifiInfo.isConnected()) //无线已连接。
		
    ftpServer.noticeConnectChange(ssidName,  connected,connect_type); //发送广播，连接状态变化。	
  } //public static void onWifiConnectChange(Context context)
	
  @Override
  /**
  * 接收到了广播。
  */
  public void onReceive(Context context, Intent intent) 
  {
    onWifiConnectChange(context); //调用回调函数。
  } //public void onReceive(Context context, Intent intent)
}
