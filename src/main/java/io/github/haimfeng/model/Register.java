package io.github.haimfeng.model;

import io.github.haimfeng.utils.NumberUtils;

public class Register {
    private String value;
    private int bitLength;

    public Register(int bitLength) {
        this.bitLength = bitLength;
        this.value = "0".repeat(bitLength);
    }

    public Register(String value, int bitLength) {
        this.bitLength = bitLength;
        this.value = value;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public int getBitLength() { return bitLength; }

    public void arithmeticShiftRight(int steps) {
        for (int i = 0; i < steps; i++) {
            char signBit = value.charAt(0);
            value = signBit + value.substring(0, value.length() - 1);
        }
    }

    public void add(String bin) {
        long a = NumberUtils.twosComplementToLong(value);
        long b = NumberUtils.twosComplementToLong(bin);
        long result = a + b;
        this.value = NumberUtils.toTwosComplement(result, bitLength);
    }

    public void subtract(String bin) {
        long a = NumberUtils.twosComplementToLong(value);
        long b = NumberUtils.twosComplementToLong(bin);
        long result = a - b;
        this.value = NumberUtils.toTwosComplement(result, bitLength);
    }

    @Override
    public String toString() { return value; }
}
