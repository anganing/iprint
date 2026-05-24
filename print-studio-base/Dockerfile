# 基础镜像: eclipse-temurin:17-jammy (JRE 17 on Ubuntu 22.04)
# 用途: 为 print-studio 提供 JRE 17 + wkhtmltopdf 运行环境，含中文字体
# 重建: docker build -t ibootio/print-studio-base:17-jammy .

FROM eclipse-temurin:17-jammy

LABEL maintainer="iboot"
LABEL description="Base image with JRE 17 and wkhtmltopdf for print-studio"

ENV TZ=Asia/Shanghai

RUN apt-get update && apt-get install -y --no-install-recommends \
      wget \
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
      fonts-wqy-zenhei \
 && wget -q https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6.1-3/wkhtmltox_0.12.6.1-3.jammy_amd64.deb \
 && dpkg -i wkhtmltox_0.12.6.1-3.jammy_amd64.deb || apt-get install -f -y \
 && rm wkhtmltox_0.12.6.1-3.jammy_amd64.deb \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

RUN wkhtmltopdf --version
