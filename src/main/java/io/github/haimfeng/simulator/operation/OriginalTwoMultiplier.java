package io.github.haimfeng.simulator.operation;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.model.StepRecord;
import io.github.haimfeng.utils.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OriginalTwoMultiplier implements ArithmeticOperation {

    private final Map<String, String> resultMap = new HashMap<>();

    @Override
    public List<StepRecord> execute(Operand op1, Operand op2) {
        List<StepRecord> steps = new ArrayList<>();
        resultMap.clear();
        int n = op1.getBitLength();
        int m = n - 1;

        char signX = op1.getTwosComplement().charAt(0);
        char signY = op2.getTwosComplement().charAt(0);
        char resultSign = (char) (((signX - '0') ^ (signY - '0')) + '0');

        String absX = getMagnitude(op1.getTwosComplement());
        String absY = getMagnitude(op2.getTwosComplement());

        absX = NumberUtils.padLeftZero(absX, m);
        absY = NumberUtils.padLeftZero(absY, m);

        String xDec = op1.isFractionMode()
                ? String.format("%.6f", Math.abs(op1.getActualDoubleValue()))
                : String.valueOf(Math.abs(op1.getDecimalValue()));
        String yDec = op2.isFractionMode()
                ? String.format("%.6f", Math.abs(op2.getActualDoubleValue()))
                : String.valueOf(Math.abs(op2.getDecimalValue()));

        steps.add(new StepRecord("Proc",
                "$\\text{Sign} = " + signX + " \\oplus " + signY + " = " + resultSign + "$"));
        steps.add(new StepRecord("Proc",
                "$|X|\\!: \\mathtt{" + op1.getTwosComplement() + "} \\to " + xDec + " \\to \\mathtt{" + absX + "}$"));
        steps.add(new StepRecord("Proc",
                "$|Y|\\!: \\mathtt{" + op2.getTwosComplement() + "} \\to " + yDec + " \\to \\mathtt{" + absY + "}$"));

        int valueLength = m;
        if (m % 2 != 0) {
            absY = "0" + absY;
            valueLength = m + 1;
        }
        int numSteps = valueLength / 2;

        String a = "0".repeat(m);
        String q = absY;
        char c = '0';

        steps.add(new StepRecord("INIT", a + " " + q, c + " " + q.substring(q.length() - 2),
                "$\\text{Init: } A=0,\\; Q=|Y|,\\; C=0$"));

        for (int i = 1; i <= numSteps; i++) {
            int qLen = q.length();
            char qn1 = q.charAt(qLen - 2);
            char qn = q.charAt(qLen - 1);
            String operation;
            String stepDesc;
            char newC = '0';

            int combo = (c - '0') * 4 + (qn1 - '0') * 2 + (qn - '0');
            switch (combo) {
                case 0:
                    operation = "none";
                    stepDesc = "$\\texttt{>>>}2$";
                    break;
                case 1:
                    operation = "+X";
                    stepDesc = "$A = A + |X|;\\; \\texttt{>>>}2$";
                    break;
                case 2:
                    operation = "+X";
                    stepDesc = "$A = A + |X|;\\; \\texttt{>>>}2$";
                    break;
                case 3:
                    operation = "+2X";
                    stepDesc = "$A = A + 2|X|;\\; \\texttt{>>>}2$";
                    break;
                case 4:
                    operation = "-2X";
                    newC = '1';
                    stepDesc = "$A = A - 2|X|;\\; \\texttt{>>>}2$";
                    break;
                case 5:
                    operation = "-X";
                    newC = '1';
                    stepDesc = "$A = A - |X|;\\; \\texttt{>>>}2$";
                    break;
                case 6:
                    operation = "-X";
                    newC = '1';
                    stepDesc = "$A = A - |X|;\\; \\texttt{>>>}2$";
                    break;
                default:
                    operation = "none";
                    stepDesc = "$\\texttt{>>>}2$";
                    break;
            }

            switch (operation) {
                case "+X":
                    a = NumberUtils.addBinary(NumberUtils.padLeftZero(a, m), absX);
                    if (a.length() > m) a = a.substring(a.length() - m);
                    break;
                case "+2X": {
                    String twoX = absX.substring(1) + "0";
                    a = NumberUtils.addBinary(NumberUtils.padLeftZero(a, m), twoX);
                    if (a.length() > m) a = a.substring(a.length() - m);
                    break;
                }
                case "-X": {
                    String negX = NumberUtils.negate(absX);
                    a = NumberUtils.addBinary(NumberUtils.padLeft(a, m), NumberUtils.padLeft(negX, m));
                    if (a.length() > m) a = a.substring(a.length() - m);
                    break;
                }
                case "-2X": {
                    String twoX = absX.substring(1) + "0";
                    String negTwoX = NumberUtils.negate(twoX);
                    a = NumberUtils.addBinary(NumberUtils.padLeft(a, m + 1), NumberUtils.padLeft(negTwoX, m + 1));
                    if (a.length() > m) a = a.substring(a.length() - m);
                    break;
                }
            }

            c = newC;

            steps.add(new StepRecord("Step " + i, a + " " + q,
                    c + " " + q.substring(q.length() - 2), stepDesc));

            String combined = a + q;
            combined = NumberUtils.arithmeticShiftRight(combined, 2);
            a = combined.substring(0, m);
            q = combined.substring(m);
        }

        String resultMagnitude = a + q;
        resultMap.put("resultMagnitude", resultMagnitude);
        resultMap.put("resultDecimal", String.valueOf(Long.parseLong(resultMagnitude, 2)));

        steps.add(new StepRecord("Result",
                "$\\text{Sign}=" + resultSign + ",\\; |\\text{Result}|=\\mathtt{" + resultMagnitude
                        + "},\\; \\text{Original}=" + resultSign + "\\mathtt{" + resultMagnitude + "}$"));

        return steps;
    }

    private String getMagnitude(String twosComplement) {
        if (twosComplement.charAt(0) == '0') {
            return twosComplement.substring(1);
        }
        String magnitude = twosComplement.substring(1);
        return NumberUtils.negate(magnitude);
    }

    @Override
    public String getOperationName() {
        return "Original-Code 2-Bit Multiplication";
    }

    @Override
    public String[] getColumnHeaders() {
        return new String[]{"Step", "A Q", "C Qₙ₋₁Qₙ", "Info."};
    }

    @Override
    public Map<String, String> getResultMap() {
        return resultMap;
    }

    @Override
    public boolean checkOverflow(Operand op1, Operand op2, String result) {
        long product = op1.getDecimalValue() * op2.getDecimalValue();
        int doubleBitLength = op1.getBitLength() * 2;
        long min = -(1L << (doubleBitLength - 1));
        long max = (1L << (doubleBitLength - 1)) - 1;
        return product < min || product > max;
    }
}
