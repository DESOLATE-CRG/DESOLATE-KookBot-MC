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

    //int类型转varint算法
    public static byte[] IntToVarint(int input) {
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



    //获取服务器状态(成功返回服务器信息数组，失败返回空字符串数组)

}