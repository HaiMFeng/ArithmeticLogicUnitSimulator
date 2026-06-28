package io.github.haimfeng.model;

public class Operand {
    private String originalInput;
    private long decimalValue;
    private int radix;
    private boolean isNegative;
    private int bitLength;
    private String twosComplement;
    private double actualDoubleValue;
    private boolean fractionMode;

    public Operand(String originalInput, long decimalValue, int radix, boolean isNegative, int bitLength, String twosComplement) {
        this(originalInput, decimalValue, radix, isNegative, bitLength, twosComplement, decimalValue, false);
    }

    public Operand(String originalInput, long decimalValue, int radix, boolean isNegative,
                   int bitLength, String twosComplement, double actualDoubleValue, boolean fractionMode) {
        this.originalInput = originalInput;
        this.decimalValue = decimalValue;
        this.radix = radix;
        this.isNegative = isNegative;
        this.bitLength = bitLength;
        this.twosComplement = twosComplement;
        this.actualDoubleValue = actualDoubleValue;
        this.fractionMode = fractionMode;
    }

    public String getOriginalInput() { return originalInput; }
    public long getDecimalValue() { return decimalValue; }
    public int getRadix() { return radix; }
    public boolean isNegative() { return isNegative; }
    public int getBitLength() { return bitLength; }
    public String getTwosComplement() { return twosComplement; }
    public double getActualDoubleValue() { return actualDoubleValue; }
    public boolean isFractionMode() { return fractionMode; }
}
