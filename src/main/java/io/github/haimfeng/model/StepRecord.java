package io.github.haimfeng.model;

public class StepRecord {
    private String stepName;
    private String partialProduct;
    private String yAndYMinus1;
    private String quotient;
    private String remainder;
    private String description;

    public StepRecord(String stepName, String partialProduct, String yAndYMinus1, String description) {
        this.stepName = stepName;
        this.partialProduct = partialProduct;
        this.yAndYMinus1 = yAndYMinus1;
        this.description = description;
    }

    public StepRecord(String stepName, String quotient, String remainder, String description, boolean isDivision) {
        this.stepName = stepName;
        this.quotient = quotient;
        this.remainder = remainder;
        this.description = description;
    }

    public StepRecord(String stepName, String description) {
        this.stepName = stepName;
        this.description = description;
    }

    public String getStepName() { return stepName; }
    public String getPartialProduct() { return partialProduct; }
    public String getYAndYMinus1() { return yAndYMinus1; }
    public String getQuotient() { return quotient; }
    public String getRemainder() { return remainder; }
    public String getDescription() { return description; }
}
