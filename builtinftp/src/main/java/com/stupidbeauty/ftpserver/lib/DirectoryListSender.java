          if (fileName.equals(nameOfFile) || nameOfFile.isEmpty() || nameOfFile.endsWith("/"))  // 匹配或全部列出
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