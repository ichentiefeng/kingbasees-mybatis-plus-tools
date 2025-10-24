package com.afeng;
import com.alibaba.fastjson.JSON;
import com.afeng.config.MyBatisPlusConfig;
import com.afeng.model.ExecutionResult;
import com.afeng.model.InputParams;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class SqlSessionTester {
    public static void main(String[] args) {
        ExecutionResult result = new ExecutionResult();
        try {
            // 解析输入参数
            if (args.length == 0) {
                result.setSuccess(false);
                result.setMessage("请传入JSON参数");
                System.out.println(JSON.toJSONString(result));
                return;
            }
            InputParams input = JSON.parseObject(args[0], InputParams.class);

            // 创建SqlSessionFactory（加载修改后的XML，替换resultMap为Map）
            SqlSessionFactory factory = MyBatisPlusConfig.createSqlSessionFactory(input);
            SqlSession session = factory.openSession(false); // 不自动提交

            // 直接通过SqlSession执行SQL（无需Mapper接口）
            // 格式：namespace.sqlId（如org.springblade.drp.mapper.PipelineFloodPreventionMapper.selectById）
            String fullSqlId = input.getNamespace() + "." + input.getSqlId();
            Object data = session.selectOne(fullSqlId, input.getSqlParams()); // 适用于查询
            // 若为增删改，使用session.insert/fullSqlId, params)等

            session.rollback(); // 回滚事务
            session.close();

            result.setSuccess(true);
            result.setMessage("SQL执行成功");
            result.setData(data);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("执行失败：" + e.getMessage());
        }
        System.out.println(JSON.toJSONString(result));
    }
}
