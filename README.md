# ArithmeticLogicUnitSimulator

ALU 定点数运算模拟器 — 模拟计算机算术逻辑单元中的定点数运算算法，支持多进制输入、补码转换、分步演算及 Markdown + LaTeX 报告生成。

## 功能特性

- **5 种运算算法**：Booth 乘法、原码一位乘、原码两位乘、恢复余数除法、加减交替除法
- **多进制输入**：十进制、十六进制 (`0x`)、八进制 (`0o`)、二进制 (`0b`)
- **双模式支持**：整数模式 & 纯小数模式（范围 \([-1, 1)\)）
- **两种交互方式**：CLI 命令行参数 / 交互式引导输入
- **自动生成报告**：输出含 LaTeX 公式的 Markdown 演算报告（`output.md`），并自动打开

## 环境要求

- **Java 25+**
- **Maven 3.6+**

## 构建与运行

```bash
# 编译
mvn clean compile

# 交互式运行
mvn exec:java -Dexec.mainClass="io.github.haimfeng.Application"

# CLI 参数运行
mvn exec:java -Dexec.mainClass="io.github.haimfeng.Application" \
-Dexec.args="--bits 8 --mode int --algo booth --op1 5 --op2 6"
```

## CLI 参数说明

| 参数 | 说明 | 示例 |
| :---: | :--- | :--- |
| `--bits` | 寄存器位数（1-64） | `8` |
| `--mode` | 数据模式：`int` / `frac` | `int` |
| `--algo` | 算法选择 | `booth` |
| `--op1` | 操作数 1 | `5`, `-0x7B` |
| `--op2` | 操作数 2 | `6`, `0b01111011` |

> 参数不完整时自动回退到交互式输入模式。

## 支持的算法

| 编号 | 算法 | CLI 名称 | 说明 |
| :---: | :--- | :---: | :--- |
| 1 | Booth 乘法 | `booth` | 补码乘法，处理 Y₋₁ 判断操作 |
| 2 | 原码一位乘 | `original1` | 符号位单独处理，取绝对值运算 |
| 3 | 原码两位乘 | `original2` | 根据 Y 末两位 + C 判断操作 |
| 4 | 恢复余数除法 | `restore` | 试减后余数为负则恢复 |
| 5 | 加减交替除法 | `nonrestore` | 根据余数符号交替加减 |

## 输入格式

**整数模式**：

| 进制 | 格式 | 示例 |
| :---: | :--- | :--- |
| 十进制 | 直接输入或 `0d` 前缀 | `123`, `-5` |
| 十六进制 | `0x` 前缀 | `0x7B`, `-0x7B` |
| 八进制 | `0o` 前缀 | `0o173` |
| 二进制 | `0b` 前缀 | `0b01111011` |

**纯小数模式**：

| 格式 | 示例 |
| :--- | :--- |
| 十进制小数 | `-0.5` |
| `0d` 前缀小数 | `0d0.5` |
| 二进制小数 | `0b1.1` |

## 项目结构

```
src/main/java/io/github/haimfeng/
├── Application.java              # 程序入口
├── model/
│   ├── Operand.java              # 操作数模型
│   ├── Register.java             # 寄存器模型
│   └── StepRecord.java           # 步骤记录
├── parser/
│   └── InputParser.java          # 输入解析（CLI + 交互式）
├── report/
│   └── ReportGenerator.java      # Markdown 报告生成
├── simulator/
│   ├── ALUSimulator.java         # 运算调度
│   ├── exception/
│   │   └── OverflowException.java
│   └── operation/
│       ├── ArithmeticOperation.java    # 运算接口
│       ├── BoothMultiplier.java        # Booth 乘法
│       ├── OriginalOneMultiplier.java  # 原码一位乘
│       ├── OriginalTwoMultiplier.java  # 原码两位乘
│       ├── RestoreDivision.java        # 恢复余数除法
│       └── NonRestoreDivision.java     # 加减交替除法
└── utils/
		├── NumberUtils.java          # 进制转换、补码工具
		└── ValidationUtils.java      # 输入校验
```

## 示例

**交互式输入**：

```
=== ALU 定点数运算模拟器 ===

请输入寄存器位数(1-64) [默认: 8]: 8

请选择数据模式:
1. 整数模式 (int)
2. 纯小数模式 (frac, 范围: [-1, 1))
   请输入编号或名称 [默认: int]: 1

请选择算法:
1. Booth乘法 (booth)
2. 原码一位乘 (original1)
3. 原码两位乘 (original2)
4. 恢复余数除法 (restore)
5. 加减交替除法 (nonrestore)
   请输入编号或名称 [默认: booth]: 1

支持格式: 十进制(123/-5), 十六进制(0x7B/-0x7B), 八进制(0o173), 二进制(0b01111011)
请输入操作数1: 5
请输入操作数2: 6
```

**CLI 一行命令**：

```bash
mvn exec:java -Dexec.mainClass="io.github.haimfeng.Application" \
-Dexec.args="--bits 8 --algo booth --op1 5 --op2 6"
```

运算完成后自动生成 `output.md` 报告并打开。

## 许可证

本项目仅供学习参考使用。
