  /**
  * 构造针对这个文件的一行输出。
  * @param path 真实的 DocumentFile 对象，用于获取文件大小、时间、权限等信息。
  * @param virtualFileName 虚拟路径名，用于在 FTP 响应中显示。
  */
  private String construct1LineListFile(DocumentFile path, String virtualFileName)
  {
    String fileName = virtualFileName;

    Date dateOfFile = new Date(path.lastModified());
    Date dateNow = new Date();
    boolean sameYear = false;

    if (dateOfFile.getYear() == dateNow.getYear())
    {
      sameYear = true;
    }

    Locale localEnUs = new Locale("en", "US");
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", localEnUs);
    String time = formatter.format(dateOfFile);

    SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", localEnUs);
    String year = yearFormatter.format(dateOfFile);

    SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM", localEnUs);
    SimpleDateFormat dayFormatter = new SimpleDateFormat("dd", localEnUs);
    String dateString = dayFormatter.format(dateOfFile);

    long fileSize = path.length();
    String group = "cx";
    String user = "ChenXin";

    Uri directoryUri = path.getUri();
    String directyoryUriPath = directoryUri.getPath();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      File fileObject = new File(directyoryUriPath);
      Path filePathObject = fileObject.toPath();

      if (directoryUri.getScheme().equals("file"))
      {
        try
        {
          UserPrincipal userPrincipal = Files.getOwner(filePathObject);
          user = userPrincipal.getName();
        }
        catch (IOException e)
        {
          Log.d(TAG, "construct1LineListFile, failed to get owner name:");
          e.printStackTrace();
        }
      }
    }

    String linkNumber = "1";
    String permission = getPermissionForFile(path);
    String month = monthFormatter.format(dateOfFile);
    String timeOrYear = sameYear ? time : year;

    String currentLine = "";

    if (extraInformationEnabled)
    {
      currentLine = permission + " " + linkNumber + " " + user + " " + group + " " + fileSize + " " + month + " " + dateString + " " + timeOrYear + " ";
    }

    currentLine = currentLine + fileName;

    // 🔍 DEBUG: Log the exact line content with visible line ending markers
    Log.d(TAG, "📤 [CONSTRUCT] Line constructed: [" + currentLine + "] (length=" + currentLine.length() + ")");

    return currentLine;
  }

  /**
  * File name tolerant. For example: /Android/data/com.client.xrxs.com.xrxsapp/files/XrxsSignRecordLog/Zw40VlOyfctCQCiKL_63sg==, with a trailing <LF> (%0A).
  */
  public void setFileNameTolerant(boolean toleranttrue)
  {
    fileNameTolerant=toleranttrue; // Remember.
  } // public void setFileNameTolerant(boolean toleranttrue)
  
  /**
  * 获取目录的完整列表。
  */
  private String getDirectoryContentList(DocumentFile photoDirecotry, String nameOfFile)
  {
    nameOfFile = nameOfFile.trim(); // 去除空白字符。陈欣

    String result = ""; // 结果。

    if (photoDirecotry.isFile())  // 是一个文件。
    {
      String currentLine = construct1LineListFile(photoDirecotry, photoDirecotry.getName()); // 构造针对这个文件的一行输出。
      Log.d(TAG, "DirectoryListSender [Single File], sending line: [" + currentLine + "]"); // Debug
      
      // 🔍 DEBUG: Log exact bytes being sent
      byte[] lineBytes = (currentLine + "\r\n").getBytes();
      StringBuilder hexDump = new StringBuilder();
      for (byte b : lineBytes) {
        hexDump.append(String.format("%02X ", b));
      }
      Log.d(TAG, "📤 [SINGLE FILE] Sending bytes (" + lineBytes.length + "): " + hexDump.toString());
      Log.d(TAG, "📤 [SINGLE FILE] Sending text: [" + currentLine + "\\r\\n]");
      
      binaryStringSender.sendStringInBinaryMode(currentLine); // 发送回复内容。
    }
    else  // 是目录
    {
      DocumentFile[] paths = photoDirecotry.listFiles();

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
      else  // 列出成功
      {
        PathDocumentFileCacheManager pathDocumentFileCacheManager = filePathInterpreter.getPathDocumentFileCacheManager(); // 获取缓存管理器

        for (DocumentFile path : paths)  // 遍历每个文件
        {
          String fileName = path.getName(); // 获取文件名

          Log.d(TAG, CodePosition.newInstance().toString() + ", wholeDirecotoryPath : " + wholeDirecotoryPath + ", target document: " + path.getUri().toString() + ", file name length: " + fileName.length() + ", file name content: " + fileName + ", root directory: " + rootDirectory + ", working directory: " + workingDirectory); // Debug.

          String wholeFilePath = filePathInterpreter.resolveWholeDirectoryPath(rootDirectory, workingDirectory, fileName); // 解析完整路径
          wholeFilePath = wholeFilePath.replace("//", "/"); // 替换双斜杠

          boolean isAVirtualPath = filePathInterpreter.isExactVirtualPath(wholeFilePath); // 是否是虚拟路径

          String currentLine;
          if (isAVirtualPath)
          {
            // 如果是虚拟路径，使用虚拟路径名，但真实信息从 DocumentFile 获取
            currentLine = construct1LineListFile(path, fileName); // 👈 传入虚拟路径名
          }
          else
          {
            // 否则，正常调用
            currentLine = construct1LineListFile(path, path.getName()); // 传入真实文件名
          }


          // if (isAVirtualPath)  // 是虚拟路径
          // {
          //   path = filePathInterpreter.getFile(rootDirectory, workingDirectory, fileName); // 替换为实际路径
          // }
          //
          // String currentLine = construct1LineListFile(path); // 构造一行输出

          String effectiveVirtualPathForCurrentSegment = wholeDirecotoryPath + "/" + fileName; // 构建虚拟路径
          effectiveVirtualPathForCurrentSegment = effectiveVirtualPathForCurrentSegment.replace("//", "/"); // 去掉多余斜杠

          pathDocumentFileCacheManager.put(effectiveVirtualPathForCurrentSegment, path); // 存入缓存

          if (fileNameTolerant)  // 容错文件名特殊字符
          {
            String tolerantEffectiveVirtualPath = effectiveVirtualPathForCurrentSegment.trim();

            if (!tolerantEffectiveVirtualPath.equals(effectiveVirtualPathForCurrentSegment))
            {
              DocumentFile documentFileForTolerantPath = pathDocumentFileCacheManager.get(tolerantEffectiveVirtualPath);

              if (documentFileForTolerantPath == null)
              {
                pathDocumentFileCacheManager.put(tolerantEffectiveVirtualPath, path); // 添加容错映射
              }
            }
          }

          if (fileName.equals(nameOfFile) || nameOfFile.isEmpty())  // 匹配或全部列出
          {
            Log.d(TAG, "DirectoryListSender [Dir Loop], sending line: [" + currentLine + "]"); // Debug
            
            // 🔍 DEBUG: Log exact bytes being sent for each line
            byte[] lineBytes = (currentLine + "\r\n").getBytes();
            StringBuilder hexDump = new StringBuilder();
            for (byte b : lineBytes) {
              hexDump.append(String.format("%02X ", b));
            }
            Log.d(TAG, "📤 [DIR LOOP] Sending bytes (" + lineBytes.length + "): " + hexDump.toString());
            Log.d(TAG, "📤 [DIR LOOP] Sending text: [" + currentLine + "\\r\\n]");
            
            binaryStringSender.sendStringInBinaryMode(currentLine); // 发送当前行
          }
        }
      }
    }

    // 🔍 DEBUG: Log the final \r\n terminator
    Log.d(TAG, "📤 [FINAL] Writing final \\r\\n terminator (2 bytes: 0D 0A)");
    
    Util.writeAll(data_socket, "\r\n".getBytes(), new CompletedCallback()
    {
      @Override
      public void onCompleted(Exception ex)
      {
        if (ex != null) throw new RuntimeException(ex);

        Log.d(TAG, CodePosition.newInstance().toString() + ", [Server] data Successfully wrote message: " + fileToSend + ", going to close data_socket: " + data_socket); // Debug.

        notifyLsCompleted(); // 通知已发送完成
        fileToSend = null; // 清空文件对象
        data_socket.close(); // 关闭连接
      }
    });

    return result;
  }