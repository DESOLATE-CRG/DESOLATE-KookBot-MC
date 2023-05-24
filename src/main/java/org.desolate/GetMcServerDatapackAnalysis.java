package org.desolate;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GetMcServerDatapackAnalysis {
    private String serverAddress = "0.0.0.0";
    private int serverPort = 0;

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    //获取服务器状态(成功返回服务器信息数组，失败返回空字符串数组)
    public ArrayList<String> GetServerStatus() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            // 发送查询请求数据包
            outputStream.writeByte(0x00);
            outputStream.writeByte(0x00);
            outputStream.writeByte(0x09);
            outputStream.writeBytes("MC|PingHost");
            outputStream.writeShort(serverAddress.length());
            outputStream.writeBytes(serverAddress);
            outputStream.writeInt(serverPort);
            //接收数据对象
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            ByteArrayOutputStream responseData = new ByteArrayOutputStream();
            // 读取响应数据包
            int packetID = inputStream.read();
            if (packetID == -1 || packetID != 0x00) {
                throw new IOException("无效的响应数据包");
            }
            //解析数据包
            int sessionID = inputStream.read();
            int responseDataLength = inputStream.readShort();
            byte[] buffer = new byte[responseDataLength];
            inputStream.readFully(buffer);
            responseData.write(buffer);

            ByteArrayInputStream responseStream = new ByteArrayInputStream(responseData.toByteArray());
            DataInputStream responseInputStream = new DataInputStream(responseStream);
            // 跳过无关数据
            responseInputStream.readByte(); // 打包类型：响应
            responseInputStream.readByte(); // 会话ID
            responseInputStream.readByte(); // 数据包类型：查询状态
            //重新封装数据
            ArrayList<String> result = new ArrayList<>();
            // 读取服务器信息
            int protocolVersion = Integer.reverseBytes(responseInputStream.readInt());
            result.add("协议版本:" + protocolVersion);
            String serverVersion = readString(responseInputStream);
            result.add("服务器版本:" + serverVersion);
            String motd = readString(responseInputStream);
            result.add("motd:" + motd);
            int onlinePlayers = responseInputStream.read();
            result.add("在线玩家数:" + onlinePlayers);
            int maxPlayers = responseInputStream.read();
            result.add("最大玩家数:" + maxPlayers);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    //字符串辅助读取方法
    private String readString(DataInputStream inputStream) throws IOException {
        int length = inputStream.readShort();
        byte[] bytes = new byte[length];
        inputStream.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}