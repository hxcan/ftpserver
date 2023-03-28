package com.stupidbeauty.ftpserver.lib;

import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import com.koushikdutta.async.Util;
import android.util.Log;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.HashMap;
import android.view.View;
import android.os.AsyncTask;
import java.util.HashMap;

public class FilePathInterpreter
{
  private static final String TAG="FilePathInterpreter"; // !< The tag used to output debug code.
  private HashMap<String, Uri> virtualPathMap=new HashMap<>(); //!< the map of virtual path to uri.
  private Context context=null; //!< Context.
  
  /**
  * Forbidden default constrctor to help migrate to member object.
  */
  private FilePathInterpreter()
  {
  } // FilePathInterpreter
  
  /**
  * Create the file path interpreter.
  */
  public static FilePathInterpreter migrateCreateFilePathInterpreter() 
  {
    return new FilePathInterpreter();
  } // public static FilePathInterpreter migrateCreateFilePathInterpreter()
  
  /**
  * Mount virtual path.
  */
  public void mountVirtualPath(String fullPath, Uri uri)
  {
    virtualPathMap.put(fullPath, uri); // Put it into the map.
  } // public void mountVirtualPath(String fullPath, Uri uri)

  public void setContext(Context context)
  {
    this.context=context;
  } // fileContentSender
  
  /**
  * Does virtual path exist
  */
  public boolean virtualPathExists(String ConstantsFilePathAndroidData)
  {
    return virtualPathMap.containsKey(ConstantsFilePathAndroidData);
  } // public boolean virtualPathExists(String ConstantsFilePathAndroidData) // Does virtual path exist
  
  
  /**
  * resolve file path.
  */
  public DocumentFile getFile(File rootDirectory, String currentWorkingDirectory, String data51) 
  {
    DocumentFile result=null; // Result;
    String currentWorkingDirectoryUpdate=currentWorkingDirectory; // 更新后的当前工作目录。
    
    if (data51.startsWith("/")) // 绝对路径。
    {
      currentWorkingDirectoryUpdate="/"; // 当前工作目录更新为根目录。
    }
  
    String wholeDirecotoryPath = rootDirectory.getPath() + currentWorkingDirectoryUpdate + "/" + data51; // 构造完整路径。

    wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠

    Log.d(TAG, "getFile: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.

    File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

    if (virtualPathExists(wholeDirecotoryPath)) // It is in the virtual path map
    {
      Uri uri=virtualPathMap.get(wholeDirecotoryPath); // Get the uri.
      
//       Chen xin.
      
      DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
      
      result=documentFile;
    } // if (virtualPathMap.contains(wholeDirecotoryPath)) // It is in the virtual path map
    else // Not in the virtual path map
    {
      if (photoDirecotry.exists()) // 文件存在
      {
      } //if (photoDirecotry.exists()) // 文件存在
      else // 文件不 存在
      {
        //       wholeDirecotoryPath = rootDirectory.getPath() + data51; // 构造完整路径。
        // 
        //       wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
        // 
        //       photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
      } //else // 文件不 存在
      
//       result=photoDirecotry;
      result=DocumentFile.fromFile(photoDirecotry);
    } // else // Not in the virtual path map


    return result;
  }
}

