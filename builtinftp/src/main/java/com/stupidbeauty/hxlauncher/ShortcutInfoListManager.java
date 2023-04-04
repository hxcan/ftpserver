package com.stupidbeauty.hxlauncher;

import android.content.pm.ShortcutInfo;

import java.util.ArrayList;

public class ShortcutInfoListManager
{
    private ArrayList<ShortcutInfo> shortcutInfoList=new ArrayList<>(); //!<快捷方式列表。

    /**
     * 获取对应位置的快捷方式。
     * @param shortcutInfoIndex 快捷方式下标。
     * @return 对应位置的快捷方式。
     */
    public ShortcutInfo getShortcut(int shortcutInfoIndex)
    {
        ShortcutInfo shortcutInfo=shortcutInfoList.get(shortcutInfoIndex);

        return shortcutInfo;
    } //public ShortcutInfo getShortcut(int shortcutInfoIndex)

    /**
     * 添加到列表中。
     * @param shortcutInfo 要添加的快捷方式。
     */
    public void addShortcut(ShortcutInfo shortcutInfo)
    {
        shortcutInfoList.add(shortcutInfo); //添加。
    } //public void addShortcut(ShortcutInfo shortcutInfo)

    /**
     * 获取快捷方式个数。
     * @return 快捷方式个数。
     */
    public int getShortcutAmount()
    {
        int result=shortcutInfoList.size();

        return result;
    } //public int getShortcutAmount()
}



