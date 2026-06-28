package io.github.haimfeng;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.model.StepRecord;
import io.github.haimfeng.parser.InputParser;
import io.github.haimfeng.report.ReportGenerator;
import io.github.haimfeng.simulator.ALUSimulator;
import io.github.haimfeng.simulator.operation.ArithmeticOperation;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        InputParser parser = new InputParser();
        InputParser.Config config = parser.parseFromCli(args);

        if (config == null) {
            Scanner scanner = new Scanner(System.in);
            parser = new InputParser(scanner);
            config = parser.parseInteractively();
            scanner.close();
        } else {
            System.out.println("=== ALU 定点数运算模拟器 ===");
            System.out.println("[CLI] 参数解析成功，直接执行运算...");
        }

        int bitLength = config.bitLength();
        boolean fractionMode = config.fractionMode();
        String algorithm = config.algorithm();
        Operand op1 = config.op1();
        Operand op2 = config.op2();

        String algorithmName = ALUSimulator.getOperationName(algorithm);
        ArithmeticOperation operation = ALUSimulator.getOperation(algorithm);
        List<StepRecord> steps = operation.execute(op1, op2);
        Map<String, String> resultMap = operation.getResultMap();

        String resultBinary;
        if (operation.isDivision()) {
            resultBinary = resultMap.get("quotientBinary");
        } else {
            resultBinary = resultMap.getOrDefault("resultBinary", resultMap.getOrDefault("resultMagnitude", ""));
        }

        boolean overflow = ALUSimulator.checkOverflow(algorithm, op1, op2, resultBinary);

        String outputPath = "output.md";
        ReportGenerator.generate(algorithmName, op1, op2, steps, resultBinary,
                fractionMode, overflow, outputPath, operation);

        if (operation.isDivision()) {
            System.out.println("运算完成！商: " + resultMap.get("quotientDecimal")
                    + ", 余数: " + resultMap.get("remainderDecimal"));
        } else if (fractionMode) {
            double resultDouble = op1.getActualDoubleValue() * op2.getActualDoubleValue();
            System.out.printf("运算完成！结果: %.6f%n", resultDouble);
        } else {
            System.out.println("运算完成！结果: " + resultMap.get("resultDecimal"));
        }

        try {
            File file = new File(outputPath);
            if (Desktop.isDesktopSupported() && file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            System.err.println("无法自动打开报告文件: " + e.getMessage());
        }
    }
}
