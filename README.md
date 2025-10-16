# Nethuns 🚀

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/xukyle-area/nethuns/pulls)

> 自动化交易系统

## 📋 项目概述
Nethuns 是一个基于 Java 和 Spring Boot 构建的自动化交易系统，主要用于加密货币交易策略的回测和实盘交易。该系统支持多种技术指标分析，并提供了完整的数据获取、策略执行和图表分析功能。

## 🚀 主要功能

- **多数据源支持**：支持 Binance API 和 CSV 文件数据源
- **技术指标分析**：内置 RSI、移动平均线、交叉指标等多种技术指标
- **交易策略**：支持自定义交易规则和策略
- **实时数据**：WebSocket 实时数据推送
- **图表分析**：基于 JFreeChart 的可视化图表生成
- **回测系统**：完整的历史数据回测功能
- **Excel 导出**：支持交易结果导出为 Excel 格式

## 📁 项目架构

项目采用多模块设计，各模块职责如下：

```
nethuns/
├── commons/          # 公共组件和工具类
├── data/            # 数据获取和处理模块
├── engine/          # 交易引擎和图表生成
├── strategy/        # 交易策略和应用入口
└── trade/          # 交易执行相关功能
```

### 模块详细说明

#### 🔧 Commons 模块
- 常量定义（交易对、时间周期等）
- 通用工具类（JSON、日期、集合操作等）
- 数据模型（K线、订单等）

#### 📊 Data 模块
- Binance API 集成（REST 和 WebSocket）
- CSV 数据读取
- Retrofit HTTP 客户端配置
- 数据转换和处理

#### ⚙️ Engine 模块
- 技术指标计算（RSI、移动平均等）
- 交易规则引擎
- 图表生成和可视化
- 回测执行引擎

#### 🎯 Strategy 模块
- Spring Boot 应用程序入口
- 具体交易策略实现
- REST API 控制器
- 应用配置和日志

#### 💰 Trade 模块
- 交易执行逻辑
- 订单管理
- 风险控制

## 🛠️ 技术栈

- **框架**：Spring Boot 2.7.18
- **构建工具**：Maven
- **Java 版本**：Java 8
- **HTTP 客户端**：Retrofit2 + OkHttp3
- **WebSocket**：Java-WebSocket
- **图表库**：JFreeChart
- **Excel 处理**：Apache POI
- **JSON 处理**：Gson
- **日志**：Logback + SLF4J
- **工具库**：Lombok, Apache Commons

## 🚀 快速开始

### 环境要求

- Java 8 或更高版本
- Maven 3.6+
- Binance API 密钥（如需实盘交易）

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/xukyle-area/nethuns.git
   cd nethuns
   ```

2. **编译项目**
   ```bash
   mvn clean install
   ```

3. **配置 API 密钥**（如需实盘交易）

   **方法 1：使用环境变量（推荐）**
   ```bash
   export BINANCE_API_KEY=your_api_key_here
   export BINANCE_API_SECRET=your_api_secret_here
   ```
   
   **方法 2：使用 .env 文件**
   ```bash
   # 复制示例文件
   cp .env.example .env
   
   # 编辑 .env 文件，填入真实的 API 密钥
   # BINANCE_API_KEY=your_api_key_here
   # BINANCE_API_SECRET=your_api_secret_here
   ```
   
   **方法 3：使用启动脚本**
   ```bash
   # 使用提供的启动脚本，会自动加载 .env 文件
   ./start.sh
   ```
   
   ⚠️ **重要安全提醒**：
   - 项目已移除所有硬编码的 API 密钥
   - `.env` 文件已被 `.gitignore` 忽略，不会提交到版本控制
   - 请勿在任何配置文件中直接写入真实的 API 密钥
   - 生产环境建议使用专门的密钥管理服务

4. **运行应用**
   
   **方法 1：使用启动脚本（推荐）**
   ```bash
   ./start.sh
   ```
   
   **方法 2：直接使用 Maven**
   ```bash
   cd strategy
   mvn spring-boot:run
   ```
   
   **方法 3：使用环境变量启动**
   ```bash
   export BINANCE_API_KEY=your_key
   export BINANCE_API_SECRET=your_secret
   cd strategy
   mvn spring-boot:run
   ```

## 📈 使用示例

### RSI 策略回测

```java
public class RsiStrategy {
    public static void main(String[] args) {
        Symbol symbol = Symbol.BTCUSDT;
        String startStr = "20240101";
        String endStr = "20241001";
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        
        // 获取历史K线数据
        Map<Long, Kline> klineMap = KlineService.getKLineMap(symbol, Period.D_1, start, end);
        
        // 创建RSI指标
        RsiIndicator rsiIndicator = new RsiIndicator(klineMap);
        
        // 定义交易规则
        CrossedDownIndicatorRule buyRule = new CrossedDownIndicatorRule(rsiIndicator, 30.0d);
        CrossedUpIndicatorRule sellRule = new CrossedUpIndicatorRule(rsiIndicator, 70.0d);
        
        // 执行回测
        TradeExecutor tradeExecutor = new TradeExecutor(klineMap, symbol, buyRule, sellRule);
        TradeExecutor.processAndExport(tradeExecutor);
    }
}
```

### 实时数据获取

```java
public class SocketMain {
    public static void main(String[] args) {
        String binanceUrl = "wss://stream.binance.com:9443/stream";
        SocketTask.startSocketJob(binanceUrl, Callback::klineCallback, 
                                Collections.singleton(Symbol.BTCUSDT),
                                Subscriber::subscribeKline);
    }
}
```

## 📊 支持的交易对

- **加密货币**：BTC/USDT, ETH/USDT, DOGE/USDT, PEPE/USDT, SOL/USDT
- **美股 ETF**：QQQ/USD, TQQQ/USD, SQQQ/USD

## 🔧 配置说明

### 日志配置

项目使用 Logback 进行日志管理，日志文件存储在 `alpha/logs/` 目录下：
- `application.log`：当前日志
- `application-yyyy-MM-dd.log`：按日期分割的历史日志

### API 配置

Binance API 配置位于 `Constants.java` 文件中：
```java
public static final String BINANCE_URL = "https://api.binance.com";
```

### 性能参数

关键性能参数配置：
- **RSI 周期**：6（可在 `Constants.java` 中调整）
- **初始资金**：10,000 USDT
- **交易手续费**：0.1%
- **最大数据天数**：1500天（Binance API 限制）

## 📁 数据存储

- **导出数据**：`alpha/export/` 目录下按日期和时间组织
- **CSV 数据**：`data/` 目录存放历史数据文件
- **日志文件**：`alpha/logs/` 目录

## � 测试

目前项目暂未包含单元测试，这是一个待改进的方面。建议：
- 为核心交易逻辑添加单元测试
- 为技术指标计算添加测试用例
- 添加集成测试验证 API 调用

## 🔧 故障排除

### 常见问题

**Q: API 调用失败怎么办？**
A: 检查 API 密钥配置是否正确，确保网络连接正常，查看日志文件获取详细错误信息。

**Q: 回测数据不准确？**
A: 确认数据源的时间范围不超过1500天，检查 K线数据的完整性。

**Q: 内存使用过高？**
A: 对于大量历史数据，考虑分批处理或增加 JVM 堆内存大小：`-Xmx2g`

**Q: 图表生成失败？**
A: 确保系统支持图形界面，或在无头模式下运行：`-Djava.awt.headless=true`

## �🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 🗺️ 发展路线图

### 已完成
- ✅ 基础交易系统架构
- ✅ Binance API 集成
- ✅ RSI 策略实现
- ✅ 图表生成和数据导出
- ✅ WebSocket 实时数据支持

### 进行中
- 🔄 更多技术指标（MACD、布林带等）
- 🔄 风险管理模块优化
- 🔄 Web UI 界面开发

### 计划中
- 📋 单元测试覆盖
- 📋 数据库持久化
- 📋 多交易所支持
- 📋 机器学习策略集成
- 📋 实时告警系统
- 📋 策略回测报告优化

## ⚠️ 免责声明

本项目仅供学习和研究使用。使用本系统进行实盘交易需要您自行承担风险。加密货币交易具有高风险性，可能导致资金损失。在使用本系统前，请确保您完全理解相关风险。

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📞 联系方式

- 项目仓库：[https://github.com/xukyle-area/nethuns](https://github.com/xukyle-area/nethuns)
- 问题反馈：请通过 GitHub Issues 提交

---

⭐ 如果这个项目对您有帮助，请给它一个星标！