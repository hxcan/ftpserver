package com.stupidbeauty.ftpserver.lib;

// import com.stupidbeauty.hxlauncher.asynctask.LoadVoicePackageNameMapTask;
import java.util.Timer;
import java.util.TimerTask;
// import android.Manifest;
import android.annotation.SuppressLint;
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
// import com.stupidbeauty.hxlauncher.asynctask.VirtualPathLoadInterface;

public class PathDocumentFileCacheManager
{
  private static final String TAG="PathDocumentFileCacheManager"; // !< The tag used to output debug code.
  private HashMap<String, Uri> virtualPathMap=new HashMap<>(); //!< the map of virtual path to uri.
  private HashMap<String, DocumentFile> pathDocumentFileMap=new HashMap<>(); //!< Cache. path to documentfile object.
  // private PathDocumentFileCacheManager pathDocumentFileCacheManager=new PathDocumentFileCacheManager(); //!< The manager of path to documentfile objects cache.
  // private Context context=null; //!< Context.
  // private boolean externalStoragePerformanceOptimize=false; //!< Whether to do external storage performance optimize.
  // private ExternalStorageUriGuessor externalStorageUriGuessor=new ExternalStorageUriGuessor(); //!< Guess the external storage uri.
  
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
  *  Get the uri of specified virtual path.
  */
  public Uri getVirtualPath(String path)
  {
    return virtualPathMap.get(path);
  } // public Uri getVirtualPath(String path)

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
  *  Remove it from the cache.
  */
  public void remove(String effectiveVirtualPathForCurrentSegment)
  {
    pathDocumentFileMap.remove(effectiveVirtualPathForCurrentSegment); // Remove it from the cache.    
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
  public void put(String effectiveVirtualPathForCurrentSegment, DocumentFile  targetdocumentFile) 
  {
    DocumentFile result=null; // Result;
    
    {
//       Uri uri=virtualPathMap.get(wholeDirecotoryPath); // Get the uri.
      
      // for(String currentSegmetn: trialingPathSegments)
      {
        // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn); // Debug.
        // else
        {
          effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment.replace("//", "/"); // Remove consecutive /

          // else // Not exist. Need to find
          {
            if (targetdocumentFile!=null) // Target document exists
            {
              pathDocumentFileMap.put(effectiveVirtualPathForCurrentSegment, targetdocumentFile); // Put it into the cache.
              // pathDocumentFileCacheManager.put(effectiveVirtualPathForCurrentSegment, targetdocumentFile); // Put it into the cache.
            } // if (targetdocumentFile!=null) // Target document exists
          } // else // Not exist
        } // if (currentSegmetn.isEmpty())
      } // //       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
      
//       Chen xin.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + uri.toString()); // Debug.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + uri.toString()  + ", target documetn file: " + targetdocumentFile); // Debug.

//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, targetPathuri);
      
      result=targetdocumentFile;
    } // if (virtualPathMap.contains(wholeDirecotoryPath)) // It is in the virtual path map
  } // public DocumentFile getFile(File rootDirectory, String currentWorkingDirectory, String data51) 
  
  /**
  * resolve file path.
  */
  public DocumentFile get(String effectiveVirtualPathForCurrentSegment) 
  {
    DocumentFile result=null; // Result;
    
    {
//       Uri uri=virtualPathMap.get(wholeDirecotoryPath); // Get the uri.
      DocumentFile targetdocumentFile=null;
      
      // for(String currentSegmetn: trialingPathSegments)
      {
        // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn); // Debug.
        // else
        {
          effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment.replace("//", "/"); // Remove consecutive /

          DocumentFile cachedtargetdocumentFile=pathDocumentFileMap.get(effectiveVirtualPathForCurrentSegment); // Get it from cache.
          // DocumentFile cachedtargetdocumentFile=pathDocumentFileCacheManager.get(effectiveVirtualPathForCurrentSegment); // Get it from cache.
          
          if (cachedtargetdocumentFile!=null) // It exists
          {
            targetdocumentFile=cachedtargetdocumentFile; // Remember it.
          } // if (targetdocumentFile!=null) // It exists
        } // if (currentSegmetn.isEmpty())
      } // //       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
      
//       Chen xin.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + uri.toString()); // Debug.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory + ", uri to use: " + uri.toString()  + ", target documetn file: " + targetdocumentFile); // Debug.

//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
//       DocumentFile documentFile=DocumentFile.fromTreeUri(context, targetPathuri);
      
      result=targetdocumentFile;
    } // if (virtualPathMap.contains(wholeDirecotoryPath)) // It is in the virtual path map
    
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


