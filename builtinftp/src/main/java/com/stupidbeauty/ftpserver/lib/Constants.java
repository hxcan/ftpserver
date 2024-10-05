package com.stupidbeauty.ftpserver.lib;

import android.os.Environment;
import java.io.File;

public class Constants
{
  public static class ErrorCode
  {
    public static final Integer ADDRESS_ALREADY_IN_USE = 182735; //!< Address already in use. Chen xin.
    public static final Integer ControlConnectionEndedUnexpectedly = 95731; //!< Control connection ended unexpectedly.
  } // public static class ErrorCode

  public static class FilePath
  {
    public static final String AndroidData = Environment.getExternalStorageDirectory().getPath() + "/Android/data/"; //!< /Android/data directory.
    public static final String ExternalRoot = Environment.getExternalStorageDirectory().getPath() ; //!< External root directory.
  }
  
  public static class RequestCode
  {
    public static final Integer AndroidDataPermissionRequestCode= 100345; //!< Request code of /Android/data permission.
  }
  
  public static class Permission
  {
    public static final Integer Write = 194238; //!< Permison of write.
    public static final Integer Read = 202152; //!< Permission of read.
  } // public static class Permission
}

