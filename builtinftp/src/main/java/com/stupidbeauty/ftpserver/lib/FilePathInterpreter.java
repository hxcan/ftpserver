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

public class FilePathInterpreter implements VirtualPathLoadInterface
{
  private static final String TAG="FilePathInterpreter"; // !< The tag used to output debug code.
  private HashMap<String, Uri> virtualPathMap=new HashMap<>(); //!< the map of virtual path to uri.
  private HashMap<String, DocumentFile> pathDocumentFileMap=new HashMap<>(); //!< Cache. path to documentfile object.
  private PathDocumentFileCacheManager pathDocumentFileCacheManager=new PathDocumentFileCacheManager(); //!< The manager of path to documentfile objects cache.
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
    * Load the map of virtual path to uri
    */
  public void loadVirtualPathMap()
  {
    LoadVirtualPathMapTask loadVirtualPathMapTask =new LoadVirtualPathMapTask(); // Create the async task

    loadVirtualPathMapTask.execute(this, context); // Execute it.
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
    if (this.virtualPathMap==null)
    {
      this.virtualPathMap = voicePackageNameMap;
    }
    else // NOt null
    {
      Log.d(TAG, CodePosition.newInstance().toString()+  ", existing map : " + virtualPathMap ); // Debug.
      this.virtualPathMap.putAll(voicePackageNameMap);
    } // else // NOt null

    Log.d(TAG, CodePosition.newInstance().toString()+  ", map : " + virtualPathMap ); // Debug.
    // virtualPathMap = voicePackageNameMap;
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
  *  un Mount virtual path.
  */
  public void unmountVirtualPath(String fullPath)
  {
    Log.d(TAG, CodePosition.newInstance().toString()+  ", full path : " + fullPath ); // Debug.
    virtualPathMap.remove(fullPath); // Put it into the map.
    
    saveVirtualPathMap(); // Save the virtual path map.
  } // public void unmountVirtualPath(String path)
  
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

    String currentTryingPath = wholeDirecotoryPath;

    Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.

    String theFinalPath=null; // The final path.
    
    while((!currentTryingPath.equals("/")) && (!result)) // Not to root
    {
      result=virtualPathMap.containsKey(currentTryingPath);
      Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", virtual path map: " + virtualPathMap); // Debug.
      
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
        currentTryingPath = currentTryingPath + "/"; // Append /
      } // else // NOt end with /
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", curent trying Path : " + currentTryingPath + ", result: " + result); // Debug.
    } // while(!currentTryingPath.equals("/")) // Not to root
    
    if (result) // Found virtual path
    {
      theFinalPath=currentTryingPath;
    } // if (result) // Found virtual path
    Log.d(TAG, CodePosition.newInstance().toString()+  ", the final Path : " + theFinalPath + ", result: " + result); // Debug.

    return theFinalPath;
  } // private String getParentVirtualPathByVirtualPathMap(String wholeDirecotoryPath)
  
  /**
  * Check for exact virtual path.
  */
  public boolean isExactVirtualPath(String wholeFilePath)
  {
    boolean result=virtualPathMap.containsKey(wholeFilePath);
    
    return result;
  } // public boolean isExactVirtualPath(String wholeFilePath)

  /**
  * Does virtual path exist
  */
  public boolean virtualPathExists(String ConstantsFilePathAndroidData)
  {
    boolean result = false;
    
    ConstantsFilePathAndroidData = ConstantsFilePathAndroidData.replace("//", "/"); // 双斜杠替换成单斜杠

    Log.d(TAG, CodePosition.newInstance().toString()+  ", checking path : " + ConstantsFilePathAndroidData  ); // Debug.

    String currentTryingPath = getParentVirtualPathByVirtualPathMap(ConstantsFilePathAndroidData); // Get the paretn virtual path map.
    
    if (currentTryingPath!=null) // The virtual path exists
    {
      result=true;
    } // if (currentTryingPath!=null) // The virtual path exists

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
  * Get the path documetnfile cache manager.
  */
  public PathDocumentFileCacheManager getPathDocumentFileCacheManager() 
  {
    return pathDocumentFileCacheManager;
  } // public PathDocumentFileCacheManager getPathDocumentFileCacheManager()
  
  /**
  * resolve file path.
  */
  public DocumentFile getFile(File rootDirectory, String currentWorkingDirectory, String data51) 
  {
    DocumentFile result=null; // Result;
    
    String wholeDirecotoryPath = resolveWholeDirectoryPath( rootDirectory, currentWorkingDirectory, data51); // resolve 完整路径。

    File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
    
    wholeDirecotoryPath = wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠

    if (virtualPathExists(wholeDirecotoryPath)) // It is in the virtual path map
    {
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  virtual path exists"); // Debug.
      
      Uri uri=  getParentUriByVirtualPathMap(wholeDirecotoryPath); // Get the uri.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  virtual path exists, uri: " + uri); // Debug.

      DocumentFile documentFile=getDocumentFileFromUri(context, uri); // 04-08 18:22:04.279 15010 15045 W System.err: java.lang.IllegalArgumentException: Invalid URI: file:///storage/emulated/0/DCIM/GoddessCamera
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  parent document file: " + documentFile); // Debug.
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  parent uri: " + uri); // Debug.
      String parentVirtualPath=getParentVirtualPathByVirtualPathMap(wholeDirecotoryPath); // Get the paretn virtual path map.
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  parent virtual path: " + parentVirtualPath); // Debug.
      String trailingPath=wholeDirecotoryPath.substring(parentVirtualPath.length(), wholeDirecotoryPath.length());
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath); // Debug.
      
      String[] trialingPathSegments=trailingPath.split("/");
      
      DocumentFile targetdocumentFile=documentFile;
      
      Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", target document: " + targetdocumentFile.getUri().toString()); // Debug.
      
      String effectiveVirtualPathForCurrentSegment=parentVirtualPath; // Effective virtual path for current segment. Used for DocumentFile cache.
      
      for(String currentSegmetn: trialingPathSegments)
      {
        // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn); // Debug.
        if (currentSegmetn.isEmpty()) // Skip empty segment
        {
          // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", skip"); // Debug.
        } // if (currentSegmetn.isEmpty()) // Skip empty segment
        else
        {
          if (targetdocumentFile!=null) // Got sub documentfile from last iteration.
          {
            effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment+ "/" + currentSegmetn; // Remember effective virtual path.
            effectiveVirtualPathForCurrentSegment=effectiveVirtualPathForCurrentSegment.replace("//", "/"); // Remove consecutive /

            Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", current segment length: " + currentSegmetn.length()); // Debug.
            // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document: " + targetdocumentFile+ ", effective virtual path: " + effectiveVirtualPathForCurrentSegment); // Debug.
            // Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document: " + targetdocumentFile.getUri().toString()+ ", effective virtual path: " + effectiveVirtualPathForCurrentSegment); // Debug.
            // DocumentFile cachedtargetdocumentFile=pathDocumentFileMap.get(effectiveVirtualPathForCurrentSegment); // Get it from cache.
            DocumentFile cachedtargetdocumentFile=pathDocumentFileCacheManager.get(effectiveVirtualPathForCurrentSegment); // Get it from cache.
            
            if (cachedtargetdocumentFile!=null) // It exists
            {
              targetdocumentFile=cachedtargetdocumentFile; // Remember it.
              Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document: " + targetdocumentFile.getUri().toString()); // Debug.
            } // if (targetdocumentFile!=null) // It exists
            else // Not exist. Need to find
            {
            
              targetdocumentFile=targetdocumentFile.findFile(currentSegmetn);
              
              Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document object: " + targetdocumentFile); // Debug.
              if (targetdocumentFile!=null) // Target document exists
              {
                Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document: " + targetdocumentFile.getUri().toString()); // Debug.
                
                // pathDocumentFileMap.put(effectiveVirtualPathForCurrentSegment, targetdocumentFile); // Put it into the cache.
                pathDocumentFileCacheManager.put(effectiveVirtualPathForCurrentSegment, targetdocumentFile); // Put it into the cache.
              } // if (targetdocumentFile!=null) // Target document exists
            } // else // Not exist

          } // if (targetdocumentFile!=null) // Got sub documentfile from last iteration.
          else // Failed to get sub document file from last iteration.
          {
            Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", working directory: " + currentWorkingDirectory+ ",  trailing path: " + trailingPath + ", current segment: " + currentSegmetn + ", target document: " + targetdocumentFile+ ", last effective virtual path: " + effectiveVirtualPathForCurrentSegment); // Debug.
            
            break; // No need to iterate any more.
          } // else // Failed to get sub document file from last iteration.
        } // if (currentSegmetn.isEmpty())
      } // //       DocumentFile documentFile=DocumentFile.fromTreeUri(context, uri);
      
      result=targetdocumentFile;
    } // if (virtualPathMap.contains(wholeDirecotoryPath)) // It is in the virtual path map
    else // Not in the virtual path map
    {
      result=DocumentFile.fromFile(photoDirecotry);
    } // else // Not in the virtual path map
    
    return result;
  } // public DocumentFile getFile(File rootDirectory, String currentWorkingDirectory, String data51) 
  
  /**
  * Get docuemtn file from uri
  */
  private DocumentFile getDocumentFileFromUri(Context context, Uri uri)
  {
    DocumentFile result = null;
    Log.d(TAG, CodePosition.newInstance().toString()+  ", uri : " + uri); // Debug.

    if (uri!=null) // The uri exists
    {
      if (uri.getScheme().equals("file")) // it is a raw file
      {
        File photoDirecotry=new File(uri.getPath()); // Create a file object from the path.
        result=DocumentFile.fromFile(photoDirecotry);
      } // if (uri.getScheme().equals("file")) // it is a raw file
      else // Not a raw file
      {
        try
        {
          result=DocumentFile.fromTreeUri(context, uri); // 04-08 18:22:04.279 15010 15045 W System.err: java.lang.IllegalArgumentException: Invalid URI: file:///storage/emulated/0/DCIM/GoddessCamera
        }
        catch(IllegalArgumentException e)
        {
          result=DocumentFile.fromSingleUri(context, uri); // 04-08 18:22:04.279 15010 15045 W System.err: java.lang.IllegalArgumentException: Invalid URI: file:///storage/emulated/0/DCIM/GoddessCamera
        }
      } // else // Not a raw file
    } // if (uri!=null) // The uri exists

    return result;
  } // private DocumentFile getDocumentFileFromUri(Context context, Uri uri)
} // public class FilePathInterpreter implements VirtualPathLoadInterface

