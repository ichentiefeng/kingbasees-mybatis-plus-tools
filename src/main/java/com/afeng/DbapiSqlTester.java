package com.afeng;

import com.afeng.model.ExecutionResult;
import com.afeng.model.DbapiInputParams;
import com.alibaba.fastjson.JSON;
import com.gitee.freakchicken.dbapi.basic.util.JdbcUtil;
import com.gitee.freakchicken.dbapi.basic.util.SqlEngineUtil;
import com.github.freakchick.orange.SqlMeta;
import com.kingbase8.Driver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DbapiSqlTester {
    public static void main(String[] args) {
        Connection connection = null;
        ExecutionResult result = new ExecutionResult();
        try {
            // 1. 解析输入参数（JSON格式）
            if (args.length == 0) {
                result.setSuccess(false);
                result.setMessage("请传入JSON格式的参数");
                System.out.println(JSON.toJSONString(result));
                return;
            }
            DbapiInputParams input = JSON.parseObject(args[0], DbapiInputParams.class);

            DataSource dataSource = new DriverManagerDataSource();
            ((DriverManagerDataSource) dataSource).setDriverClassName(Driver.class.getName());
            ((DriverManagerDataSource) dataSource).setUrl(
                    String.format("jdbc:kingbase8://%s:%d/%s", input.getHost(), input.getPort(), input.getDb())
            );
            ((DriverManagerDataSource) dataSource).setUsername(input.getUsername());
            ((DriverManagerDataSource) dataSource).setPassword(input.getPassword());

            connection = dataSource.getConnection();
            Map<String, Object> map = input.getParams();
            SqlMeta sqlMeta = SqlEngineUtil.getEngine().parse(input.getSql(), map);
            Object data = JdbcUtil.executeSql(connection, sqlMeta.getSql(), sqlMeta.getJdbcParamValues());
            System.out.println(data);
            // 5. 构造成功结果
            result.setSuccess(true);
            result.setMessage("SQL执行成功");
            result.setData(data); // 查询返回数据，增删改返回影响行数
        } catch (Exception e) {
            e.printStackTrace();
            // 构造失败结果
            result.setSuccess(false);
            result.setMessage("执行失败：" + e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // 输出结果（JSON格式，供Python解析）
            System.out.println(JSON.toJSONString(result));
        }
    }
}
