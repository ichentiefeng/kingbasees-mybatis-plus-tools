package com.afeng;

import com.alibaba.fastjson.JSON;
import com.afeng.config.MyBatisPlusConfig;
import com.afeng.model.ExecutionResult;
import com.afeng.model.InputParams;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.List;

public class MyBatisPlusTester {
    public static void main(String[] args) {
        ExecutionResult result = new ExecutionResult();
        try {
            // 1. 解析输入参数（JSON格式或Base64编码的JSON格式）
            if (args.length == 0) {
                result.setSuccess(false);
                result.setMessage("请传入JSON格式的参数");
                System.out.println(JSON.toJSONString(result));
                return;
            }
            
            String inputParam = args[0];
            String jsonString;
            
            // 检查参数是否为Base64编码
            if (isBase64Encoded(inputParam)) {
                // 解码Base64参数
                byte[] decodedBytes = Base64.getDecoder().decode(inputParam);
                jsonString = new String(decodedBytes);
            } else {
                // 直接使用参数作为JSON字符串
                jsonString = inputParam;
            }
            
            System.out.println("参数：" + jsonString);
            InputParams input = JSON.parseObject(jsonString, InputParams.class);

            // 2. 初始化MyBatis-Plus并获取SqlSessionFactory
            SqlSessionFactory factory = MyBatisPlusConfig.createSqlSessionFactory(input);

            // 3. 获取动态Mapper接口（基于namespace）
            Object mapper = MyBatisPlusConfig.getMapper(factory, input.getNamespace(), input);

            // 4. 反射调用Mapper方法（sqlId即方法名）
            if (input.getSqlId() == null || input.getSqlId().isEmpty()) {
                throw new IllegalArgumentException("sqlId不能为空");
            }
            Method method = findMethod(mapper.getClass(), input.getSqlId(), input.getSqlParams());
            Object executionData = method.invoke(mapper, getMethodParams(method, input.getSqlParams()));

            // 5. 构造成功结果
            result.setSuccess(true);
            result.setMessage("SQL执行成功");
            result.setData(executionData); // 查询返回数据，增删改返回影响行数

            // 6. 测试环境回滚事务（避免脏数据）
            SqlSession session = factory.openSession();
            session.rollback();
            session.close();

        } catch (Exception e) {
            e.printStackTrace();
            // 构造失败结果
            result.setSuccess(false);
            result.setMessage("执行失败：" + e.getMessage());
        } finally {
            // 输出结果（JSON格式，供Python解析）
            System.out.println(JSON.toJSONString(result));
        }
    }

    // 查找Mapper中与sqlId匹配的方法
    private static Method findMethod(Class<?> mapperClass, String methodName, Object params) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("方法名不能为空");
        }
        for (Method method : mapperClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new RuntimeException("未找到方法：" + methodName);
    }

    // 转换参数为方法所需格式（支持单参数或Map参数）
    private static Object[] getMethodParams(Method method, Object params) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return new Object[0];
        }
        // 简单处理：若参数为JSON对象，转换为Map或实体类（此处简化为直接传参）
        return new Object[]{params};
    }
    
    // 简单检查字符串是否为Base64编码
    private static boolean isBase64Encoded(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}