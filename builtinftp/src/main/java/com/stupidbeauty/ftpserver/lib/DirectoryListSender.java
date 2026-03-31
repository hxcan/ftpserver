      if (paths.length == 0)  // 空目录
      {
        controlConnectHandler.checkFileManagerPermission(Constants.Permission.Read, null); // 检查权限

        // 👇 新增：如果启用了 Dolphin bug #474238 的绕过选项，则插入一个占位文件
        if (isEnableDolphinBug474238Placeholder())
        {
          String placeholderLine = "-rw-r--r-- 1 user group 0 Jan 01 00:00 .dolphin_placeholder\r\n";
          binaryStringSender.sendStringInBinaryMode(placeholderLine);
          Log.d(TAG, "DirectoryListSender [Empty Dir], sending placeholder line: [" + placeholderLine + "]"); // Debug
        }
      }