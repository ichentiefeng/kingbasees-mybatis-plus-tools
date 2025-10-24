package com.afeng.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class InputParams {
    // 数据库连接信息
    private String host;
    private int port;
    private String db;
    private String username;
    private String password;
    // MyBatis XML信息
    private String xmlFilePath; // XML文件绝对路径
    private String namespace;   // XML的namespace（需与动态Mapper接口对应）
    private String sqlId;       // 要执行的SQL ID（如"selectById"）
    // 执行参数
    private JSONObject sqlParams;  // SQL所需参数（如{"id": 1}）
}