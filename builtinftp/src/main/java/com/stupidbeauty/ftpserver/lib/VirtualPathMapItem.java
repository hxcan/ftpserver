package com.stupidbeauty.ftpserver.lib;

import android.net.Uri;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;

public class VirtualPathMapItem
{
  private Uri uri; //!< The uri.
  private String virtualPath; //!< Virtual path.
  
  public String getVirtualPath()
  {
    return virtualPath;
  }
  
  public Uri getUri()
  {
    return uri;
  } 
  
  public void setVirtualPath(String virtualPath)
  {
    this.virtualPath=virtualPath;
  }
  
  public void setUri(Uri uri)
  {
    this.uri=uri;
  }
}
