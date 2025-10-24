### 项目名称：kingbasees-mybatis-plus-tools

#### 项目简介
这是一个基于 MyBatis-Plus 和 KingbaseES 数据库的工具项目，用于演示 MyBatis-Plus 与 KingbaseES 的集成使用。

#### 技术栈
- **数据库**: KingbaseES 9.0.0
- **ORM框架**: MyBatis-Plus 3.4.2
- **日志**: SLF4J 1.7.36
- **其他依赖**: SpringBlade DRP 3.0.0

#### 环境要求
- JDK 8+
- Maven 3.6+
- KingbaseES 数据库

#### 快速开始
1. **克隆项目**
   ```bash
   git clone <项目仓库地址>
   cd kingbasees-mybatis-plus-tools
   ```

2. **配置数据库**
   - 修改 `src/main/resources/application.yml` 文件中的数据库连接信息。

3. **构建项目**
   ```bash
   mvn clean package
   ```

4. **运行项目**
   ```bash
   java -cp "target/classes;target/dependency/*" com.afeng.MyBatisTester <参数>
   ```
   或者使用jar包执行：
    ```bash
   java -jar D:\workspace\workspace\dbapi-learn\kingbasees-mybatis-plus-tools\target\kingbasees-mybatis-plus-tools-1.0-SNAPSHOT-jar-with-dependencies.jar java -jar D:\workspace\workspace\dbapi-learn\kingbasees-mybatis-plus-tools\target\kingbasees-mybatis-plus-tools-1.0-SNAPSHOT-jar-with-dependencies.jar '{\"host\":\"192.168.0.12\",\"port\":\"54322\",\"db\":\"test-db\",\"username\":\"system\",\"password\":\"123456\",\"xmlFilePath\":\"C:\\Users\\imche\\Desktop\\work\\weizhou\\code\\ModuBoot\\BladeBoot300\\src\\main\\resources\\org\\springblade\\drp\\mapper\\PipelineFloodPreventionMapper.xml\",\"namespace\":\"org.springblade.drp.mapper.PipelineFloodPreventionMapper\",\"sqlIds\":[\"selectPipelineFloodPreventionPage\",\"selectDetailById\",\"selectStatusStatistics\",\"selectExceptionTypeStatistics\"],\"sqlParams\":{}}'
    ```
5. 测试示例：
```bash
java -jar target/kingbasees-mybatis-plus-tools-1.0-SNAPSHOT-jar-with-dependencies.jar '{\"host\":\"192.168.0.12\",\"port\":\"54322\",\"db\":\"wz_mengde_product\",\"username\":\"system\",\"password\":\"szjq1234!\",\"xmlFilePath\":\"C:\\Users\\imche\\Desktop\\work\\weizhou\\code\\ModuBoot\\BladeBoot300\\src\\main\\resources\\org\\springblade\\drp\\mapper\\PipelineFloodPreventionMapper.xml\",\"namespace\":\"org.springblade.drp.mapper.PipelineFloodPreventionMapper\",\"sqlId\":\"selectStatusStatistics\",\"sqlParams\":{}}' 
```
6. 测试DBAPI
```bash
java -cp .\target\kingbasees-mybatis-plus-tools-1.0-SNAPSHOT-jar-with-dependencies.jar com.afeng.DbapiSqlTester '{\"host\":\"192.168.0.12\",\"port\":\"54322\",\"db\":\"wz_mengde_product\",\"username\":\"system\",\"password\":\"szjq1234!\",\"sql\":\"select\tversion();\",\"sqlParams\":{}}' 
```
7. 

#### 依赖管理
项目的依赖已通过 Maven 管理，具体依赖见 `pom.xml` 文件。

#### 常见问题
1. **依赖未加载**  
   确保运行命令中包含 `target/dependency/*` 以加载所有依赖。

2. **类未找到**  
   检查 `pom.xml` 是否包含所有必要的依赖。

3. **数据库连接失败**  
   确认数据库服务已启动，且连接信息正确。

#### 贡献指南
欢迎提交 Pull Request 或 Issue。