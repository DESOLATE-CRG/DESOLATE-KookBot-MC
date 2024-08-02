package org.desolate;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Path dbFilePath = Paths.get(dataFolderPath, playerDBFileName);

        try {
            if (Files.notExists(dbFilePath)) {
                Files.createFile(dbFilePath);

                JSONArray jsonFileObj = new JSONArray();
                try (BufferedWriter fileWriter = Files.newBufferedWriter(dbFilePath, StandardCharsets.UTF_8)) {
                    fileWriter.write(jsonFileObj.toString());
                }

                KookBotMain.MyLogger("数据库文件初始化成功");
            } else {
                KookBotMain.MyLogger("已找到数据库文件");
            }
            return true;
        } catch (IOException e) {
            KookBotMain.MyLogger("数据库文件初始化失败");
            return false;
        }
    }


    public boolean IfPlayerIsNoBinding(String playerKookID) {
        try {
            File targetFile = new File(dataFolderPath + playerDBFileName);
            String fileContent = new String(Files.readAllBytes(targetFile.toPath()), StandardCharsets.UTF_8);

            JSONArray jsonArray = JSONArray.parse(fileContent);
            String playerName = "None_Player_String";

            for (Object item : jsonArray) {
                JSONObject jsonObject = JSONObject.parse(item.toString());
                if (Objects.equals(jsonObject.getString("playerKookID"), playerKookID)) {
                    playerName = jsonObject.getString("playerName");
                    this.currentClassPlayerInfo = jsonObject;
                    break;
                }
            }

            return Objects.equals(playerName, "None_Player_String");
        } catch (IOException e) {
            KookBotMain.MyLogger(e.getMessage());
            return true; // 默认返回true，表示用户未绑定，以防止不可预料的错误
        }
    }

    public boolean AddNewBinding(String playerName, String playerUUID, String playerKookID) {
        Path filePath = Paths.get(dataFolderPath, playerDBFileName);

        try {
            // 读取数据库文件并解析
            String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            JSONArray jsonArrayObject = JSONArray.parseArray(fileContent);

            // 构建新玩家数据信息
            JSONObject newPlayerObj = new JSONObject();
            newPlayerObj.put("playerName", playerName);
            newPlayerObj.put("playerUUID", playerUUID);
            newPlayerObj.put("playerKookID", playerKookID);

            // 写入主数据库
            jsonArrayObject.add(newPlayerObj);
            Files.write(filePath, jsonArrayObject.toString().getBytes(StandardCharsets.UTF_8));

            KookBotMain.MyLogger("检测到新写入内容，已更新文件");
            return true;
        } catch (IOException e) {
            KookBotMain.MyLogger("文件更新失败");
            return false;
        }
    }

    public boolean ChangePlayerBinding(String playerName, String playerNewName, String playerNewUUID) {
        Path filePath = Paths.get(dataFolderPath, playerDBFileName);

        try {
            // 读取数据库文件并解析
            String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            JSONArray playerDBObj = JSONArray.parseArray(fileContent);

            // 查找原玩家绑定信息并更新
            boolean playerFound = false;
            for (int i = 0; i < playerDBObj.size(); i++) {
                JSONObject jsonObject = playerDBObj.getJSONObject(i);
                if (Objects.equals(jsonObject.getString("playerName"), playerName)) {
                    // 删除原玩家绑定信息
                    playerDBObj.remove(i);

                    // 构建新玩家数据模型
                    JSONObject newPlayerObj = new JSONObject();
                    newPlayerObj.put("playerName", playerNewName);
                    newPlayerObj.put("playerUUID", playerNewUUID);
                    newPlayerObj.put("playerKookID", jsonObject.getString("playerKookID"));

                    // 写入主数据库
                    playerDBObj.add(newPlayerObj);

                    // 保存更新数据库文件
                    Files.write(filePath, playerDBObj.toString().getBytes(StandardCharsets.UTF_8));

                    KookBotMain.MyLogger("检测到新写入内容，已更新文件");
                    playerFound = true;
                    break;
                }
            }

            return playerFound;
        } catch (IOException e) {
            KookBotMain.MyLogger(e.getMessage());
            return false;
        }
    }


    public boolean IfPlayerHavePermission(String playerKookID) {
        Path filePath = Paths.get(dataFolderPath, administratorDBFileName);

        try {
            // 读取文件并解析
            String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            JSONObject administratorJsonObj = JSONObject.parseObject(fileContent);

            // 取出管理员ID数组并检测权限
            JSONArray adminArray = administratorJsonObj.getJSONArray("adminArray");
            for (Object item : adminArray) {
                if (Objects.equals(item, playerKookID)) {
                    return true;
                }
            }

            return false;
        } catch (IOException e) {
            KookBotMain.MyLogger(e.getMessage());
            return false;
        }
    }
}