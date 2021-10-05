package com.stupidbeauty.builtinftp;

import android.content.Context;
import android.os.AsyncTask;
import com.stupidbeauty.ftpserver.lib.FtpServer;
import java.net.BindException;

public class BuiltinFtpServer
{
    private ErrorListener errorListener=null; //!< Error listener.
    private FtpServerErrorListener ftpServerErrorListener=null; //!< The ftp server error listner. Chen xin.
    private int port=1421; //!< Port.
    private FtpServer ftpServer=null; //!< Ftp server object.
    private boolean allowActiveMode=true; //!<  Whether to allow active mode.
    
    public void setErrorListener(ErrorListener errorListener)    
    {
        this.errorListener = errorListener;
    } //public void setErrorListener(ErrorListener errorListener)    
    
    public void onError(Integer errorCode) 
    {
        if (errorListener!=null)
        {
            errorListener.onError(errorCode); // Report error.
        }
        else // Not listener
        {
//             throw new BindException();
            Exception ex = new BindException();
            throw new RuntimeException(ex);
        }
    } //public void onError(Integer errorCode)
    
    /**
    * Set to allow or not allow active mode.
    */
    public void setAllowActiveMode(boolean allowActiveMode)
    {
        this.allowActiveMode=allowActiveMode;
    } //private void setAllowActiveMode(allowActiveMode)
    
    public void setPort(int port)
    {
        this.port=port;
    } //public void setPort(int port)
        
    private BuiltinFtpServer() {
    }

    public BuiltinFtpServer(Context context) 
    {
        this.context = context;
    }

    private Context context; //!< Context.

    public void start()
    {
        ftpServerErrorListener=new FtpServerErrorListener(this);
    
        ftpServer = new FtpServer("0.0.0.0", port, context, allowActiveMode);
        ftpServer.setErrorListener(ftpServerErrorListener); // Set error listner. Chen xin.

//     new AsyncTask<Void, Void, Void>() {
//         @Override
//                 protected Void doInBackground(Void... params) {
//                     new FtpServer("0.0.0.0", port, context, allowActiveMode);
// 
//                     return null;
//             }
//         }.execute();
    }
}
