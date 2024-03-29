package org.desolate;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class JsonFileOperate {
    private String dataFolderPath = "";
    private final String playerDBFileName = "/PlayerDatabase.json";
    private final String administratorDBFileName = "/Administrator.json";
    private JSONObject currentClassPlayerInfo = new JSONObject();

    public void setDataFolderPath(String dataFolderPath) {
        this.dataFolderPath = dataFolderPath;
    }

    public JSONObject getCurrentClassPlayerInfo() {
        return currentClassPlayerInfo;
    }

    public boolean DatabaseFileInit() {
        try {
            File dbFile = new File(dataFolderPath + playerDBFileName);
            if (!dbFile.exists()) {
                dbFile.createNewFile();
                JSONArray jsonFileObj = new JSONArray();
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(dataFolderPath + playerDBFileName));
                fileWriter.write(jsonFileObj.toString());
                fileWriter.close();
                KookBotMain.MyLogger("数据库文件初始化成功");
                return true;
            } else {
                KookBotMain.MyLogger("已找到数据库文件");
                return true;
            }
        } catch (IOException e) {
            KookBotMain.MyLogger("数据库文件初始化失败");
            return false;
        }
    }

    public boolean IfPlayerIsNoBinding(String playerKookID) {
        try {
            //读取文件
            File targetFile = new File(dataFolderPath + playerDBFileName);
            FileReader fileReader = new FileReader(targetFile);
            Reader reader = new InputStreamReader(Files.newInputStream(targetFile.toPath()), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer stringBuffer = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            fileReader.close();
            reader.close();
            //构造JSONArray对象并判断目标用户是否存在
            JSONArray jsonArray = JSONArray.parse(stringBuffer.toString());
            String playerName = "None_Player_String";
            for (Object item : jsonArray) {
                JSONObject jsonObject = JSONObject.parse(item.toString());
                if (Objects.equals(jsonObject.getString("playerKookID"), playerKookID)) {
                    playerName = jsonObject.getString("playerName");
                    this.currentClassPlayerInfo = jsonObject;
                }
            }
            //判断玩家是否存在(true表示玩家不在数据库内,即未绑定)
            return Objects.equals(playerName, "None_Player_String");
        } catch (IOException e) {
            KookBotMain.MyLogger(e.getMessage());
            //抓取到错误默认判断用户绑定存在以防不可预料错误
            return true;
        }
    }

    public boolean AddNewBinding(String playerName, String playerUUID, String playerKookID) {
        try {
            //读取数据库文件并解析
            File jsonFile = new File(dataFolderPath + playerDBFileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer stringBuffer = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            fileReader.close();
            reader.close();
            JSONArray jsonArrayObject = JSONArray.parse(stringBuffer.toString());
            //构建新玩家数据信息
            JSONObject newPlayerObj = new JSONObject();
            newPlayerObj.put("playerName", playerName);
            newPlayerObj.put("playerUUID", playerUUID);
            newPlayerObj.put("playerKookID", playerKookID);
            //写入主数据库
            jsonArrayObject.add(newPlayerObj);
            //构建缓冲区写入流并实时写入文件
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(dataFolderPath + playerDBFileName));
            fileWriter.write(jsonArrayObject.toString());
            fileWriter.close();
            KookBotMain.MyLogger("检测到新写入内容，已更新文件");
            return true;
        } catch (IOException e) {
            KookBotMain.MyLogger("文件更新失败");
            return false;
        }
    }

    public boolean ChangePlayerBinding(String playerName, String playerNewName, String playerNewUUID) {
        try {
            //检测标志位
            boolean flag = false;
            //读取数据库文件并解析
            File jsonFile = new File(dataFolderPath + playerDBFileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer stringBuffer = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            fileReader.close();
            reader.close();
            //构建JsonArrayObj
            JSONArray playerDBObj = JSONArray.parse(stringBuffer.toString());
            //保存玩家数组位置
            int index = 0;
            //保存原玩家绑定信息
            JSONObject jsonObject = null;
            for (Object item : playerDBObj) {
                jsonObject = JSONObject.parse(item.toString());
                if (Objects.equals(jsonObject.getString("playerName"), playerName)) {
                    //检测到玩家信息
                    flag = true;
                    break;
                }
                index++;
            }
            if (flag) {
                //删除原玩家绑定信息
                playerDBObj.remove(index);
                //构建新玩家数据模型
                JSONObject newPlayerObj = new JSONObject();
                newPlayerObj.put("playerName", playerNewName);
                newPlayerObj.put("playerUUID", playerNewUUID);
                newPlayerObj.put("playerKookID", jsonObject.getString("playerKookID"));
                //写入主数据库
                playerDBObj.add(newPlayerObj);
                //保存更新数据库文件
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(dataFolderPath + playerDBFileName));
                fileWriter.write(playerDBObj.toString());
                fileWriter.close();
                KookBotMain.MyLogger("检测到新写入内容，已更新文件");
                return true;
            } else
                return false;
        } catch (IOException e) {
            KookBotMain.MyLogger(e.getMessage());
            return false;
        }
    }

    public boolean IfPlayerHavePermission(String playerKookID) {
        try {
            //检测标志位
            boolean flag = false;
            //读取文件
            File targetFile = new File(dataFolderPath + administratorDBFileName);
            FileReader fileReader = new FileReader(targetFile);
            Reader reader = new InputStreamReader(Files.newInputStream(targetFile.toPath()), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer stringBuffer = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            fileReader.close();
            reader.close();
            //构造JSON对象
            JSONObject administratorJsonObj = JSONObject.parse(stringBuffer.toString());
            //取出管理员ID数组
            JSONArray adArray = JSONArray.parse(administratorJsonObj.getString("adminArray"));
            for (Object item : adArray) {
                //检测到是管理员
                if (Objects.equals(item, playerKookID)) {
                    flag = true;
                    break;
                }
            }
            return flag;
        } catch (IOException e) {
            KookBotMain.MyLogger(e.getMessage());
            return false;
        }
    }
}