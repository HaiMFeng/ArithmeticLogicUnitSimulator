package io.github.haimfeng.simulator;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.model.StepRecord;
import io.github.haimfeng.simulator.operation.ArithmeticOperation;
import io.github.haimfeng.simulator.operation.BoothMultiplier;
import io.github.haimfeng.simulator.operation.OriginalOneMultiplier;
import io.github.haimfeng.simulator.operation.OriginalTwoMultiplier;
import io.github.haimfeng.simulator.operation.RestoreDivision;
import io.github.haimfeng.simulator.operation.NonRestoreDivision;

import java.util.List;

public class ALUSimulator {

    public static List<StepRecord> execute(String algorithmType, Operand op1, Operand op2) {
        ArithmeticOperation operation = getOperation(algorithmType);
        return operation.execute(op1, op2);
    }

    public static ArithmeticOperation getOperation(String algorithmType) {
        return switch (algorithmType.toLowerCase().trim()) {
            case "1", "booth" -> new BoothMultiplier();
            case "2", "original1" -> new OriginalOneMultiplier();
            case "3", "original2" -> new OriginalTwoMultiplier();
            case "4", "restore" -> new RestoreDivision();
            case "5", "nonrestore" -> new NonRestoreDivision();
            default -> throw new IllegalArgumentException("不支持的算法类型: " + algorithmType);
        };
    }

    public static String getOperationName(String algorithmType) {
        return getOperation(algorithmType).getOperationName();
    }

    public static boolean checkOverflow(String algorithmType, Operand op1, Operand op2, String result) {
        return getOperation(algorithmType).checkOverflow(op1, op2, result);
    }
}
