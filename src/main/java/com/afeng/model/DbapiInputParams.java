package com.afeng.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class DbapiInputParams {
    private String dbType;
    private String driver;
    // 数据库连接信息
    private String host;
    private int port;
    private String db;
    private String username;
    private String password;
    private String sql;       // 要执行的SQL
    // 执行参数
    private JSONObject sqlParams;  // SQL所需参数（如{"id": 1}）
}