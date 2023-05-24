package org.desolate;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GetMcServerDataPackAnalysis {
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
    private byte[] IntToVarInt(int input) {
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
    private int readVarIntFromStream(DataInputStream in) throws IOException {
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
    private String GetMcInfoDataPack() {
        try {
            //构建数据包输入参数
            ByteArrayOutputStream byteArrayOutputStream_Input = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream_Input = new DataOutputStream(byteArrayOutputStream_Input);

            //构建数据包
            dataOutputStream_Input.write(0x00);
            dataOutputStream_Input.write(IntToVarInt(755));
            dataOutputStream_Input.write(IntToVarInt(serverAddress.length()));
            dataOutputStream_Input.writeBytes(serverAddress);
            dataOutputStream_Input.writeShort(serverPort);
            dataOutputStream_Input.write(IntToVarInt(0x01));

            //与服务器建立Socket连接
            Socket socket = new Socket(serverAddress, serverPort);

            //构建服务器返回数据包接收参数
            DataOutputStream dataOutputStream_Result = new DataOutputStream(socket.getOutputStream());

            //重构数据包
            dataOutputStream_Result.write(IntToVarInt(byteArrayOutputStream_Input.size()));
            dataOutputStream_Result.write(byteArrayOutputStream_Input.toByteArray());
            dataOutputStream_Result.writeByte(0x01);
            dataOutputStream_Result.writeByte(0x00);

            //解析数据包
            DataInputStream dataInputStream_Result = new DataInputStream(socket.getInputStream());
            readVarIntFromStream(dataInputStream_Result);
            //数据包标志
            //int FLAG = readVarIntFromStream(dataInputStream_Result);
            //数据包长度
            int LENGTH = readVarIntFromStream(dataInputStream_Result);
            byte[] data = new byte[LENGTH];
            dataInputStream_Result.readFully(data);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return new String("");
        }
    }
}