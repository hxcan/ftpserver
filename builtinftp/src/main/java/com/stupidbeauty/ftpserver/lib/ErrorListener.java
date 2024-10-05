package com.stupidbeauty.ftpserver.lib;

import java.net.BindException;

public interface ErrorListener
{
    public void onError(Integer errorCode) ; //!< Error occured.
}

