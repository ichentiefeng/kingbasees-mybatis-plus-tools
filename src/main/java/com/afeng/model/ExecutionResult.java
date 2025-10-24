package com.afeng.model;

import lombok.Data;

@Data
public class ExecutionResult {
    private boolean success;       // 是否执行成功
    private String message;        // 成功/失败信息
    private Object data;           // 执行结果（查询返回的数据，增删改返回影响行数）
    private String sql;            // 实际执行的SQL（可选，用于调试）
}