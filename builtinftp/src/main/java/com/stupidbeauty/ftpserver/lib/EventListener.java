package com.stupidbeauty.ftpserver.lib;

public interface EventListener
{
    public static final String DELETE = "com.stupidbeauty.ftpserver.lib.delete"; //!< 文件被删除。
    public void onEvent(String eventCode) ; //!< Event occured.
}

