package com.stupidbeauty.hxlauncher.bean;

import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ApplicationLockInformation
{
  private HashSet<String> applicationLockSet=null; //!< 应用锁集合。
  private List<String> applicationLockList=null; //!< 应用锁列表。

  /**
  * 设置锁数据集合。
  */
  public void setApplicationLockSet(HashSet<String> applicationLockSet) 
  {
    this.applicationLockSet=applicationLockSet;
    
    applicationLockList = new ArrayList<String>(); // 创建列表。
    
    for(String packageName: applicationLockSet) // 一个个地加入到列表中。
    {
      applicationLockList.add(packageName); // 加入列表中。
    } // for(String packageName: applicationLockSet) // 一个个地加入到列表中。
  } // public void setApplicationLockSet(HashSet<String> applicationLockSet)
}
