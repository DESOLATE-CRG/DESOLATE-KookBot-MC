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

    //int类型转VarInt算法
    public static byte[] IntToVarInt(int input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (true) {
            if ((input & ~0x7F) == 0) {
                out.write(input);
                break;
            }
            out.write((input & 0x7F) | 0x80);
            input >>>= 7;
        }
        return out.toByteArray();
    }


    //VarInt类型字节流读取算法
    public static int readVarIntFromStream(DataInputStream in) throws IOException {
        int value = 0;
        int length = 0;
        byte currentByte;
        while (true) {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << (length * 7);
            length += 1;
            if (length > 5) {
                throw new RuntimeException("VarInt类型数据太大了");
            }
            if ((currentByte & 0x80) != 0x80) {
                break;
            }
        }
        return value;
    }

    //获取服务器状态(成功返回服务器信息数组，失败返回空字符串数组)

}