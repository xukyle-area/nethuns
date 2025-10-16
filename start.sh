#!/bin/bash

# Nethuns 启动脚本
# 此脚本帮助您安全地启动应用程序，使用环境变量来管理敏感信息

echo "=== Nethuns 自动交易系统启动脚本 ==="

# 检查是否存在 .env 文件
if [ -f ".env" ]; then
    echo "✅ 检测到 .env 文件，正在加载环境变量..."
    # 加载 .env 文件中的环境变量
    export $(grep -v '^#' .env | xargs)
    echo "   📋 配置流程: .env文件 → 环境变量 → application.properties → Spring Boot"
else
    echo "⚠️  警告：未找到 .env 文件"
    echo "   配置步骤："
    echo "   1. cp .env.example .env"
    echo "   2. 编辑 .env 文件，填入真实的 API 密钥"
    echo "   3. 重新运行 ./start.sh"
    echo ""
fi

# 检查必要的环境变量
if [ -z "$BINANCE_API_KEY" ] || [ -z "$BINANCE_API_SECRET" ]; then
    echo "⚠️  Binance API 配置状态检查："
    echo "   BINANCE_API_KEY: ${BINANCE_API_KEY:-❌ 未设置}"
    echo "   BINANCE_API_SECRET: ${BINANCE_API_SECRET:-❌ 未设置}"
    echo ""
    echo "💡 配置说明："
    echo "   - .env 文件中的变量会自动设置为环境变量"
    echo "   - application.properties 使用 \${BINANCE_API_KEY} 读取环境变量"
    echo "   - AuthInterceptor 最终使用这些配置进行 API 认证"
    echo ""
    echo "如果您只进行回测，可以忽略此警告"
    echo "如果需要实盘交易，请设置环境变量或创建 .env 文件"
    echo ""
    
    read -p "是否继续启动？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "启动已取消"
        exit 1
    fi
else
    echo "✅ Binance API 配置已加载："
    echo "   BINANCE_API_KEY: ${BINANCE_API_KEY:0:8}***${BINANCE_API_KEY: -4} (已隐藏中间部分)"
    echo "   BINANCE_API_SECRET: ${BINANCE_API_SECRET:0:8}***${BINANCE_API_SECRET: -4} (已隐藏中间部分)"
    echo "   📋 这些环境变量将通过 application.properties 注入到 Spring Boot 应用中"
fi

echo ""
echo "正在启动应用程序..."
echo "端口: ${SERVER_PORT:-8080}"
echo "Java 选项: ${JAVA_OPTS:--Djava.awt.headless=true -Xmx1g}"
echo ""

# 进入 strategy 目录并启动应用
cd strategy

# 使用 Maven 启动 Spring Boot 应用
mvn spring-boot:run \
    -Dspring-boot.run.jvmArguments="${JAVA_OPTS:--Djava.awt.headless=true -Xmx1g}" \
    -Dspring-boot.run.arguments="--server.port=${SERVER_PORT:-8080}"