      subDirectoryName=parameter; // 记录可能的子目录名字。

      wholeDirecotoryPath = filePathInterpreter.resolveWholeDirectoryPath( rootDirectory, currentWorkingDirectory, parameter); // resolve whole directory path.
      DocumentFile photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, parameter); // resolve 目录。
      
      // 🔍 FIX: If the resolved target is a directory, we want to list ALL its contents
      // So clear subDirectoryName to make the condition in getDirectoryContentList() match all files
      if (photoDirecotry != null && photoDirecotry.isDirectory()) {
          Log.d(TAG, "📝 [FIX] Target is a directory, clearing subDirectoryName from [" + subDirectoryName + "] to []");
          subDirectoryName = ""; // Clear to list all files in the directory
      }
      
      // Log.d(TAG, CodePosition.newInstance().toString()+  ", directory : " + photoDirecotry + ", working directory: " + currentWorkingDirectory + ", directory uri: " + photoDirecotry.getUri().toString() + ", whole directory path: " + wholeDirecotoryPath); // Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+  ", going to set file to send : " + photoDirecotry); // Debug.

      fileToSend=photoDirecotry; // 记录，要发送的文件对象。