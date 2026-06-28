package io.github.haimfeng.parser;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.utils.NumberUtils;
import io.github.haimfeng.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class InputParser {

    private static final int DEFAULT_BIT_LENGTH = 8;
    private static final String DEFAULT_MODE = "int";
    private static final String DEFAULT_ALGORITHM = "booth";

    private final Scanner scanner;

    public InputParser() {
        this(null);
    }

    public InputParser(Scanner scanner) {
        this.scanner = scanner;
    }

    public record Config(int bitLength, boolean fractionMode, String algorithm, Operand op1, Operand op2) {}

    public Config parseFromCli(String[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        Map<String, String> paramMap = parseCliArgs(args);

        String bitsStr = paramMap.get("bits");
        String modeStr = paramMap.get("mode");
        String algoStr = paramMap.get("algo");
        String op1Str = paramMap.get("op1");
        String op2Str = paramMap.get("op2");

        if (bitsStr == null || algoStr == null || op1Str == null || op2Str == null) {
            System.out.println("[CLI] 参数不完整，需要: --bits, --algo, --op1, --op2。进入交互式模式...");
            return null;
        }

        if (!ValidationUtils.isValidBitLength(bitsStr)) {
            System.out.println("[CLI] 错误: --bits 值无效。请输入 1-64 之间的正整数，当前值: " + bitsStr);
            return null;
        }
        int bitLength = Integer.parseInt(bitsStr.trim());

        boolean fractionMode = resolveFractionMode(modeStr != null ? modeStr : DEFAULT_MODE);
        if (fractionMode == Boolean.parseBoolean(null) && modeStr != null) {
            System.out.println("[CLI] 错误: --mode 值无效。支持: int/integer, frac/fraction，当前值: " + modeStr);
            return null;
        }

        String algorithm = resolveAlgorithm(algoStr);
        if (algorithm == null) {
            System.out.println("[CLI] 错误: --algo 值无效。支持: booth, original1, original2, restore, nonrestore，当前值: " + algoStr);
            return null;
        }

        try {
            Operand op1 = fractionMode ? parseFractionOperand(op1Str, bitLength) : parseIntegerOperand(op1Str, bitLength);
            Operand op2 = fractionMode ? parseFractionOperand(op2Str, bitLength) : parseIntegerOperand(op2Str, bitLength);
            return new Config(bitLength, fractionMode, algorithm, op1, op2);
        } catch (NumberFormatException e) {
            System.out.println("[CLI] 错误: 操作数解析失败 — " + e.getMessage());
            return null;
        }
    }

    public Config parseInteractively() {
        System.out.println("=== ALU 定点数运算模拟器 ===");
        System.out.println();

        int bitLength = promptBitLength();
        System.out.println();
        boolean fractionMode = promptFractionMode();
        System.out.println();
        String algorithm = promptAlgorithm();
        System.out.println();

        System.out.println(fractionMode
                ? "  支持格式: 十进制小数(-0.5), 0d小数(0d0.5), 二进制小数(0b1.1)"
                : "  支持格式: 十进制(123/-5), 十六进制(0x7B/-0x7B), 八进制(0o173), 二进制(0b01111011)");

        Operand op1 = promptOperand("请输入操作数1", bitLength, fractionMode);
        Operand op2 = promptOperand("请输入操作数2", bitLength, fractionMode);
        System.out.println();

        return new Config(bitLength, fractionMode, algorithm, op1, op2);
    }

    private Map<String, String> parseCliArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--") && i + 1 < args.length) {
                String key = arg.substring(2);
                String value = args[i + 1];
                map.put(key, value);
                i++;
            }
        }
        return map;
    }

    private int promptBitLength() {
        while (true) {
            System.out.printf("请输入寄存器位数(1-64) [默认: %d]: ", DEFAULT_BIT_LENGTH);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return DEFAULT_BIT_LENGTH;
            }
            if (ValidationUtils.isValidBitLength(input)) {
                return Integer.parseInt(input);
            }
            System.out.println("  ❌ 输入无效: \"" + input + "\" 不是合法值。请输入 1-64 之间的正整数。");
            System.out.println("  💡 示例: 8, 16, 32");
        }
    }

    private boolean promptFractionMode() {
        while (true) {
            System.out.println("请选择数据模式:");
            System.out.println("  1. 整数模式 (int)");
            System.out.println("  2. 纯小数模式 (frac, 范围: [-1, 1))");
            System.out.printf("请输入编号或名称 [默认: %s]: ", DEFAULT_MODE);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return resolveFractionMode(DEFAULT_MODE);
            }
            Boolean result = tryResolveFractionMode(input);
            if (result != null) {
                return result;
            }
            System.out.println("  ❌ 输入无效: \"" + input + "\"。请输入 1/2 或 int/frac。");
            System.out.println("  💡 示例: 1 (整数), 2 (小数)");
        }
    }

    private String promptAlgorithm() {
        while (true) {
            System.out.println("请选择算法:");
            System.out.println("  1. Booth乘法 (booth)");
            System.out.println("  2. 原码一位乘 (original1)");
            System.out.println("  3. 原码两位乘 (original2)");
            System.out.println("  4. 恢复余数除法 (restore)");
            System.out.println("  5. 加减交替除法 (nonrestore)");
            System.out.printf("请输入编号或名称 [默认: %s]: ", DEFAULT_ALGORITHM);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return DEFAULT_ALGORITHM;
            }
            String result = resolveAlgorithm(input);
            if (result != null) {
                return result;
            }
            System.out.println("  ❌ 输入无效: \"" + input + "\"。请输入 1-5 或算法名称。");
            System.out.println("  💡 示例: 1, booth, restore");
        }
    }

    private Operand promptOperand(String label, int bitLength, boolean fractionMode) {
        while (true) {
            System.out.print(label + ": ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("  ❌ 输入不能为空，请重新输入。");
                continue;
            }
            try {
                return fractionMode ? parseFractionOperand(input, bitLength) : parseIntegerOperand(input, bitLength);
            } catch (NumberFormatException e) {
                System.out.println("  ❌ " + e.getMessage());
                if (fractionMode) {
                    System.out.println("  💡 支持格式: 十进制小数(-0.5), 0d小数(0d0.5), 二进制小数(0b1.1)");
                    long min = -(1L << (bitLength - 1));
                    long max = (1L << (bitLength - 1)) - 1;
                    System.out.printf("  💡 合法范围: 缩放后 [%d, %d]%n", min, max);
                } else {
                    System.out.println("  💡 支持格式: 十进制(123/-5), 十六进制(0x7B/-0x7B), 八进制(0o173), 二进制(0b01111011)");
                    long min = -(1L << (bitLength - 1));
                    long max = (1L << (bitLength - 1)) - 1;
                    System.out.printf("  💡 合法范围: [%d, %d]%n", min, max);
                }
            }
        }
    }

    private Operand parseIntegerOperand(String input, int bitLength) {
        long value = ValidationUtils.parseNumberValue(input);
        if (!ValidationUtils.isInRange(value, bitLength)) {
            long min = -(1L << (bitLength - 1));
            long max = (1L << (bitLength - 1)) - 1;
            throw new NumberFormatException(
                    String.format("数值 %d 超出 %d 位寄存器范围 [%d, %d]", value, bitLength, min, max));
        }
        int radix = ValidationUtils.detectRadix(input);
        boolean isNegative = value < 0;
        String twosComplement = NumberUtils.toTwosComplement(value, bitLength);
        return new Operand(input, value, radix, isNegative, bitLength, twosComplement);
    }

    private Operand parseFractionOperand(String input, int bitLength) {
        long scaledValue = ValidationUtils.parseFractionScaledValue(input, bitLength);
        if (!ValidationUtils.isFractionInRange(scaledValue, bitLength)) {
            throw new NumberFormatException(
                    String.format("小数值超出范围 [-1, 1)，缩放后值: %d", scaledValue));
        }
        double doubleValue = ValidationUtils.parseFractionDoubleValue(input);
        int radix = ValidationUtils.detectRadix(input);
        boolean isNegative = scaledValue < 0;
        String twosComplement = NumberUtils.toTwosComplement(scaledValue, bitLength);
        return new Operand(input, scaledValue, radix, isNegative, bitLength, twosComplement, doubleValue, true);
    }

    private boolean resolveFractionMode(String input) {
        Boolean result = tryResolveFractionMode(input);
        return result != null ? result : false;
    }

    private Boolean tryResolveFractionMode(String input) {
        String s = input.trim().toLowerCase();
        if (s.equals("1") || s.equals("int") || s.equals("integer")) {
            return false;
        }
        if (s.equals("2") || s.equals("frac") || s.equals("fraction")) {
            return true;
        }
        return null;
    }

    private String resolveAlgorithm(String input) {
        String s = input.trim().toLowerCase();
        return switch (s) {
            case "1", "booth" -> "booth";
            case "2", "original1" -> "original1";
            case "3", "original2" -> "original2";
            case "4", "restore" -> "restore";
            case "5", "nonrestore" -> "nonrestore";
            default -> null;
        };
    }
}
