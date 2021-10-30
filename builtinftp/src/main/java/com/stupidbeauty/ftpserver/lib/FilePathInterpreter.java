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
    String wholeDirecotoryPath = rootDirectory.getPath() + currentWorkingDirectory + data51; // 构造完整路径。

    wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠

    Log.d(TAG, "processSizeCommand: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.

    File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

    String replyString=""; // 回复字符串。

    if (photoDirecotry.exists()) // 文件存在
    {
//             long fileSize= photoDirecotry.length(); //文件尺寸。 陈欣
//
//             replyString="213 " + fileSize + " \n"; // 文件尺寸。
    } //if (photoDirecotry.exists()) // 文件存在
    else // 文件不 存在
    {
        wholeDirecotoryPath = rootDirectory.getPath() + data51; // 构造完整路径。

        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠

        photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

//             replyString="550 No directory traversal allowed in SIZE param\n"; // File does not exist.
    } //else // 文件不 存在

    return photoDirecotry;
  }
}

