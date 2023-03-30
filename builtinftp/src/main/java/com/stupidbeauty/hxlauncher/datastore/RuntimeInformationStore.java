package com.stupidbeauty.hxlauncher.datastore;

import android.content.pm.ShortcutInfo;

public class RuntimeInformationStore
{
    public ShortcutInfo getShortcut() {
        return shortcut;
    }

    public void setShortcut(ShortcutInfo shortcut) {
        this.shortcut = shortcut;
    }

    private ShortcutInfo shortcut; //!<快捷方式对象。
}





