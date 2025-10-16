# Nethuns - 自动化交易系统

Nethuns 是一个基于 Java 和 Spring Boot 构建的自动化交易系统，主要用于加密货币交易策略的回测和实盘交易。该系统支持多种技术指标分析，并提供了完整的数据获取、策略执行和图表分析功能。

## 🚀 主要功能

- **多数据源支持**：支持 Binance API 和 CSV 文件数据源
- **技术指标分析**：内置 RSI 等多种技术指标
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

3. **配置 API 密钥**（可选）
   
   如需使用 Binance 实盘交易功能，请配置相关环境变量或修改配置文件。

4. **运行应用**
   ```bash
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

## 📁 数据存储

- **导出数据**：`alpha/export/` 目录下按日期和时间组织
- **CSV 数据**：`data/` 目录存放历史数据文件
- **日志文件**：`alpha/logs/` 目录

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## ⚠️ 免责声明

本项目仅供学习和研究使用。使用本系统进行实盘交易需要您自行承担风险。加密货币交易具有高风险性，可能导致资金损失。在使用本系统前，请确保您完全理解相关风险。

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📞 联系方式

- 项目仓库：[https://github.com/xukyle-area/nethuns](https://github.com/xukyle-area/nethuns)
- 问题反馈：请通过 GitHub Issues 提交

---

⭐ 如果这个项目对您有帮助，请给它一个星标！