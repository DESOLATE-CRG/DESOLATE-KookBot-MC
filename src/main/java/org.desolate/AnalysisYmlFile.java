package org.desolate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AnalysisYmlFile {
    public static String getYmlValue(String ymlPath, String cronName) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        InputStream input = null;
        try {
            input = new FileInputStream(ymlPath);
        } catch (FileNotFoundException e) {
            return null;
        }
        Map map;
        try {
            map = objectMapper.readValue(input, Map.class);
        } catch (IOException e) {
            return null;
        }
        String[] split = cronName.split("\\.");
        Map info = new HashMap();
        String cron = "";
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                info = (Map) map.get(split[i]);
            } else if (i == split.length - 1) {
                cron = (String) info.get(split[i]);
            } else {
                info = (Map) info.get(split[i]);
            }
            if (info == null) {
                return null;
            }
        }
        return cron;
    }
}