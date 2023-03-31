package com.stupidbeauty.ftpserver.lib;

import android.net.Uri;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;

public class VirtualPathMapItem
{
  private String uri; //!< The uri.
  private String virtualPath; //!< Virtual path.
  
  public String getVirtualPath()
  {
    return virtualPath;
  }
  
  public String getUri()
  {
    return uri;
  } 
  
  public void setVirtualPath(String virtualPath)
  {
    this.virtualPath=virtualPath;
  }
  
  public void setUri(String uri)
  {
    this.uri=uri;
  }
}
