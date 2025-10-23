package com.afeng;


import com.afeng.util.JsonUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MyBatisTester {
    // 参数示例：{\"host\":\"192.168.0.12\",\"port\":\"54322\",\"db\":\"test-db\",\"username\":\"system\",\"password\":\"123456\",\"xmlFilePath\":\"C:\\Users\\imche\\Desktop\\work\\weizhou\\code\\ModuBoot\\BladeBoot300\\src\\main\\resources\\org\\springblade\\drp\\mapper\\PipelineFloodPreventionMapper.xml\",\"namespace\":\"org.springblade.drp.mapper.PipelineFloodPreventionMapper\",\"sqlIds\":[\"selectPipelineFloodPreventionPage\",\"selectDetailById\",\"selectStatusStatistics\",\"selectExceptionTypeStatistics\"],\"sqlParams\":{}}
    public static void main(String[] args) {
        try {
            // 解析Python传入的参数（JSON格式）
            if (args.length == 0) {
                System.err.println("参数错误：需传入JSON格式的配置参数");
                return;
            }
            String jsonParams = args[0];
            JSONObject params = JsonUtils.parseParams(jsonParams);

            // 提取数据库配置
            String host = params.getString("host");
            String port = params.getString("port");
            String db = params.getString("db");
            String username = params.getString("username");
            String password = params.getString("password");
            String xmlFilePath = params.getString("xmlFilePath");
            JSONObject sqlParams = params.getJSONObject("sqlParams"); // SQL执行所需参数

            // 加载MyBatis配置模板并替换占位符
            String configTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n" +
                    "<configuration>\n" +
                    "    <environments default=\"development\">\n" +
                    "        <environment id=\"development\">\n" +
                    "            <transactionManager type=\"JDBC\"/>\n" +
                    "            <dataSource type=\"POOLED\">\n" +
                    "                <property name=\"driver\" value=\"com.kingbase8.Driver\"/>\n" +
                    "                <property name=\"url\" value=\"jdbc:kingbase8://${host}:${port}/${db}\"/>\n" +
                    "                <property name=\"username\" value=\"${username}\"/>\n" +
                    "                <property name=\"password\" value=\"${password}\"/>\n" +
                    "            </dataSource>\n" +
                    "        </environment>\n" +
                    "    </environments>\n" +
                    "    <mappers>\n" +
                    "        <mapper url=\"file:///${xmlFilePath}\"/>\n" +
                    "    </mappers>\n" +
                    "</configuration>";

            // 替换模板中的占位符
            String configContent = configTemplate
                    .replace("${host}", host)
                    .replace("${port}", port)
                    .replace("${db}", db)
                    .replace("${username}", username)
                    .replace("${password}", password)
                    .replace("${xmlFilePath}", xmlFilePath);

            // 创建MyBatis会话
            InputStream configStream = new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8));
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(configStream);
            SqlSession session = factory.openSession();

            // 执行XML中的所有SQL（通过namespace+id定位）
            Map<String, Object> resultMap = new HashMap<>();
            String namespace = params.getString("namespace"); // MyBatis XML的namespace

            // 遍历所有需要测试的SQL ID
            for (String sqlId : params.getJSONArray("sqlIds").toJavaList(String.class)) {
                String fullSqlId = namespace + "." + sqlId; // 完整ID：namespace.id
                try {
                    // 执行SQL（根据类型自动选择方法）
                    Object result;
                    if (sqlId.startsWith("select")) {
                        result = session.selectOne(fullSqlId, sqlParams);
                    } else if (sqlId.startsWith("insert")) {
                        result = session.insert(fullSqlId, sqlParams);
                    } else if (sqlId.startsWith("update")) {
                        result = session.update(fullSqlId, sqlParams);
                    } else if (sqlId.startsWith("delete")) {
                        result = session.delete(fullSqlId, sqlParams);
                    } else {
                        result = "未知SQL类型";
                    }
                    resultMap.put(sqlId, "SUCCESS: " + result);
                } catch (Exception e) {
                    resultMap.put(sqlId, "FAILED: " + e.getMessage());
                }
            }

            // 测试环境不提交事务，直接回滚
            session.rollback();
            session.close();

            // 输出结果（JSON格式，便于Python解析）
            System.out.println(JSONObject.toJSONString(resultMap));

        } catch (Exception e) {
            // 输出错误信息
            System.err.println("测试失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}