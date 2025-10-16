package com.stupidbeauty.ftpserver.lib;

import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ListenCallback;

import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import com.stupidbeauty.ftpserver.lib.DocumentTreeBrowseRequest;
import android.widget.Button;

import android.os.Debug;

import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.AsyncServerSocket;

import com.stupidbeauty.ftpserver.lib.EventListener;
import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import java.util.Random;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import android.content.ClipboardManager;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Debug;

import java.util.Timer;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.CheckBox;
import android.widget.ImageView;

import android.widget.RelativeLayout;

public class RenameInformationObject
{
  private static final String TAG="RenameInformationObject"; //!< 输出调试信息时使用的标记

  private Timer timerObj = null; //!< 用于报告下载完毕的定时器。

  private String originalName = null; //!< The original file name.
  private DocumentFile file; //!< the file object.
  
  /**
  * Get the original name.
  */
  public String getOriginalName()
  {
    return originalName;
  } // public String getOriginalName()

  /**
  * SEt the original name.
  */
  public void setOriginalName(String originalName)
  {
    this.originalName = originalName;
  } // public void setOriginalName(String originalName)
  
  /**
  * Gett he file object.
  */
  public DocumentFile getFile()
  {
    return file;
  } // public DocumentFile getFile()
  
  /**
  * SEt the file object.
  */
  public void setFile(DocumentFile photoDirecotry)
  {
    this.file = photoDirecotry;
  } // public void setFile(DocumentFile photoDirecotry)
  
    /**
    * 告知文件下载开始。
    */
    public void notifyDownloadStart()
    {
      cancelNotifyDownloadFinish(); // 取消通知。
    } // notifyDownloadStart(); // 告知文件下载开始。
    
    /**
    * 取消通知，文件下载完毕。
    */
    private void cancelNotifyDownloadFinish() 
    {
      if (timerObj!=null) // 定时器存在
      {
        timerObj.cancel(); // 取消。
      } // if (timerObj!=null) // 定时器存在
    } // private void cancelNotifyDownloadFinish()
}
