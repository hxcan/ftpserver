package com.stupidbeauty.ftpserver.lib;

import java.net.BindException;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.AsyncSocket;
// import com.stupidbeauty.async.AsyncServer;
import com.koushikdutta.async.AsyncServer;
// import com.stupidbeauty.async.*;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import org.apache.commons.io.FileUtils;
import com.koushikdutta.async.callback.ConnectCallback;
import java.net.InetSocketAddress;
import android.text.format.Formatter;
import android.net.wifi.WifiManager;
import java.util.Random;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.stupidbeauty.builtinftp.Utils.shellExec;

public class FtpServer {
    private ErrorListener errorListener=null; //!< Error listener. Chen xin. 
    private Context context; //!< 执行时使用的上下文。
    private static final String TAG="Server"; //!< 输出调试信息时使用的标记
    private InetAddress host;
    private int port;
    private boolean allowActiveMode=true; //!< 是否允许主动模式。
        
    public void setErrorListener(ErrorListener errorListener)    
    {
        this.errorListener = errorListener;
    } //public void setErrorListener(ErrorListener errorListener)    

    public FtpServer(String host, int port, Context context, boolean allowActiveMode) {
        this.context=context;
        this.allowActiveMode=allowActiveMode;
        
        try {
            this.host = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        this.port = port;

        setup();
    }

    private void setup()
    {
        AsyncServer.getDefault().listen(host, port, new ListenCallback() {

        @Override
        public void onAccepted(final AsyncSocket socket)
        {
        ControlConnectHandler handler=new ControlConnectHandler(context, allowActiveMode, host); // 创建处理器。
            handler.handleAccept(socket);
        }

        @Override
        public void onListening(AsyncServerSocket socket)
        {
            System.out.println("[Server] Server started listening for connections");
        }

            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) 
                {
//                 Caused by: java.net.BindException: Address already in use

                        
                    if ( ex instanceof BindException )
                    {  
                        if (errorListener!=null)
                        {
                            errorListener.onError(Constants.ErrorCode.ADDRESS_ALREADY_IN_USE); // Report error. Chen xin.
                        }
                        else // No error listener
                        {
                            throw new RuntimeException(ex);
                        } //else // No error listener
                    }
                    else // Other exceptions
                    {
                        throw new RuntimeException(ex);
                    }
                }
                System.out.println("[Server] Successfully shutdown server");
            }
        });
    }
    
    
    
    
    
    
    
//         def processSizeCommand(data51)
//         if File.exists?(data51)
//             send_data("213 #{File.size(data51)} \n")
//         else
//             send_data("550 \n") # file not found
//         end
//     end



    
    
    



}
