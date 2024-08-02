package org.desolate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AnalysisYmlFile {
    private String ymlFilePath = "";
    private String dataFilePath = "";

    public void setYmlFilePath(String ymlFilePath, String dataFilePath) {
        this.ymlFilePath = ymlFilePath;
        this.dataFilePath = dataFilePath;
    }

    // 通用的方法从指定路径读取YML文件
    private Map<String, Object> readYmlFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        InputStream input;
        try {
            input = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            return null;
        }
        try {
            return objectMapper.readValue(input, Map.class);
        } catch (IOException e) {
            return null;
        }
    }

    // 解析yml封装，读取ymlFilePath
    public String getYmlValueFromYmlFile(String ymlKeyPath) {
        return getYmlValue(ymlKeyPath, ymlFilePath);
    }

    // 通用的方法从指定文件路径解析YML值
    private String getYmlValue(String ymlKeyPath, String filePath) {
        Map<String, Object> map = readYmlFile(filePath);
        if (map == null) {
            return null;
        }

        String[] split = ymlKeyPath.split("\\.");
        Map<String, Object> info = map;
        String cron = "";

        for (int i = 0; i < split.length; i++) {
            if (i == split.length - 1) {
                Object value = info.get(split[i]);
                if (value instanceof String) {
                    cron = (String) value;
                } else if (value instanceof Integer) {
                    cron = Integer.toString((Integer) value);
                }
            } else {
                info = (Map<String, Object>) info.get(split[i]);
                if (info == null) {
                    return null;
                }
            }
        }
        return cron;
    }

    public Set<String> getKeysForPlayer(String playerName) {
        Map<String, Object> dataMap = readYmlFile(dataFilePath);
        if (dataMap == null) {
            return Collections.emptySet();
        }
        Map<String, Object> playerData = (Map<String, Object>) dataMap.get(playerName);
        return playerData != null ? playerData.keySet() : Collections.emptySet();
    }

    public Object getPlayerKeyValue(String playerName, String key) {
        Map<String, Object> dataMap = readYmlFile(dataFilePath);
        if (dataMap == null) {
            return null;
        }
        Map<String, Object> playerData = (Map<String, Object>) dataMap.get(playerName);
        return playerData != null ? playerData.get(key) : null;
    }
}