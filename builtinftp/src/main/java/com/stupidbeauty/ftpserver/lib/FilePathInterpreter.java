package com.stupidbeauty.ftpserver.lib;

import com.stupidbeauty.hxlauncher.asynctask.LoadVoicePackageNameMapTask;
import java.util.Timer;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
// import com.stupidbeauty.hxlauncher.asynctask.VoicePackageNameMapSaveTask;
// import com.stupidbeauty.hxlauncher.bean.VoiceCommandHitDataObject;
// import com.android.volley.RequestQueue;
// import com.google.gson.Gson;
// import com.google.protobuf.ByteString;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
// import com.stupidbeauty.hxlauncher.listener.BuiltinFtpServerErrorListener; 
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
import com.stupidbeauty.hxlauncher.asynctask.VirtualPathLoadInterface;

public class FilePathInterpreter implements VirtualPathLoadInterface
{
  private static final String TAG="FilePathInterpreter"; // !< The tag used to output debug code.
  private HashMap<String, Uri> virtualPathMap=new HashMap<>(); //!< the map of virtual path to uri.
  private Context context=null; //!< Context.
  
  /**
  * Forbidden default constrctor to help migrate to member object.
  */
  private FilePathInterpreter()
  {
//     loadVirtualPathMap(); // Load the virtual path map.
  } // FilePathInterpreter
  
  /**
  *  Whether the two are same path.
  */
  public boolean isSamePath (String fullPath, String ConstantsFilePathAndroidData)
  {
    File fullPathFile=new File(fullPath);
    File constantsFilePathAndroidDataFile=new File(ConstantsFilePathAndroidData);
    
    boolean result= fullPathFile.getPath().equals(constantsFilePathAndroidDataFile.getPath());
    Log.d(TAG, CodePosition.newInstance().toString()+  ", full path : " + fullPath + ", other path: " + ConstantsFilePathAndroidData + ", result: " + result); // Debug.
    
    return result;
  } // public boolean isSamePath (String fullPath, String ConstantsFilePathAndroidData)
  
  /**
    * 载入 virtual path to uri 之间的映射。
    */
  public void loadVirtualPathMap()
  {
    LoadVoicePackageNameMapTask translateRequestSendTask =new LoadVoicePackageNameMapTask(); //创建异步任务。

    translateRequestSendTask.execute(this, context); //执行任务。
  } //private void loadVoicePackageNameMap()

  /**
  * Create the file path interpreter.
  */
  public static FilePathInterpreter migrateCreateFilePathInterpreter() 
  {
    return new FilePathInterpreter();
  } // public static FilePathInterpreter migrateCreateFilePathInterpreter()
  
  /**
    * 保存映射。 The virtual path map.
    */ 
  private void saveVirtualPathMap()
  {
    VirtualPathMapSaveTask translateRequestSendTask =new VirtualPathMapSaveTask(); // 创建异步任务。

    translateRequestSendTask.execute(virtualPathMap, context); // 执行任务。
  } //private void saveVoicePackageNameMap()

  @Override
  /**
  * Set the virtual path map.
  */
  public void  setVoicePackageNameMap (HashMap<String, Uri> voicePackageNameMap)
  {
    virtualPathMap=voicePackageNameMap;
  } // public void  setVoicePackageNameMap (HashMap<String, Uri> voicePackageNameMap)
  
  /**
  * Mount virtual path.
  */
  public void mountVirtualPath(String fullPath, Uri uri)
  {
    Log.d(TAG, CodePosition.newInstance().toString()+  ", full path : " + fullPath + ", uri to use: " + uri.toString()); // Debug.
    virtualPathMap.put(fullPath, uri); // Put it into the map.
    
    saveVirtualPathMap(); // Save the virtual path map.
  } // public void mountVirtualPath(String fullPath, Uri uri)

  public void setContext(Context context)
  {
    this.context=context;
  } // fileContentSender
  
  /**
  * Get the uri. of a virtual path.
  */
  private Uri getParentUriByVirtualPathMap(String wholeDirecotoryPath) 
  {
    String currentTryingPath=getParentVirtualPathByVirtualPathMap(wholeDirecotoryPath); // Get the paretn virtual path map.
    
    Uri result=null;
    
    result=virtualPathMap.get(currentTryingPath); // Get the uri.
    //       Uri uri=virtualPathMap.get(wholeDirecotoryPath); // Get the uri.

    return result;
  } // private Uri getParentUriByVirtualPathMap(String wholeDirecotoryPath)
  
  /**
  * Get the paretn virtual path map.
  */
  private String getParentVirtualPathByVirtualPathMap(String wholeDirecotoryPath)
  {
    boolean result=false;

    String currentTryingPath=wholeDirecotoryPath;

    Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.

    String theFinalPath=null; // The final path.
    
    while((!currentTryingPath.equals("/")) && (!result)) // Not to root
    {
      result=virtualPathMap.containsKey(currentTryingPath);
      Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.
      
      if (result) // Found it
      {
        break;
      } //  if (result) // Found it

      File virtualFile=new File(currentTryingPath);
      
      File parentVirtualFile=virtualFile.getParentFile();
      
      currentTryingPath=parentVirtualFile.getPath();
      
      if (currentTryingPath.endsWith("/")) // Ends iwth /
      {
      } // if (currentTryingPath.endsWith("/")) // Ends iwth /
      else // NOt end with /
      {
//         currentTryingPath.append("/"); // Append /
        currentTryingPath=currentTryingPath+"/"; // Append /
      } // else // NOt end with /
      Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.
    } // while(!currentTryingPath.equals("/")) // Not to root
    
    if (result) // Found virtual path
    {
      theFinalPath=currentTryingPath;
    } // if (result) // Found virtual path
    Log.d(TAG, CodePosition.newInstance().toString()+  ", the final Path : " + theFinalPath + ", result: " + result); // Debug.

    return theFinalPath;
  } // private String getParentVirtualPathByVirtualPathMap(String wholeDirecotoryPath)
  
  /**
  * Does virtual path exist
  */
  public boolean virtualPathExists(String ConstantsFilePathAndroidData)
  {
    boolean result=false;
    
    

    String currentTryingPath=getParentVirtualPathByVirtualPathMap(ConstantsFilePathAndroidData); // Get the paretn virtual path map.
    
    if (currentTryingPath!=null)
    {
      result=true;
    }

    return result;
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
    
//     if (wholeDirecotoryPath.endsWith("/")) // Ends with /
//     {
//       String bbPackageString=wholeDirecotoryPath.substring(0, wholeDirecotoryPath.length()-1);
//       wholeDirecotoryPath=bbPackageString;
// 
//       Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory); // Debug.
//     } // if (wholeDirecotoryPath.endWith("/"))

//     Log.d(TAG, "getFile: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
    Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory); // Debug.

    File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

    if (virtualPathExists(wholeDirecotoryPath)) // It is in the virtual path map
    {
//       Uri uri=virtualPathMap.get(wholeDirecotoryPath); // Get the uri.
      Uri uri=  getParentUriByVirtualPathMap(wholeDirecotoryPath); // Get the uri.
      DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
      
      String parentVirtualPath=getParentVirtualPathByVirtualPathMap(wholeDirecotoryPath); // Get the paretn virtual path map.
      
      String trailingPath=wholeDirecotoryPath.substring(parentVirtualPath.length(), wholeDirecotoryPath.length());
      
      
      String[] trialingPathSegments=trailingPath.split("/");
      
      DocumentFile targetdocumentFile=documentFile;
      
      for(String currentSegmetn: trialingPathSegments)
      {
        if (currentSegmetn.isEmpty())
        {
        }
        else
        {
          targetdocumentFile=targetdocumentFile.findFile(currentSegmetn);
        } // if (currentSegmetn.isEmpty())
      } // //       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);

      
//       Chen xin.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + uri.toString()); // Debug.

//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, targetPathuri);
      
      result=targetdocumentFile;
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
    Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + result.getUri().toString()); // Debug.

    return result;
  } // public DocumentFile getFile(File rootDirectory, String currentWorkingDirectory, String data51) 
} // public class FilePathInterpreter implements VirtualPathLoadInterface

