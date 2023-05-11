package com.stupidbeauty.ftpserver.lib;

// import com.stupidbeauty.hxlauncher.asynctask.LoadVoicePackageNameMapTask;
import java.util.Timer;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Environment;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.ConnectivityManager;
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
import android.provider.DocumentsContract;
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

public class ExternalStorageUriGuessor
{
  private static final String TAG="ExternalStorageUriGuessor"; // !< The tag used to output debug code.
  private HashMap<String, Uri> virtualPathMap=new HashMap<>(); //!< the map of virtual path to uri.
  private Context context=null; //!< Context.
  private boolean externalStoragePerformanceOptimize=false; //!< Whether to do external storage performance optimize.
  // private ExternalStorageUriGuessor externalStorageUriGuessor=new ExternalStorageUriGuessor(); //!< Guess the external storage uri.
  
  public void setContext(Context context)
  {
    this.context=context;
    
    // externalStorageUriGuessor.setContext(context);
  } // fileContentSender
  
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
  * Set option. Whether to do external storage perforamnce optimize.
  */
  public void setExternalStoragePerformanceOptimize(boolean isChecked)
  {
    externalStoragePerformanceOptimize=isChecked; // Remember the option.
  } // public void setExternalStoragePerformanceOptimize(boolean isChecked)
  
  /**
  * Guess the uri.
  */
  public Uri guessUri(Uri sourceUrit)
  {
    Uri result=sourceUrit; // Result;
    
    
    Log.d(TAG, CodePosition.newInstance().toString()+  ", result uri: " + result.toString()); // Debug.
    String sourceUriString=sourceUrit.toString();

    Uri cachedResult=virtualPathMap.get(sourceUriString); // Try to get it from cache.
    
    if (cachedResult!=null) // It exists in the cache.
    {
      result=cachedResult; // Use cached result.
    } // if (cachedResult!=null) // It exists in the cache.
    else // It does not exist in the cache.
    {
      if (sourceUriString.startsWith("content://com.android.externalstorage.documents/")) // Possible external storage
      {
        String filePath ="";
        
        DocumentFile documentFile=DocumentFile.fromTreeUri(context, sourceUrit);
        
        Uri uriWithDocumentId=documentFile.getUri(); // Get the uri that correspondse to a document.

        Log.d(TAG, CodePosition.newInstance().toString()+  ", uri used to retrieve document id : " + uriWithDocumentId.toString()); // Debug.

        // ExternalStorageProvider
        try
        {
          String docId = DocumentsContract.getDocumentId(uriWithDocumentId); // java.lang.IllegalArgumentException: Invalid URI: content://com.android.externalstorage.documents/tree/primary%3ADCIM%2FGoddessCamera
          Log.d(TAG, CodePosition.newInstance().toString()+  ", docId : " + docId); // Debug.
          String[] split = docId.split(":");
          String type = split[0];
          Log.d(TAG, CodePosition.newInstance().toString()+  ", type : " + type); // Debug.

          if ("primary".equalsIgnoreCase(type)) // Primary external storage.
          {
            String wholePath=Environment.getExternalStorageDirectory() +"/" + split[1];
            
            File whoelPathFile=new File(wholePath); // Create the file.
            
            
            File[] paths = whoelPathFile.listFiles();
            // DocumentFile[] paths = photoDirecotry.listFiles();
            // Log.d(TAG, CodePosition.newInstance().toString()+  ", paths size: " + paths.length); // Debug.

            if (paths!=null) // NOt null pointer
            {
              Log.d(TAG, "getDirectoryContentList, path: " + whoelPathFile + ", file amount: " + paths.length); // Debug.
              
              if (paths.length==0) // No conet listed
              {
                // controlConnectHandler.checkFileManagerPermission(Constants.Permission.Read, null); // Check file manager permission.
              } // if (paths.length==0) // No conet listed
              else // Listed Successfully
              {
                result=Uri.fromFile(whoelPathFile); // Construct a file uri.
                Log.d(TAG, CodePosition.newInstance().toString()+  ", wholeDirecotoryPath : " + wholePath + ", result uri: " + result.toString()); // Debug.
              } // else // Listed Successfully
            } // if (paths!=null) // NOt null pointer

            
            
            
            
            
          } // if ("primary".equalsIgnoreCase(type)) // Primary external storage.
        }
        catch(IllegalArgumentException e)
        {
          e.printStackTrace();
        }
      } // if (sourceUriString.startsWith("content://com.android.externalstorage.documents/")) // Possible external storage
      
      virtualPathMap.put(sourceUriString, result); // Add it into cache.
      
    } // else // It does not exist in the cache.

    return result;
  } // public Uri guessUri(Uri result)
  
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
} // public class FilePathInterpreter implements VirtualPathLoadInterface


