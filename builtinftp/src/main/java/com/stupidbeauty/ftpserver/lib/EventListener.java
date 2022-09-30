package com.stupidbeauty.ftpserver.lib;

public interface EventListener
{
  public static final String DELETE = "com.stupidbeauty.ftpserver.lib.delete"; //!< 文件被删除。
  public static final String DOWNLOAD_FINISH = "com.stupidbeauty.ftpserver.lib.download_finish"; //!< 文件下载完毕。
  public static final String DOWNLOAD_START = "com.stupidbeauty.ftpserver.lib.download_start"; //!< 文件下载开始。
  public static final String IP_CHANGE = "com.stupidbeauty.ftpserver.lib.ip_change"; //!< Ip changed.

  public void onEvent(String eventCode) ; //!< Event occured.
  public void onEvent(String eventCode, String information) ; //!< Event occured. With information
}

