package com.stupidbeauty.ftpserver.lib;

public interface EventListener
{
  public static final String DELETE = "com.stupidbeauty.ftpserver.lib.delete"; //!< 文件被删除。
  public static final String DOWNLOAD_FINISH = "com.stupidbeauty.ftpserver.lib.download_finish"; //!< 文件下载完毕。
  public static final String UPLOAD_FINISH = "com.stupidbeauty.ftpserver.lib.upload_finish"; //!< File upload finished.
  public static final String DOWNLOAD_START = "com.stupidbeauty.ftpserver.lib.download_start"; //!< 文件下载开始。
  public static final String IP_CHANGE = "com.stupidbeauty.ftpserver.lib.ip_change"; //!< Ip changed.
  public static final String NEED_BROWSE_DOCUMENT_TREE = "com.stupidbeauty.ftpserver.lib.need_browse_document_tree"; //!< Need to browse document tree using storage access framework.

  @Deprecated
  public void onEvent(String eventCode) ; //!< Event occured.
  
  public void onEvent(String eventCode, Object extraContent) ; //!< Event occured.
}

