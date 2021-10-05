package com.stupidbeauty.ftpserver.lib;

import com.koushikdutta.async.*;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.*;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import org.apache.commons.io.FileUtils;
// import com.stupidbeauty.commons.io.FileUtils;
import com.koushikdutta.async.callback.ConnectCallback;
import java.net.InetSocketAddress;
import android.text.format.Formatter;
import android.net.wifi.WifiManager;
import java.util.Random;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.stupidbeauty.builtinftp.Utils.shellExec;

class ControlConnectHandler
{
    private AsyncSocket socket; //!< 当前的客户端连接。
    private static final String TAG ="ControlConnectHandler"; //!<  输出调试信息时使用的标记。
     private Context context; //!< 执行时使用的上下文。
       private AsyncSocket data_socket; //!< 当前的数据连接。
    private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
    private String currentWorkingDirectory="/"; //!< 当前工作目录
    private int data_port=1544; //!< 数据连接端口。
        private boolean allowActiveMode=true; //!< 是否允许主动模式。
    private File writingFile; //!< 当前正在写入的文件。
        private boolean isUploading=false; //!< 是否正在上传。陈欣
    private InetAddress host;

        /**
    * 从数据套接字处接收数据。陈欣
    */
    private void receiveDataSocket( ByteBufferList bb)
    {
        byte[] content=bb.getAllByteArray(); // 读取全部内容。
        
        boolean appendTrue=true;

        try
        {
        FileUtils.writeByteArrayToFile(writingFile, content, appendTrue); // 写入。
        }
        catch (Exception e)
        {
        e.printStackTrace();
        }
    } //private void                         receiveDataSocket( ByteBufferList bb)

    public ControlConnectHandler(Context context, boolean allowActiveMode, InetAddress host)
    {
            this.context=context;
            this.allowActiveMode=allowActiveMode;
            this.host=host;

            setupDataServer(); // 启动数据传输服务器。
    }
    
        /**
    * 打开指向客户端特定端口的连接。
    */
    private void openDataConnectionToClient(String content)
    {
    String portString=content.split(" ")[1].trim(); // 端口字符串。
    
    String[] addressStringList= portString.split(","); //获取地址字符串。
    
    String ip=addressStringList[0]+"."+addressStringList[1]+"."+addressStringList[2]+"."+addressStringList[3]; // 构造IP。陈欣
    int port=Integer.parseInt(addressStringList[4])*256+Integer.parseInt(addressStringList[5]); // 计算出端口号。
    
    //连接：陈欣
    
            AsyncServer.getDefault().connectSocket(new InetSocketAddress(ip, port), new ConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, final AsyncSocket socket) {
                handleConnectCompleted(ex, socket);
            }
        });
    } //private void openDataConnectionToClient(String content)


        /**
    * 以二进制模式发送字符串内容。
    */
    private void sendStringInBinaryMode(String stringToSend)
    {
            Util.writeAll(socket, stringToSend.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully wrote message");
            }
        });

    } //private sendStringInBinaryMode(String stringToSend)

        /**
    * 告知已经发送文件内容数据。
    */
    private void notifyFileSendCompleted() 
    {
        String replyString="216 " + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
        
        sendStringInBinaryMode(replyString); // 发送。



    } //private void notifyFileSendCompleted()

        /**
    * 发送文件内容。
    */
    private void sendFileContent(String data51, String currentWorkingDirectory) 
    {
        String wholeDirecotoryPath= context.getFilesDir().getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
        File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
            
        String replyString=""; // 回复字符串。

            byte[] photoBytes=null; //数据内容。

            try //尝试构造请求对象，并且捕获可能的异常。
            {
				photoBytes= FileUtils.readFileToByteArray(photoDirecotry); //将照片文件内容全部读取。
            } //try //尝试构造请求对象，并且捕获可能的异常。
		catch (Exception e)
		{
			e.printStackTrace();
		}

        if (data_socket!=null) // 数据连接存在
        {
            Util.writeAll(data_socket, photoBytes, new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] data Successfully wrote message");
                
                notifyFileSendCompleted(); // 告知已经发送文件内容数据。
            }
        });

                } //if (data_socket!=null)
        else // 数据连接不存在
        {
            notifyLsFailedDataConnectionNull(); // 告知，数据连接未建立。
        } //else // 数据连接不存在

    } //private void sendFileContent(String data51, String currentWorkingDirectory)

    /**
    * 告知上传完成。
    */
    private void notifyStorCompleted() 
    {
//         def notifyStorCompleted
//         send_data("226 \n")
//     end

        String replyString="226 Stor completed." + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        sendStringInBinaryMode(replyString);
    } //private void notifyStorCompleted()
    
    /**
    * 告知，数据连接未建立。
    */
    private void notifyLsFailedDataConnectionNull() 
    {
    //        send_data "216 \n"

        String replyString="426 " + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                Log.d(TAG, "notifyLsFailedDataConnectionNull, [Server] Successfully wrote message");
            }
        });

    } //private void notifyLsFailedDataConnectionNull()

    /**
     * 告知已经发送目录数据。
     */
    private void notifyLsCompleted()
    {
//        send_data "216 \n"

        String replyString="226 Data transmission OK. ChenXin" + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully wrote message");
            }
        });

    } //private void notifyLsCompleted()

        /**
     * 发送目录列表数据。
     * @param content The path of the directory.
     * @param currentWorkingDirectory 当前工作目录。
     */
    private void sendListContent(String content, String currentWorkingDirectory)
    {
//        puts "currentWorkingDirectory: #{currentWorkingDirectory}, lenght: #{currentWorkingDirectory.length}"
//        currentWorkingDirectory.strip!
//            puts "currentWorkingDirectory: #{currentWorkingDirectory}, lenght: #{currentWorkingDirectory.length}"
//        extraParameter=data.split(" ")[1]
//        puts "extraParameter: #{extraParameter}"
//        command="ls #{extraParameter} #{currentWorkingDirectory}"
//        puts "command: #{command}"
//        #command: ls -la /
//
//            output=`#{command}`

        String parameter=content.substring(5).trim(); // 获取额外参数。
        
        if (parameter.equals("-la")) // 忽略
        {
            parameter=""; // 忽略成空白。
        } //if (parameter.equals("-la")) // 忽略
        
        

        currentWorkingDirectory=currentWorkingDirectory.trim();

        String wholeDirecotoryPath= context.getFilesDir().getPath() + currentWorkingDirectory; // 构造完整路径。

        String output = getDirectoryContentList(wholeDirecotoryPath, parameter); // Get the whole directory list.
        
        Log.d(TAG, "output: " + output); // Debug
        
        if (data_socket!=null) // 数据连接存在
        {
        Util.writeAll(data_socket, (output + "\n").getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] data Successfully wrote message");
                
                notifyLsCompleted(); // 告知已经发送目录数据。
            }
        });
        } //if (data_socket!=null)
        else // 数据连接不存在
        {
//             notifyLsFailedDataConnectionNull(); // 告知，数据连接未建立。
            queueForDataSocket(output); // 将回复数据排队。
        } //else // 数据连接不存在

    } //private void sendListContent(String content, String currentWorkingDirectory)

        /**
    * 将回复数据排队。
    */
    private void queueForDataSocket(String output) 
    {
        dataSocketPendingByteArray=output.getBytes(); // 排队。
    } //private void queueForDataSocket(String output)

    /**
    *  获取目录的完整列表。
    */
    private String getDirectoryContentList(String wholeDirecotoryPath, String nameOfFile)
    {
    nameOfFile=nameOfFile.trim();
    
    String result="";
            File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
            
           File[]   paths = photoDirecotry.listFiles();
         
         // for each pathname in pathname array
         for(File path:paths) {
         
            // prints file and directory paths
            System.out.println(path);
            
            // -rw-r--r-- 1 nobody nobody     35179727 Oct 16 07:31 VID_20201015_181816.mp4
// -rw-r--r-- 1 nobody nobody       243826 Jan 15 11:52 forum.php.jpg
// -rw-r--r-- 1 nobody nobody       240927 Jan 16 11:15 forum.php.1.jpg
// -rw-r--r-- 1 nobody nobody       205318 Jan 16 11:16 forum.php.2.jpg

            String fileName=path.getName(); // 获取文件名。

                            Date date=new Date(path.lastModified());  
                            
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
//   String time= date.format(formatter);
                            String time="8:00";

                              DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM");

                            String dateString="30";
                            
                            long fileSize=path.length(); // 文件尺寸。
                            
                            String group="cx";
                            
                            String user = "ChenXin";
                            
                            String linkNumber="1";
                            
                            String permission="-rw-r--r--";

                            String month="Jan"; // 月份 。
            String currentLine = permission + " " + linkNumber + " " + user + " " + group + " " + fileSize + " " + month + " " + dateString + " " + time + " " + fileName + "\n" ; // 构造当前行。
            
            if (fileName.equals(nameOfFile)  || (nameOfFile.isEmpty())) // 名字匹配。
            {
            result=result+currentLine; // 构造结果。
            } //if (fileName.equals(nameOfFile)) // 名字匹配。
         }

         return result;
    } //private String getDirectoryContentList(String wholeDirecotoryPath)

        /**
    * 处理尺寸查询命令。
    */
    private void processSizeCommand(String data51)
    {
        Log.d(TAG, "processSizeCommand: filesdir: " + context.getFilesDir().getPath()); // Debug.
        Log.d(TAG, "processSizeCommand: workding directory: " + currentWorkingDirectory); // Debug.
        Log.d(TAG, "processSizeCommand: data51: " + data51); // Debug.

    
        String wholeDirecotoryPath= context.getFilesDir().getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
                    Log.d(TAG, "processSizeCommand: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
                    
            File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
            
            String replyString=""; // 回复字符串。

            if (photoDirecotry.exists()) // 文件存在
            {
            long fileSize= photoDirecotry.length(); //文件尺寸。 陈欣
            
                replyString="213 " + fileSize + " \n"; // 文件尺寸。
            } //if (photoDirecotry.exists()) // 文件存在
            else // 文件不 存在
            {
                replyString="550 No directory traversal allowed in SIZE param\n"; // File does not exist.
            } //else // 文件不 存在

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) 
                {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                } //public void onCompleted(Exception ex) 
            });
    } //private void processSizeCommand(String data51)

        /**
     * 处理命令。
     * @param command 命令关键字
     * @param content 整个消息内容。
     */
    private void processCommand(String command, String content)
    {
        Log.d(TAG, "command: " + command + ", content: " + content); //Debug.

        if (command.equals("USER")) // 用户登录
        {
            Util.writeAll(socket, "331 Send password\n".getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });
        } //if (command.equals("USER")) // 用户登录
        else if (command.equals("PASS")) // 密码
        {
            Util.writeAll(socket, "230 Loged in.\n".getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });
        } //else if (command.equals("PASS")) // 密码
        else if (command.equals("SYST")) // 系统信息
        {
            //        send_data "200 UNIX Type: L8\n"

            Util.writeAll(socket, "215 UNIX Type: L8\\n".getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });

        } //else if (command.equals("SYST")) // 系统信息
        else if (command.equals("PWD")) // 查询当前工作目录
        {
            //        send_data "200 #{@currentWorkingDirectory}\n"
//        puts "200 #{@currentWorkingDirectory}\n"

            String replyString="257 \"" + currentWorkingDirectory + "\"\n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });

        } //else if (command.equals("PWD")) // 查询当前工作目录
        else if (command.equals("cwd")) // 切换工作目录
        {
            //        elsif command=='cwd'
//        newWorkingDirectory=data[4..-1]

            String targetWorkingDirectory=content.substring(4).trim();
            
                        String wholeDirecotoryPath= context.getFilesDir().getPath() + targetWorkingDirectory; // 构造完整路径。
                    
                    wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
                    Log.d(TAG, "processSizeCommand: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
                    
            File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

                        String replyString="" ; // 回复内容。

            if (photoDirecotry.isDirectory()) // 是个目录
            {
            currentWorkingDirectory=targetWorkingDirectory;

            replyString="250 cwd succeed" + "\n"; // 回复内容。
            
            } //if (photoDirecotry.isDirectory()) // 是个目录
                else //不是个目录
                {
//                 陈欣
            replyString="550 " + "\n"; // 回复内容。
                
                }


            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                } //public void onCompleted(Exception ex) {
            }); //Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {


        } //else if (command.equals("cwd")) // 切换工作目录
        else if (command.equals("TYPE")) // 传输类型
        {
//        elsif command =='TYPE'
//        send_data "200 \n"

            String replyString="200 binery type set" + "\n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });

        } //else if (command.equals("TYPE")) // 传输类型
        else if (command.equals("PASV")) // 被动传输
        {
            //        elsif command=='PASV'
//            #227 Entering Passive Mode (a1,a2,a3,a4,p1,p2)
//            #where a1.a2.a3.a4 is the IP address and p1*256+p2 is the port number.
        
        setupDataServer(); // 初始化数据服务器。
        
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        
        String ip = ipAddress.replace(".", ",");
        
        int port256=data_port/256;
        int portModule=data_port-port256*256;

            String replyString="227 Entering Passive Mode ("+ip+","+port256+","+portModule+") \n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            sendStringInBinaryMode(replyString);
        } //else if (command.equals("PASV")) // 被动传输
        else if (command.equals("EPSV")) // 扩展被动模式
        {
            //        elsif command=='EPSV'
//        send_data "202 \n"

            String replyString="202 \n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });

        } //else if (command.equals("EPSV")) // 扩展被动模式
        else if (command.equals("PORT")) // 要求服务器主动连接客户端的端口
        {
            //        elsif command=='EPSV'

            String replyString="150 \n"; // 回复内容。正在打开数据连接


        if (allowActiveMode) // 允许主动模式
        {
            openDataConnectionToClient(content); // 打开指向客户端特定端口的连接。

                         replyString="150 \n"; // 回复内容。正在打开数据连接

            } //if (allowActiveMode) // 允许主动模式
        else // 不允许主动模式。
        {

                     replyString="202 \n"; // 回复内容。未实现。
} //else // 不允许主动模式。


            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    Log.d(TAG, "[Server] Successfully wrote message");
                }
            });

        } //else if (command.equals("EPSV")) // Extended passive mode.
        else if (command.equals("list")) // 列出目录
        {
            processListCommand(content); // 处理目录列表命令。
            
        } //else if (command.equals("list")) // 列出目录
        else if (command.equals("retr")) // 获取文件
        {
//            陈欣

            String replyString="150 \n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });

            String data51=            content.substring(5);

data51=data51.trim(); // 去掉末尾换行


            sendFileContent(data51, currentWorkingDirectory); // 发送文件内容。
        } //else if (command.equals("list")) // 列出目录
        else if (command.equals("SIZE")) // 文件尺寸
        {
            String data51=            content.substring(5);

            data51=data51.trim(); // 去掉末尾换行

            processSizeCommand(data51); // 处理尺寸 命令。
        } //else if (command.equals("SIZE")) // 文件尺寸
        else if (command.equals("DELE")) // 删除文件
        {
        //        fileName=data[5..-1]
//        File.delete(fileName.strip)
//        send_data "250 \n"

String data51=            content.substring(5);

data51=data51.trim(); // 去掉末尾换行

// 删除文件。陈欣

                        String wholeDirecotoryPath= context.getFilesDir().getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
                    wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
                    Log.d(TAG, "processSizeCommand: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
                    
            File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

            photoDirecotry.delete();
            
            
            String replyString="250 \n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });

        } //else if (command.equals("DELE")) // 删除文件
        else if (command.equals("stor")) // 上传文件
        {
            String replyString="150 \n"; // 回复内容。

            sendStringInBinaryMode(replyString);

            String data51=            content.substring(5);

data51=data51.trim(); // 去掉末尾换行


            startStor(data51, currentWorkingDirectory); // 发送文件内容。

        } //else if (command.equals("stor")) // 上传文件

//        2021-08-29 20:57:40.287 16876-16916/com.stupidbeauty.builtinftp.demo D/Server: [Server] Received Message cwd /
//            2021-08-29 20:57:40.287 16876-16916/com.stupidbeauty.builtinftp.demo D/Server: command: cwd, content: cwd /
//            2021

//        def processCommand (command,data)
//        if command== 'USER'
    } //private void processCommand(String command, String content)

    /**
    * 处理目录列表命令。
    */
    private void processListCommand(String content) 
    {
        //陈欣
        String replyString="150 Opening BINARY mode data connection for file list, ChenXin\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                } //public void onCompleted(Exception ex) {
            });

            sendListContent(content, currentWorkingDirectory); // 发送目录列表数据。
    } //private void processListCommand(String content)

    /**
    * 上传文件内容。
    */
    private void startStor(String data51, String currentWorkingDirectory) 
    {
        String wholeDirecotoryPath= context.getFilesDir().getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
        Log.d(TAG, "startStor: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
                    
        File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
            
        writingFile=photoDirecotry; // 记录文件。
        isUploading=true; // 记录，处于上传状态。

//             陈欣

        if (photoDirecotry.exists())
        {
            photoDirecotry.delete();
        }
        
        try //尝试构造请求对象，并且捕获可能的异常。
		{
            FileUtils.touch(photoDirecotry); //创建文件。
        } //try //尝试构造请求对象，并且捕获可能的异常。
		catch (Exception e)
		{
			e.printStackTrace();
		}
    } //private void startStor(String data51, String currentWorkingDirectory) // 上传文件内容。

            private void handleConnectCompleted(Exception ex, final AsyncSocket socket) {
        if(ex != null) 
        {
            ex.printStackTrace(); //报告错误
        }
        else // 无异常。
        {
            this.data_socket=socket; // Remember the data connection.

//         Util.writeAll(socket, "Hello Server".getBytes(), new CompletedCallback() {
//             @Override
//             public void onCompleted(Exception ex) {
//                 if (ex != null) throw new RuntimeException(ex);
//                 System.out.println("[Client] Successfully wrote message");
//             }
//         });

        socket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
            {
                receiveDataSocket(bb);
            } //public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) 
        }); //socket.setDataCallback(new DataCallback() {

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully closed connection");
                
                data_socket=null;
                
                notifyStorCompleted(); // 告知上传完成。
            }
        });

        socket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully end connection");
                

            }
        });
        
        } //else // 无异常。
        
    }

        /**
     * Accept data connection.
     * @param socket 连接对象。
     */
    private void handleDataAccept(final AsyncSocket socket)
    {
        this.data_socket=socket;
        Log.d(TAG, "handleDataAccept, [Server] data New Connection " + socket.toString());
        
        if (dataSocketPendingByteArray!=null) // 有等待发送的内容。
        {
            Util.writeAll(data_socket, dataSocketPendingByteArray, new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) 
                {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] data Successfully wrote message");
                    
                    notifyLsCompleted(); // 告知已经发送目录数据。
                } //public void onCompleted(Exception ex) 
            });
        
            dataSocketPendingByteArray=null; // 数据置空。
        } // if (dataSocketPendingByteArray!=null)

        socket.setDataCallback(
            new DataCallback()
            {
                @Override
                public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                    receiveDataSocket(bb);
                }
            });

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] data Successfully closed connection");
                
                data_socket=null;
                
                if (isUploading) // 是处于上传状态。
                {
                    notifyStorCompleted(); // 告知上传完成。
                    
                    isUploading=false; // 不再处于上传状态了。
                } //if (isUploading) // 是处于上传状态。
            }
        });

        socket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] data Successfully end connection");
            }
        });
    } //private void handleDataAccept(final AsyncSocket socket)


    /**
     * 接受新连接
     * @param socket 新连接的套接字对象
     */
    public void handleAccept(final AsyncSocket socket)
    {
        this.socket=socket;
        System.out.println("[Server] New Connection " + socket.toString());

        socket.setDataCallback(
                new DataCallback()
                {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                String content = new String(bb.getAllByteArray());
                Log.d(TAG, "[Server] Received Message " + content); // Debug

                String command = content.split(" ")[0]; // Get the command.


                command=command.trim();

                processCommand(command, content); // 处理命令。
            }
        });

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) {
//                 throw new RuntimeException(ex);
ex.printStackTrace(); // 报告错误。
                }
                else
                {
                System.out.println("[Server] Successfully closed connection");
                }
                
            }
        });

        socket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) // 有异常出现
                {
//                 throw new RuntimeException(ex);
                    ex.printStackTrace(); // 报告。
                }
                else // 无异常
                {
                    Log.d(TAG, "ftpmodule [Server] Successfully end connection");
                } //else // 无异常
            }
        });

        //发送初始命令：
//        send_data "220 \n"

        Util.writeAll(socket, "220 BuiltinFtp Server\n".getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) 
            {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully wrote message");
            } //public void onCompleted(Exception ex) 
        });
    } //private void handleAccept(final AsyncSocket socket)

            /**
     * 启动数据传输服务器。
     */
    private void setupDataServer()
    {
        Random random=new Random(); //随机数生成器。

        int randomIndex=random.nextInt(65535-1025)+1025; //随机选择一个端口。

        data_port=randomIndex; 

//         try // 绑定端口。
//         {
        AsyncServer.getDefault().listen(host, data_port, new ListenCallback() {
            @Override
            public void onAccepted(final AsyncSocket socket)
            {
                handleDataAccept(socket);
            } //public void onAccepted(final AsyncSocket socket)

            @Override
            public void onListening(AsyncServerSocket socket)
            {
                System.out.println("[Server] Server started listening for data connections");
            }

            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) {
//                 09-07 07:57:47.473 18998 19023 W System.err: java.lang.RuntimeException: java.net.BindException: Address already in use

//                 throw new RuntimeException(ex);
ex.printStackTrace();

                    setupDataServer(); // 重新初始化。
                }
                else
                {
                System.out.println("[Server] Successfully shutdown server");
                }
                
            }
        });
//         }
//         catch (BindException e)
//         {
//         } //catch (BindException e)
    } //private void setupDataServer()
}
