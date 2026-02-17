# 使用本地预构建的基础镜像（安装wkhtmltopdf）
FROM ibootio/print-studio-base:17-jammy

# 设置工作目录
WORKDIR /app

# 预留外部配置挂载目录
RUN mkdir -p /app/config

# 复制 jar 文件
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# 暴露端口
EXPOSE 58080

# 默认激活 prod 环境
ENV SPRING_PROFILES_ACTIVE=prod

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
