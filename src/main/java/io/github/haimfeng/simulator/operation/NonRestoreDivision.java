package io.github.haimfeng.simulator.operation;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.model.StepRecord;
import io.github.haimfeng.utils.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonRestoreDivision implements ArithmeticOperation {

    private final Map<String, String> resultMap = new HashMap<>();

    @Override
    public List<StepRecord> execute(Operand op1, Operand op2) {
        List<StepRecord> steps = new ArrayList<>();
        resultMap.clear();
        int n = op1.getBitLength();
        int m = n - 1;

        if (op2.getDecimalValue() == 0) {
            throw new ArithmeticException("Division by zero");
        }

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

        String a = "0".repeat(m);
        String q = absX;
        String M = absY;
        String negM = NumberUtils.negate(M);

        steps.add(new StepRecord("INIT", a + " " + q, "-",
                "$\\text{Init: } A=0,\\; Q=|X|,\\; M=|Y|$"));

        for (int i = 1; i <= m; i++) {
            String combined = a + q;
            combined = combined.substring(1) + "0";
            a = combined.substring(0, m);
            q = combined.substring(m);

            String trialA;
            char qi;
            String stepDesc;

            if (i == 1) {
                trialA = NumberUtils.addBinary(a, negM);
                if (trialA.charAt(0) == '0') {
                    a = trialA;
                    qi = '1';
                    q = q.substring(0, q.length() - 1) + "1";
                    stepDesc = "$\\texttt{<<}1;\\; A'=A-M=\\mathtt{" + trialA + "}\\geq 0;\\; q_{" + i + "}=1$";
                } else {
                    qi = '0';
                    q = q.substring(0, q.length() - 1) + "0";
                    stepDesc = "$\\texttt{<<}1;\\; A'=A-M=\\mathtt{" + trialA + "}<0;\\; q_{" + i + "}=0$";
                }
            } else {
                char prevSign = a.charAt(0);
                if (prevSign == '0') {
                    trialA = NumberUtils.addBinary(a, negM);
                    if (trialA.charAt(0) == '0') {
                        a = trialA;
                        qi = '1';
                        q = q.substring(0, q.length() - 1) + "1";
                        stepDesc = "$A\\geq 0\\Rightarrow -M;\\; \\texttt{<<}1;\\; A'=\\mathtt{" + trialA + "}\\geq 0;\\; q_{" + i + "}=1$";
                    } else {
                        qi = '0';
                        q = q.substring(0, q.length() - 1) + "0";
                        stepDesc = "$A\\geq 0\\Rightarrow -M;\\; \\texttt{<<}1;\\; A'=\\mathtt{" + trialA + "}<0;\\; q_{" + i + "}=0$";
                    }
                } else {
                    trialA = NumberUtils.addBinary(a, M);
                    if (trialA.charAt(0) == '0') {
                        a = trialA;
                        qi = '1';
                        q = q.substring(0, q.length() - 1) + "1";
                        stepDesc = "$A<0\\Rightarrow +M;\\; \\texttt{<<}1;\\; A'=\\mathtt{" + trialA + "}\\geq 0;\\; q_{" + i + "}=1$";
                    } else {
                        qi = '0';
                        q = q.substring(0, q.length() - 1) + "0";
                        stepDesc = "$A<0\\Rightarrow +M;\\; \\texttt{<<}1;\\; A'=\\mathtt{" + trialA + "}<0;\\; q_{" + i + "}=0$";
                    }
                }
            }

            steps.add(new StepRecord("Step " + i, a + " " + q, String.valueOf(qi), stepDesc));
        }

        if (a.charAt(0) == '1') {
            String correctedA = NumberUtils.addBinary(a, M);
            steps.add(new StepRecord("Corr", correctedA + " " + q, "-",
                    "$A<0;\\; \\text{restore: } A=A+M=\\mathtt{" + correctedA + "}$"));
            a = correctedA;
        }

        resultMap.put("quotientBinary", q);
        resultMap.put("remainderBinary", a);
        resultMap.put("quotientDecimal", String.valueOf(Long.parseLong(q, 2)));
        resultMap.put("remainderDecimal", String.valueOf(Long.parseLong(a, 2)));

        steps.add(new StepRecord("Result",
                "$\\text{Sign}=" + resultSign + ",\\; |Q|=\\mathtt{" + q + "},\\; |R|=\\mathtt{" + a
                        + "},\\; Q=" + resultSign + "\\mathtt{" + q + "},\\; R=0\\mathtt{" + a + "}$"));

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
        return "Non-Restoring Division";
    }

    @Override
    public String[] getColumnHeaders() {
        return new String[]{"Step", "A Q", "qᵢ", "Info."};
    }

    @Override
    public boolean isDivision() {
        return true;
    }

    @Override
    public Map<String, String> getResultMap() {
        return resultMap;
    }

    @Override
    public boolean checkOverflow(Operand op1, Operand op2, String result) {
        if (op2.getDecimalValue() == 0) return true;
        long quotient = op1.getDecimalValue() / op2.getDecimalValue();
        int bitLength = op1.getBitLength();
        long min = -(1L << (bitLength - 1));
        long max = (1L << (bitLength - 1)) - 1;
        return quotient < min || quotient > max;
    }
}
