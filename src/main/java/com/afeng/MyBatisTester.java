package com.afeng;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyBatisTester {
    public static void main(String[] args) {
        // 接收Python传入的参数：XML文件路径、数据库配置、SQL ID列表
        String xmlFilePath = args[0];
        String host = args[1];
        String port = args[2];
        String db = args[3];
        String user = args[4];
        String password = args[5];
        String[] sqlIds = args[6].split(","); // 要测试的SQL ID，用逗号分隔

        try {
            // 加载MyBatis配置，替换占位符
            InputStream configStream = MyBatisTester.class.getClassLoader().getResourceAsStream("mybatis-config.xml");
            Properties props = new Properties();
            props.setProperty("host", host);
            props.setProperty("port", port);
            props.setProperty("db", db);
            props.setProperty("user", user);
            props.setProperty("password", password);
            props.setProperty("xmlFilePath", xmlFilePath);

            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(configStream, props);
            SqlSession session = factory.openSession();

            // 执行每个SQL ID（假设参数为动态构造的测试数据）
            for (String sqlId : sqlIds) {
                try {
                    // 根据SQL类型构造参数（示例：查询用空参数，增删改用测试数据）
                    Map<String, Object> params = new HashMap<>();
                    // 可根据SQL ID动态添加参数，例如：
                    // if (sqlId.equals("insertUser")) params.put("id", 123);

                    Object result = session.selectOne(sqlId, params); // 或insert/update/delete
                    System.out.println("SUCCESS:" + sqlId); // 输出成功标识
                } catch (Exception e) {
                    System.err.println("FAILED:" + sqlId + "|" + e.getMessage()); // 输出失败信息
                }
            }

            session.rollback(); // 测试环境不提交事务，避免脏数据
            session.close();
        } catch (Exception e) {
            System.err.println("ERROR:" + e.getMessage());
        }
    }
}