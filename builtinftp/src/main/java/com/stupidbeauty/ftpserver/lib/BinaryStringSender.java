package com.stupidbeauty.ftpserver.lib;

import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.net.Uri;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.callback.CompletedCallback;
import android.util.Log;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;
import java.nio.charset.StandardCharsets;

public class BinaryStringSender
{
  private static final String TAG ="BinaryStringSender"; //!<  The tag used in debug strings.
  private AsyncSocket socket; //!< 当前的客户端连接。
    
  public void setSocket(AsyncSocket socketToSet)
  {
    socket=socketToSet;
  } // public void setSocket(AsyncSocket socketToSet)

    /**
    * 以二进制模式发送字符串内容。
    */
    public void sendStringInBinaryMode(String stringToSend)
    {
      byte[] contentToSend= (stringToSend+"\r\n").getBytes(StandardCharsets.UTF_8);
      
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", sending content size: " + contentToSend.length + ", original string length: " + stringToSend.length() + ", original string: " + stringToSend); // Debug.
      
      Util.writeAll(socket, contentToSend, new CompletedCallback() 
      {
        @Override
        public void onCompleted(Exception ex) 
        {
          if (ex != null) // Some exceptin happend
          {
            throw new RuntimeException(ex);
          } // if (ex != null) // Some exceptin happend
        }
      });
    } //private sendStringInBinaryMode(String stringToSend)
    
}
 
