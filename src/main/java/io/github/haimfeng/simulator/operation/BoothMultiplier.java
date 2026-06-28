package io.github.haimfeng.simulator.operation;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.model.StepRecord;
import io.github.haimfeng.utils.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoothMultiplier implements ArithmeticOperation {

    private final Map<String, String> resultMap = new HashMap<>();

    @Override
    public List<StepRecord> execute(Operand op1, Operand op2) {
        List<StepRecord> steps = new ArrayList<>();
        resultMap.clear();
        int n = op1.getBitLength();

        String x = op1.getTwosComplement();
        String y = op2.getTwosComplement();
        String negX = NumberUtils.negate(x);

        String a = "0".repeat(n);
        String q = y;
        char yMinus1 = '0';

        steps.add(new StepRecord("INIT", a + " " + q, q + " " + yMinus1,
                "$\\text{Init: } A=0,\\; Q=Y,\\; Y_{-1}=0$"));

        for (int i = 1; i <= n; i++) {
            char yLast = q.charAt(q.length() - 1);
            char oldYMinus1 = yMinus1;

            if (yLast == '0' && yMinus1 == '1') {
                a = NumberUtils.addBinary(NumberUtils.padLeft(a, n), NumberUtils.padLeft(x, n));
                a = a.substring(a.length() - n);
            } else if (yLast == '1' && yMinus1 == '0') {
                a = NumberUtils.addBinary(NumberUtils.padLeft(a, n), NumberUtils.padLeft(negX, n));
                a = a.substring(a.length() - n);
            }

            String combined = a + q;
            char newSign = combined.charAt(0);
            combined = newSign + combined.substring(0, combined.length() - 1);
            a = combined.substring(0, n);
            q = combined.substring(n);
            yMinus1 = yLast;

            String stepDesc;
            if (yLast == '0' && oldYMinus1 == '1') {
                stepDesc = "$A = A + X;\\; \\texttt{>>}1$";
            } else if (yLast == '1' && oldYMinus1 == '0') {
                stepDesc = "$A = A - X;\\; \\texttt{>>}1$";
            } else {
                stepDesc = "$\\texttt{>>}1$";
            }

            steps.add(new StepRecord("Step " + i, a + " " + q, q + " " + yMinus1, stepDesc));
        }

        resultMap.put("resultBinary", a + q);
        resultMap.put("resultDecimal", String.valueOf(NumberUtils.twosComplementToLong(a + q)));

        return steps;
    }

    @Override
    public String getOperationName() {
        return "Booth Multiplication";
    }

    @Override
    public boolean checkOverflow(Operand op1, Operand op2, String result) {
        long product = op1.getDecimalValue() * op2.getDecimalValue();
        int doubleBitLength = op1.getBitLength() * 2;
        long min = -(1L << (doubleBitLength - 1));
        long max = (1L << (doubleBitLength - 1)) - 1;
        return product < min || product > max;
    }

    @Override
    public Map<String, String> getResultMap() {
        return resultMap;
    }
}
