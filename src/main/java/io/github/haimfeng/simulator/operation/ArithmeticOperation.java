package io.github.haimfeng.simulator.operation;

import io.github.haimfeng.model.Operand;
import io.github.haimfeng.model.StepRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ArithmeticOperation {
    List<StepRecord> execute(Operand op1, Operand op2);
    String getOperationName();
    boolean checkOverflow(Operand op1, Operand op2, String result);

    default boolean isDivision() {
        return false;
    }

    default String[] getColumnHeaders() {
        return new String[]{"Step", "A Q", "Y Y₋₁", "Info."};
    }

    default Map<String, String> getResultMap() {
        return new HashMap<>();
    }
}
