package com.afeng.config;


import com.afeng.model.InputParams;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.kingbase8.Driver;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.lang.ClassLoader;


public class MyBatisPlusConfig {

    /**
     * 动态创建SqlSessionFactory（加载指定XML文件）
     */
    public static SqlSessionFactory createSqlSessionFactory(InputParams input) throws Exception {
        // 1. 配置数据源
        DataSource dataSource = new DriverManagerDataSource();
        ((DriverManagerDataSource) dataSource).setDriverClassName(Driver.class.getName());
        ((DriverManagerDataSource) dataSource).setUrl(
                String.format("jdbc:kingbase8://%s:%d/%s", input.getHost(), input.getPort(), input.getDb())
        );
        ((DriverManagerDataSource) dataSource).setUsername(input.getUsername());
        ((DriverManagerDataSource) dataSource).setPassword(input.getPassword());

        // 2. 配置MyBatis-Plus
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // 3. 加载指定的MyBatis XML文件
        if (StringUtils.isNotBlank(input.getXmlFilePath())) {
//            Resource xmlResource = new FileSystemResource(new File(input.getXmlFilePath()));
            // 读取XML文件内容并修改
            String xmlContent = new String(
                    Files.readAllBytes(Paths.get(input.getXmlFilePath())),
                    StandardCharsets.UTF_8  // 指定编码
            );
            String modifiedXml = modifyXmlForMapResult(xmlContent); // 应用替换

            // 将修改后的XML内容作为临时资源加载
            Resource xmlResource = new ByteArrayResource(modifiedXml.getBytes(StandardCharsets.UTF_8));
            factoryBean.setMapperLocations(xmlResource);
        }

        // 4. 基础配置（如驼峰命名）
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }

    /**
     * 创建动态Mapper接口（无需手动定义接口，通过namespace匹配）
     */
    public static <T> T getMapper(SqlSessionFactory factory, String namespace, InputParams input) {
        SqlSession session = factory.openSession(ExecutorType.SIMPLE, false); // 不自动提交
        Class<?> mapperInterface = createMapperInterface(namespace, input);
        // 动态注册Mapper接口到MyBatis-Plus的MapperRegistry
        factory.getConfiguration().addMapper(mapperInterface);
        return session.getMapper((Class<T>) mapperInterface);
    }

    /**
     * 动态生成与XML namespace匹配的Mapper接口，解决ClassNotFoundException
     */
    private static Class<?> createMapperInterface(String namespace, InputParams input) {
        try {
            // 尝试加载已有接口，存在则直接返回
            return Class.forName(namespace);
        } catch (ClassNotFoundException e) {
            // 接口不存在，使用ASM动态生成
            return generateMapperInterface(namespace, input);
        }
    }

    /**
     * 使用ASM字节码库动态生成接口
     * @param namespace 接口全类名（如com.example.mapper.UserMapper）
     * @return 动态生成的接口Class对象
     */
    private static Class<?> generateMapperInterface(String namespace, InputParams input) {
        try {
            // 解析包名和类名
            int lastDotIndex = namespace.lastIndexOf('.');
            String packageName = lastDotIndex > 0 ? namespace.substring(0, lastDotIndex) : "";
            String className = lastDotIndex > 0 ? namespace.substring(lastDotIndex + 1) : namespace;
            String internalName = packageName.replace('.', '/') + "/" + className;

            // 1. 关键修复：接口修饰符仅保留 ACC_PUBLIC + ACC_INTERFACE + ACC_ABSTRACT
            // ACC_INTERFACE (0x0200) 已经隐含了 ACC_ABSTRACT (0x0400)
            // 但为了符合规范，建议同时设置 ACC_INTERFACE | ACC_ABSTRACT | ACC_PUBLIC
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            cw.visit(
                    Opcodes.V1_8,
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                    internalName,
                    null,
                    "java/lang/Object",
                    new String[0]
            );

            // 解析 XML 文件中的方法名并动态生成方法
            if (StringUtils.isNotBlank(input.getXmlFilePath())) {
                String xmlContent = new String(
                        Files.readAllBytes(Paths.get(input.getXmlFilePath())),
                        StandardCharsets.UTF_8
                );
                // 提取所有方法名（select/insert/update/delete 标签的 id 属性）
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "<(select|insert|update|delete)\\s+[^>]*id=\"([^\"]+)\"[^>]*>"
                );
                java.util.regex.Matcher matcher = pattern.matcher(xmlContent);
                while (matcher.find()) {
                    String sqlType = matcher.group(1);
                    String methodName = matcher.group(2);
                    
                    // 根据SQL类型和方法名特征判断返回类型
                    String returnType;
                    if ("select".equals(sqlType)) {
                        // 根据方法名特征判断是否返回列表
                        if (methodName.contains("List") || methodName.contains("list") || 
                            methodName.contains("Page") || methodName.contains("page") ||
                            methodName.contains("All") || methodName.contains("all") ||
                            methodName.contains("Statistics") || methodName.contains("Stats")) {
                            // 返回列表类型
                            returnType = "Ljava/util/List;";
                        } else {
                            // 默认返回单个对象
                            returnType = "Ljava/lang/Object;";
                        }
                    } else {
                        // insert/update/delete 通常返回受影响的行数（int）
                        returnType = "Ljava/lang/Integer;";
                    }
                    
                    // 动态生成抽象方法
                    MethodVisitor mv = cw.visitMethod(
                            Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                            methodName,
                            "(Ljava/util/Map;)" + returnType,
                            null,
                            null
                    );
                    mv.visitEnd();
                }
            }

            cw.visitEnd();

            // 4. 生成字节码
            byte[] classBytes = cw.toByteArray();

            // 调试：保存字节码文件（可选）
            try {
                String outputPath = System.getProperty("user.dir") + "/target/" + namespace.replace('.', '_') + ".class";
                Files.write(Paths.get(outputPath), classBytes);
                System.out.println("字节码文件已保存: " + outputPath);
            } catch (IOException e) {
                System.err.println("保存字节码文件失败: " + e.getMessage());
            }

            // 5. 使用安全的类加载方式
            ClassLoader customLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    if (name.equals(namespace)) {
                        return defineClass(name, classBytes, 0, classBytes.length);
                    }
                    return super.findClass(name);
                }
            };

            return customLoader.loadClass(namespace);

        } catch (Exception e) {
            throw new RuntimeException("动态生成Mapper接口失败: " + namespace, e);
        }
    }
    // 在MyBatisPlusTester.java中添加XML内容修改逻辑
    private static String modifyXmlForMapResult(String xmlContent) {
        // 1. 替换所有 <resultMap> 的 type 属性（核心修复点）
        xmlContent = xmlContent.replaceAll(
                "<resultMap\\s+id=\"([^\"]+)\"\\s+type=\"[^\"]+\"",   // 匹配 <resultMap id="xxx" type="实体类">
                "<resultMap id=\"$1\" type=\"java.util.Map\""       // 替换为 type="java.util.Map"
        );

        // 2. 替换 select/insert/update/delete 标签中的 resultType
        xmlContent = xmlContent.replaceAll(
                "resultType=\"[^\"]+\"",
                "resultType=\"java.util.Map\""
        );

        // 3. 修改 select 标签中的 resultMap 引用为 resultType（而不是简单移除）
        xmlContent = xmlContent.replaceAll(
                "(<select\\s+[^>]*?)resultMap=\"[^\"]+\"([^>]*?>)",
                "$1resultType=\"java.util.Map\"$2"
        );
        
        // 4. 对于非select标签，移除 resultMap 引用（insert/update/delete通常不需要）
        xmlContent = xmlContent.replaceAll(
                "(<(insert|update|delete)\\s+[^>]*?)resultMap=\"[^\"]+\"([^>]*?>)",
                "$1$3"
        );

        // 5. 替换 parameterType（如果有，统一用 Map 传参）
        xmlContent = xmlContent.replaceAll(
                "parameterType=\"[^\"]+\"",
                "parameterType=\"java.util.Map\""
        );

        return xmlContent;
    }
}