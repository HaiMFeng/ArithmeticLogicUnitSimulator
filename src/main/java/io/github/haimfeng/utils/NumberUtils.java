package io.github.haimfeng.utils;

public class NumberUtils {

    public static String toTwosComplement(long value, int bitLength) {
        if (value >= 0) {
            String bin = Long.toBinaryString(value);
            if (bin.length() > bitLength) {
                return bin.substring(bin.length() - bitLength);
            }
            return "0".repeat(bitLength - bin.length()) + bin;
        } else {
            long mask = (1L << bitLength) - 1;
            long complement = value & mask;
            String bin = Long.toBinaryString(complement);
            if (bin.length() > bitLength) {
                return bin.substring(bin.length() - bitLength);
            }
            return "0".repeat(bitLength - bin.length()) + bin;
        }
    }

    public static long twosComplementToLong(String bin) {
        if (bin.isEmpty()) return 0;
        boolean negative = bin.charAt(0) == '1';
        if (!negative) {
            return Long.parseLong(bin, 2);
        }
        long val = Long.parseLong(bin, 2);
        long mask = 1L << bin.length();
        return val - mask;
    }

    public static String arithmeticShiftRight(String bin, int steps) {
        String result = bin;
        for (int i = 0; i < steps; i++) {
            char signBit = result.charAt(0);
            result = signBit + result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String addBinary(String a, String b) {
        int len = Math.max(a.length(), b.length());
        String pa = padLeft(a, len);
        String pb = padLeft(b, len);
        StringBuilder sb = new StringBuilder();
        int carry = 0;
        for (int i = len - 1; i >= 0; i--) {
            int sum = (pa.charAt(i) - '0') + (pb.charAt(i) - '0') + carry;
            sb.insert(0, sum % 2);
            carry = sum / 2;
        }
        return sb.toString();
    }

    public static String negate(String bin) {
        StringBuilder sb = new StringBuilder();
        for (char c : bin.toCharArray()) {
            sb.append(c == '0' ? '1' : '0');
        }
        String onesComplement = sb.toString();
        String one = "0".repeat(bin.length() - 1) + "1";
        String result = addBinary(onesComplement, one);
        if (result.length() > bin.length()) {
            result = result.substring(result.length() - bin.length());
        }
        return result;
    }

    public static String padLeft(String bin, int length) {
        if (bin.length() >= length) return bin;
        char padChar = bin.charAt(0) == '1' ? '1' : '0';
        return String.valueOf(padChar).repeat(length - bin.length()) + bin;
    }

    public static String padLeftZero(String bin, int length) {
        if (bin.length() >= length) return bin;
        return "0".repeat(length - bin.length()) + bin;
    }

    public static String logicalShiftLeft(String bin, int steps) {
        if (steps >= bin.length()) return "0".repeat(bin.length());
        return bin.substring(steps) + "0".repeat(steps);
    }
}
