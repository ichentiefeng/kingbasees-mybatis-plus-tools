package com.afeng.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonUtils {
    public static JSONObject parseParams(String jsonStr) {
        try {
            return JSON.parseObject(jsonStr);
        } catch (Exception e) {
            throw new RuntimeException("参数解析失败（JSON格式错误）：" + e.getMessage());
        }
    }
}