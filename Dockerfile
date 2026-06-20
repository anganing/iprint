# 单一镜像：JRE 17 + 中文字体 + wkhtmltox 原生库运行时依赖
# PDF 渲染由应用内置的 io.woo:htmltopdf（自带 wkhtmltox 原生库）完成，无需安装 wkhtmltopdf 命令行
FROM eclipse-temurin:17-jammy

LABEL maintainer="iboot"
LABEL description="print-studio runtime: JRE 17, CJK fonts and wkhtmltox native runtime deps"

ENV TZ=Asia/Shanghai

# 以下 lib* 是 io.woo:htmltopdf 内置 wkhtmltox 原生库的运行时依赖，必须保留；
# fonts-noto-cjk 提供中文字形；xfonts-* 为 Qt 渲染所需位图字体。
RUN apt-get update && apt-get install -y --no-install-recommends \
      fontconfig \
      libfreetype6 \
      libjpeg-turbo8 \
      libpng16-16 \
      libx11-6 \
      libxcb1 \
      libxext6 \
      libxrender1 \
      xfonts-75dpi \
      xfonts-base \
      fonts-noto-cjk \
 && fc-cache -f \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

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
