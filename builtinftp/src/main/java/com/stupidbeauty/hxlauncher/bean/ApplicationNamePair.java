package com.stupidbeauty.hxlauncher.bean;

public class ApplicationNamePair
{
    private String packageName; //!<包名。

    public String getPackageName() {
        return packageName;
    }

    public String getReadableApplicationName() {
        return readableApplicationName;
    }

    private String readableApplicationName; //!<可读应用程序名字。
}
