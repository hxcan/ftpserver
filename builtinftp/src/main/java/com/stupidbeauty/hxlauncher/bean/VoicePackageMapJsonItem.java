package com.stupidbeauty.hxlauncher.bean;

import android.net.Uri;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;

public class VoicePackageMapJsonItem
{
    public String voiceCommand; //!<语音指令。
    private Uri uri; //!< The uri.
    private String packageName; //!<软件包名
    private String virtualPath; //!< Virtual path.
    
    public void setVirtualPath(String virtualPath)
    {
      this.virtualPath=virtualPath;
    }
    
    public void setUri(Uri uri)
    {
      this.uri=uri;
    }

    public String getPackageName() {
        return packageName;
    }
}
