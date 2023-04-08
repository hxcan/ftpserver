package com.stupidbeauty.ftpserver.lib;

import com.stupidbeauty.hxlauncher.asynctask.LoadVoicePackageNameMapTask;
import java.util.Timer;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
// import com.stupidbeauty.hxlauncher.asynctask.VoicePackageNameMapSaveTask;
// import com.stupidbeauty.hxlauncher.bean.VoiceCommandHitDataObject;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
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
// import com.koushikdutta.async.callback.DataCallback;
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
  private boolean externalStoragePerformanceOptimize=false; //!< Whether to do external storage performance optimize.
  private ExternalStorageUriGuessor externalStorageUriGuessor=new ExternalStorageUriGuessor(); //!< Guess the external storage uri.
  
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
  *  Get the uri of specified virtual path.
  */
  public Uri getVirtualPath(String path)
  {
    return virtualPathMap.get(path);
  } // public Uri getVirtualPath(String path)

  /**
  * Set option. Whether to do external storage perforamnce optimize.
  */
  public void setExternalStoragePerformanceOptimize(boolean isChecked)
  {
    externalStoragePerformanceOptimize=isChecked; // Remember the option.
  } // public void setExternalStoragePerformanceOptimize(boolean isChecked)
  
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
    
    externalStorageUriGuessor.setContext(context);
  } // fileContentSender
  
  /**
  * Get the uri. of a virtual path.
  */
  private Uri getParentUriByVirtualPathMap(String wholeDirecotoryPath) 
  {
    String currentTryingPath=getParentVirtualPathByVirtualPathMap(wholeDirecotoryPath); // Get the paretn virtual path map.
    
    Uri result=null;
    
    result=virtualPathMap.get(currentTryingPath); // Get the uri.
    
    if (externalStoragePerformanceOptimize) // Need to do external storage performance optimize
    {
      result=externalStorageUriGuessor.guessUri(result); // Guess the uri.
    } // if (externalStoragePerformanceOptimize) // Need to do external storage performance optimize
    
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

    // Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.

    String theFinalPath=null; // The final path.
    
    while((!currentTryingPath.equals("/")) && (!result)) // Not to root
    {
      result=virtualPathMap.containsKey(currentTryingPath);
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.
      
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
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.
    } // while(!currentTryingPath.equals("/")) // Not to root
    
    if (result) // Found virtual path
    {
      theFinalPath=currentTryingPath;
    } // if (result) // Found virtual path
    // Log.d(TAG, CodePosition.newInstance().toString()+  ", the final Path : " + theFinalPath + ", result: " + result); // Debug.

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
  * resolve 完整路径。
  */
  public String resolveWholeDirectoryPath( File rootDirectory, String currentWorkingDirectory, String data51) 
  {
    String currentWorkingDirectoryUpdate=currentWorkingDirectory; // 更新后的当前工作目录。
    
    if (data51.startsWith("/")) // 绝对路径。
    {
      currentWorkingDirectoryUpdate="/"; // 当前工作目录更新为根目录。
    }
  
    String wholeDirecotoryPath = rootDirectory.getPath() + currentWorkingDirectoryUpdate + "/" + data51; // 构造完整路径。

    wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠

    return wholeDirecotoryPath;
  } // public String resolveWholeDirectoryPath( File rootDirectory, String currentWorkingDirectory, String data51)
  
  /**
  * resolve file path.
  */
  public DocumentFile getFile(File rootDirectory, String currentWorkingDirectory, String data51) 
  {
    DocumentFile result=null; // Result;
    
    String wholeDirecotoryPath = resolveWholeDirectoryPath( rootDirectory, currentWorkingDirectory, data51); // resolve 完整路径。

    // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory); // Debug.

    File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

    Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ", check virtual exists"); // Debug.
    if (virtualPathExists(wholeDirecotoryPath)) // It is in the virtual path map
    {
//       Uri uri=virtualPathMap.get(wholeDirecotoryPath); // Get the uri.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  virtual path exists"); // Debug.
      Uri uri=  getParentUriByVirtualPathMap(wholeDirecotoryPath); // Get the uri.
      // DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri); // 04-08 18:22:04.279 15010 15045 W System.err: java.lang.IllegalArgumentException: Invalid URI: file:///storage/emulated/0/DCIM/GoddessCamera
      DocumentFile documentFile=getDocumentFileFromUri(context, uri); // 04-08 18:22:04.279 15010 15045 W System.err: java.lang.IllegalArgumentException: Invalid URI: file:///storage/emulated/0/DCIM/GoddessCamera
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  parent document file: " + documentFile); // Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  parent uri: " + uri); // Debug.
      String parentVirtualPath=getParentVirtualPathByVirtualPathMap(wholeDirecotoryPath); // Get the paretn virtual path map.
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  parent virtual path: " + parentVirtualPath); // Debug.
      String trailingPath=wholeDirecotoryPath.substring(parentVirtualPath.length(), wholeDirecotoryPath.length());
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath); // Debug.
      
      String[] trialingPathSegments=trailingPath.split("/");
      
      DocumentFile targetdocumentFile=documentFile;
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", target document: " + targetdocumentFile.getUri().toString()); // Debug.
      for(String currentSegmetn: trialingPathSegments)
      {
        // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn); // Debug.
        if (currentSegmetn.isEmpty())
        {
          Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", skip"); // Debug.
        }
        else
        {
          targetdocumentFile=targetdocumentFile.findFile(currentSegmetn);
          
          // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document object: " + targetdocumentFile); // Debug.
          if (targetdocumentFile!=null) // Target document exists
          {
            Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document: " + targetdocumentFile.getUri().toString()); // Debug.
          } // if (targetdocumentFile!=null) // Target document exists
        } // if (currentSegmetn.isEmpty())
      } // //       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
      
//       Chen xin.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + uri.toString()); // Debug.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + uri.toString()  + ", target documetn file: " + targetdocumentFile); // Debug.

//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, targetPathuri);
      
      result=targetdocumentFile;
    } // if (virtualPathMap.contains(wholeDirecotoryPath)) // It is in the virtual path map
    else // Not in the virtual path map
    {
      if (photoDirecotry.exists()) // 文件存在
      {
      } // if (photoDirecotry.exists()) // 文件存在
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
    
    Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", result object: " + result); // Debug.

    if (result!=null) // The result exists
    {
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + result.getUri().toString()); // Debug.
    } // if (result!=null) // The result exists

    return result;
  } // public DocumentFile getFile(File rootDirectory, String currentWorkingDirectory, String data51) 
  
  /**
  * Get docuemtn file from uri
  */
  private DocumentFile getDocumentFileFromUri(Context context, Uri uri)
  {
    DocumentFile result;

    if (uri.getScheme().equals("file")) // it is a raw file
    {
      File photoDirecotry=new File(uri.getPath()); // Create a file object from the path.
      result=DocumentFile.fromFile(photoDirecotry);
    } // if (uri.getScheme().equals("file")) // it is a raw file
    else // Not a raw file
    {
      result=DocumentFile.fromTreeUri(context, uri); // 04-08 18:22:04.279 15010 15045 W System.err: java.lang.IllegalArgumentException: Invalid URI: file:///storage/emulated/0/DCIM/GoddessCamera
    } // else // Not a raw file

    return result;
  } // private DocumentFile getDocumentFileFromUri(Context context, Uri uri)
} // public class FilePathInterpreter implements VirtualPathLoadInterface

