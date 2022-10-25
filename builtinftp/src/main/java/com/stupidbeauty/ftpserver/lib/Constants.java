package com.stupidbeauty.ftpserver.lib;

import android.os.Environment;
import java.io.File;

public class Constants
{
  public static class ErrorCode
  {
    public static final Integer ADDRESS_ALREADY_IN_USE = 182735; //!< Address already in use. Chen xin.
  }

  public static class FilePath
  {
    public static final String AndroidData = Environment.getExternalStorageDirectory().getPath() + "/Android/data"; //!< Address already in use. Chen xin.
  }
  
  public static class RequestCode
  {
    public static final Integer AndroidDataPermissionRequestCode= 100345; //!< Request code of /Android/data permission.
  }
}

