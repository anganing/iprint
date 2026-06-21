#!/bin/bash

# 定义密码文件路径
PASSWORD_FILE="/etc/cups/data/password.txt"

# 如果没有提供用户名，使用默认值
if [ -z "$CUPS_USER" ]; then
    CUPS_USER="admin"
fi

if [ -z "$CUPS_PASSWORD" ]; then
    # 检查是否存在已保存的密码
    if [ -f "$PASSWORD_FILE" ]; then
        CUPS_PASSWORD=$(cat "$PASSWORD_FILE")
        echo "------------------------------------------------"
        echo "Using existing password for user: $CUPS_USER/$CUPS_PASSWORD"
        echo "------------------------------------------------"
    else
        # 生成6位随机密码
        CUPS_PASSWORD=$(openssl rand -base64 4 | tr -dc 'a-zA-Z0-9' | head -c 6)
        # 保存密码到文件
        mkdir -p $(dirname "$PASSWORD_FILE")
        echo "$CUPS_PASSWORD" > "$PASSWORD_FILE"
        chmod 600 "$PASSWORD_FILE"
        echo "================================================"
        echo "Default Username: $CUPS_USER"
        echo "Default Password: $CUPS_PASSWORD"
        echo "Password has been saved and will persist across restarts"
        echo "================================================"
    fi
fi

# 创建用户并设置密码
useradd -m -s /bin/bash $CUPS_USER 2>/dev/null || true
echo "$CUPS_USER:$CUPS_PASSWORD" | chpasswd

# 将用户添加到lpadmin组
usermod -aG lpadmin $CUPS_USER

# 启动CUPS服务
exec /usr/sbin/cupsd -f 