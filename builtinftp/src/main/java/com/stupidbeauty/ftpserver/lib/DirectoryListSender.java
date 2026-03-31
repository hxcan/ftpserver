          // 🔍 FIX: Check if nameOfFile ends with "/" which indicates a directory listing
          // This handles cases like "LIST /", "LIST /Download/", etc.
          // For specific files like "LIST file.txt", only match that exact filename
          if (fileName.equals(nameOfFile) || nameOfFile.isEmpty() || nameOfFile.endsWith("/"))  // 匹配或全部列出
          {
            Log.d(TAG, "📤 [DIR LOOP] Sending line: [" + currentLine + "]");
            
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