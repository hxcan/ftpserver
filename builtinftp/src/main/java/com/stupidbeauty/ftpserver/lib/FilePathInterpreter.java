package com.stupidbeauty.ftpserver.lib;

import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import com.koushikdutta.async.Util;
import android.util.Log;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.io.File;

public class FilePathInterpreter
{
  private static final String TAG="FilePathInterpreter"; // !< The tag used to output debug code.

  public File getFile(File rootDirectory, String currentWorkingDirectory, String data51) //照片目录。
  {
    String currentWorkingDirectoryUpdate=currentWorkingDirectory; // 更新后的当前工作目录。
    
    if (data51.startsWith("/")) // 绝对路径。
    {
      currentWorkingDirectoryUpdate="/"; // 当前工作目录更新为根目录。
    }
  
    String wholeDirecotoryPath = rootDirectory.getPath() + currentWorkingDirectoryUpdate + "/" + data51; // 构造完整路径。

    wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠

    Log.d(TAG, "getFile: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.

    File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

    String replyString=""; // 回复字符串。

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

    return photoDirecotry;
  }
}

