package io.github.haimfeng.report;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.model.StepRecord;
import io.github.haimfeng.simulator.operation.ArithmeticOperation;
import io.github.haimfeng.utils.NumberUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    public static void generate(String algorithmName, Operand op1, Operand op2,
                                List<StepRecord> steps, String resultBinary,
                                boolean fractionMode, boolean overflow, String outputPath,
                                ArithmeticOperation operation) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> resultMap = operation.getResultMap();

        sb.append("# ").append(algorithmName).append("\n\n");

        sb.append("$$\n");
        if (fractionMode) {
            sb.append(String.format("\\displaystyle X = %.6f\\;(\\mathtt{%s}),\\quad Y = %.6f\\;(\\mathtt{%s}),\\quad n = %d\n",
                    op1.getActualDoubleValue(), op1.getTwosComplement(),
                    op2.getActualDoubleValue(), op2.getTwosComplement(),
                    op1.getBitLength()));
        } else {
            sb.append(String.format("\\displaystyle X = %d\\;(\\mathtt{%s}),\\quad Y = %d\\;(\\mathtt{%s}),\\quad n = %d\n",
                    op1.getDecimalValue(), op1.getTwosComplement(),
                    op2.getDecimalValue(), op2.getTwosComplement(),
                    op1.getBitLength()));
        }
        sb.append("$$\n\n");

        sb.append(algorithmName).append(" Steps:\n");
        String[] headers = operation.getColumnHeaders();
        sb.append(String.format("| %s | %s | %s | %s |\n",
                headers[0], headers[1], headers[2], headers[3]));
        sb.append("| :---: | :---: | :---: | :--- |\n");
        for (StepRecord step : steps) {
            sb.append(String.format("| %s | %s | %s | %s |\n",
                    step.getStepName(),
                    escapeMd(step.getPartialProduct()),
                    escapeMd(step.getYAndYMinus1()),
                    step.getDescription()));
        }
        sb.append("\n");

        sb.append("## Verification\n");
        sb.append("$$\n");
        sb.append("\\begin{array}{l}\n");

        if (operation.isDivision()) {
            int m = op1.getBitLength() - 1;
            char signX = op1.getTwosComplement().charAt(0);
            char signY = op2.getTwosComplement().charAt(0);
            char resultSign = (char) (((signX - '0') ^ (signY - '0')) + '0');

            String quotientBin = resultMap.get("quotientBinary");
            String remainderBin = resultMap.get("remainderBinary");
            long algQuotient = Long.parseLong(resultMap.get("quotientDecimal"));
            long algRemainder = Long.parseLong(resultMap.get("remainderDecimal"));

            long javaQuotient = op1.getDecimalValue() / op2.getDecimalValue();
            long javaRemainder = op1.getDecimalValue() % op2.getDecimalValue();

            boolean quotientMatch = (algQuotient == Math.abs(javaQuotient));
            boolean remainderMatch = (algRemainder == Math.abs(javaRemainder));
            boolean match = quotientMatch && remainderMatch;

            if (fractionMode) {
                sb.append(String.format("X = %.6f = \\mathtt{%s} \\\\\n", op1.getActualDoubleValue(), op1.getTwosComplement()));
                sb.append(String.format("Y = %.6f = \\mathtt{%s} \\\\\n", op2.getActualDoubleValue(), op2.getTwosComplement()));
            } else {
                sb.append(String.format("X = %d = \\mathtt{%s} \\\\\n", op1.getDecimalValue(), op1.getTwosComplement()));
                sb.append(String.format("Y = %d = \\mathtt{%s} \\\\\n", op2.getDecimalValue(), op2.getTwosComplement()));
            }
            sb.append(String.format("X \\div Y: \\quad \\text{quotient} = %d,\\; \\text{remainder} = %d \\\\[6pt]\n",
                    javaQuotient, javaRemainder));
            sb.append(String.format("\\text{Quotient (2's comp)} = \\mathtt{%s} \\\\\n",
                    NumberUtils.toTwosComplement(javaQuotient, op1.getBitLength())));
            sb.append(String.format("\\text{Quotient (original)} = %s\\mathtt{%s} \\quad \\text{(algorithm: } %s\\mathtt{%s}\\text{)} \\\\\n",
                    resultSign, NumberUtils.toTwosComplement(Math.abs(javaQuotient), m),
                    resultSign, quotientBin));
            sb.append(String.format("\\text{Remainder (original)} = 0\\mathtt{%s} \\quad \\text{(algorithm: } 0\\mathtt{%s}\\text{)} \\\\\n",
                    NumberUtils.toTwosComplement(Math.abs(javaRemainder), m), remainderBin));
            sb.append(String.format("\\text{Result (decimal)} = %d \\quad %s\n", javaQuotient,
                    (overflow || !match) ? "\\text{✗}" : "\\text{✓}"));
        } else {
            String algResultBin = resultMap.getOrDefault("resultBinary", resultMap.getOrDefault("resultMagnitude", ""));
            long algResultDec = Long.parseLong(resultMap.get("resultDecimal"));

            boolean isOriginalCode = operation.getOperationName().contains("Original");

            if (fractionMode) {
                sb.append(String.format("X = %.6f = \\mathtt{%s} \\\\\n", op1.getActualDoubleValue(), op1.getTwosComplement()));
                sb.append(String.format("Y = %.6f = \\mathtt{%s} \\\\\n", op2.getActualDoubleValue(), op2.getTwosComplement()));
                double javaResultDouble = op1.getActualDoubleValue() * op2.getActualDoubleValue();
                sb.append(String.format("X \\times Y = %.6f \\\\[6pt]\n", javaResultDouble));
            } else {
                sb.append(String.format("X = %d = \\mathtt{%s} \\\\\n", op1.getDecimalValue(), op1.getTwosComplement()));
                sb.append(String.format("Y = %d = \\mathtt{%s} \\\\\n", op2.getDecimalValue(), op2.getTwosComplement()));
                long javaProduct = op1.getDecimalValue() * op2.getDecimalValue();
                sb.append(String.format("X \\times Y = %d \\\\[6pt]\n", javaProduct));
            }

            if (isOriginalCode) {
                char signX = op1.getTwosComplement().charAt(0);
                char signY = op2.getTwosComplement().charAt(0);
                char resultSign = (char) (((signX - '0') ^ (signY - '0')) + '0');
                int m = op1.getBitLength() - 1;
                long javaMagProduct = Math.abs(op1.getDecimalValue()) * Math.abs(op2.getDecimalValue());
                String javaResultMagnitude = NumberUtils.toTwosComplement(javaMagProduct, 2 * m);
                sb.append(String.format("\\text{Result (original)} = %s\\mathtt{%s} \\quad \\text{(algorithm: } %s\\mathtt{%s}\\text{)} \\\\\n",
                        resultSign, javaResultMagnitude, resultSign, algResultBin));
            } else {
                String javaResultBinary = NumberUtils.toTwosComplement(
                        op1.getDecimalValue() * op2.getDecimalValue(), op1.getBitLength() * 2);
                sb.append(String.format("\\text{Result (2's comp)} = \\mathtt{%s} \\quad \\text{(algorithm: } \\mathtt{%s}\\text{)} \\\\\n",
                        javaResultBinary, algResultBin));
            }

            long javaExpected = op1.getDecimalValue() * op2.getDecimalValue();
            boolean match;
            if (isOriginalCode) {
                match = (algResultDec == Math.abs(javaExpected));
            } else {
                match = (NumberUtils.twosComplementToLong(algResultBin) == javaExpected);
            }

            if (fractionMode) {
                double resultDouble = op1.getActualDoubleValue() * op2.getActualDoubleValue();
                sb.append(String.format("\\text{Result (decimal)} = %.6f \\quad %s\n", resultDouble,
                        (overflow || !match) ? "\\text{✗}" : "\\text{✓}"));
            } else {
                sb.append(String.format("\\text{Result (decimal)} = %d \\quad %s\n", javaExpected,
                        (overflow || !match) ? "\\text{✗}" : "\\text{✓}"));
            }
        }

        sb.append("\\end{array}\n");
        sb.append("$$\n");

        String content = sb.toString();

        try (PrintWriter pw = new PrintWriter(new FileWriter(outputPath, StandardCharsets.UTF_8))) {
            pw.print(content);
            System.out.println("Report generated: " + outputPath);
        } catch (IOException e) {
            System.err.println("File write failed, output to console:");
            System.out.println(content);
        }
    }

    private static String escapeMd(String s) {
        if (s == null) return "-";
        return s.replace("|", "\\|");
    }
}
