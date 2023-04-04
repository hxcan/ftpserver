package com.stupidbeauty.hxlauncher.bean;

import java.util.List;

public class BuiltinShortcut
{
    public String getPackageName() {
        return packageName;
    }

    public List<String> getActivities() {
        return activities;
    }

    private String packageName; //!<包名。
    private List<String> activities; //!<活动列表。
}



