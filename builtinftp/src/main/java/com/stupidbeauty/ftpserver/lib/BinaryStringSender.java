package com.stupidbeauty.ftpserver.lib;

import com.koushikdutta.async.*;
import com.koushikdutta.async.callback.CompletedCallback;

public class BinaryStringSender
{
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
      Util.writeAll(socket, (stringToSend+"\r\n").getBytes(), new CompletedCallback() 
      {
        @Override
        public void onCompleted(Exception ex) 
        {
          if (ex != null) throw new RuntimeException(ex);
          System.out.println("[Server] Successfully wrote message");
        }
      });
    } //private sendStringInBinaryMode(String stringToSend)
    

}
 
