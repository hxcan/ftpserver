package com.stupidbeauty.hxlauncher.datastore;

//         NonsenseIconType=0,
//         ShortcutIconType=1,
//         ActivityIconType=2,

public enum  LauncherIconType
{
    NonsenseIconType(0),
    ShortcutIconType(1),
    ActivityIconType(2);
    
    private int value;
    
    LauncherIconType(int value)
    {
    this.value=value;
    }
    
    public int getValue()
    {
        return value;
    };
}

/*
public enum EXIT_CODE {
    A(104), B(203);

    private int numVal;

    EXIT_CODE(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}*/
