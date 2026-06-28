package io.github.haimfeng.utils;

public class ValidationUtils {

    public static boolean isValidBitLength(String input) {
        try {
            int val = Integer.parseInt(input.trim());
            return val > 0 && val <= 64;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidNumber(String input, int bitLength) {
        try {
            parseNumberValue(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFractionInput(String input) {
        String s = stripSignAndPrefix(input);
        return s.contains(".");
    }

    public static long parseNumberValue(String input) {
        String s = input.trim().toLowerCase();
        boolean negative = false;
        if (s.startsWith("-")) {
            negative = true;
            s = s.substring(1);
        } else if (s.startsWith("+")) {
            s = s.substring(1);
        }

        long value;
        if (s.startsWith("0x")) {
            value = Long.parseLong(s.substring(2), 16);
        } else if (s.startsWith("0o")) {
            value = Long.parseLong(s.substring(2), 8);
        } else if (s.startsWith("0b")) {
            value = Long.parseLong(s.substring(2), 2);
        } else if (s.startsWith("0d")) {
            value = Long.parseLong(s.substring(2), 10);
        } else {
            value = Long.parseLong(s, 10);
        }

        return negative ? -value : value;
    }

    public static long parseFractionScaledValue(String input, int bitLength) {
        String s = input.trim().toLowerCase();
        boolean negative = false;
        if (s.startsWith("-")) {
            negative = true;
            s = s.substring(1);
        } else if (s.startsWith("+")) {
            s = s.substring(1);
        }

        long scaledValue;
        if (s.startsWith("0b")) {
            String bits = s.substring(2);
            String bitPattern = bits.replace(".", "");
            if (bitPattern.length() > bitLength) {
                throw new NumberFormatException("Binary fraction bit pattern exceeds register length");
            }
            String padded = bitPattern + "0".repeat(bitLength - bitPattern.length());
            scaledValue = NumberUtils.twosComplementToLong(padded);
        } else {
            String numPart = s;
            if (numPart.startsWith("0d")) {
                numPart = numPart.substring(2);
            }
            double doubleVal = Double.parseDouble(numPart);
            double scaled = doubleVal * (1L << (bitLength - 1));
            scaledValue = Math.round(scaled);
        }

        return negative ? -scaledValue : scaledValue;
    }

    public static double parseFractionDoubleValue(String input) {
        String s = input.trim().toLowerCase();
        boolean negative = false;
        if (s.startsWith("-")) {
            negative = true;
            s = s.substring(1);
        } else if (s.startsWith("+")) {
            s = s.substring(1);
        }

        double value;
        if (s.startsWith("0b")) {
            String bits = s.substring(2);
            String bitPattern = bits.replace(".", "");
            int dotPos = bits.indexOf('.');
            int totalBits = dotPos >= 0 ? bits.length() - 1 : bits.length();
            String padded = bitPattern + "0".repeat(totalBits - bitPattern.length());
            long scaledValue = NumberUtils.twosComplementToLong(padded);
            value = (double) scaledValue / (1L << (totalBits - 1));
        } else {
            String numPart = s;
            if (numPart.startsWith("0d")) {
                numPart = numPart.substring(2);
            }
            value = Double.parseDouble(numPart);
            value = negative ? -value : value;
        }

        return value;
    }

    public static int detectRadix(String input) {
        String s = stripSignAndPrefix(input);
        String raw = input.trim().toLowerCase();
        if (raw.startsWith("-") || raw.startsWith("+")) raw = raw.substring(1);
        if (raw.startsWith("0x")) return 16;
        if (raw.startsWith("0o")) return 8;
        if (raw.startsWith("0b")) return 2;
        if (raw.startsWith("0d")) return 10;
        return 10;
    }

    public static boolean isInRange(long value, int bitLength) {
        long min = -(1L << (bitLength - 1));
        long max = (1L << (bitLength - 1)) - 1;
        return value >= min && value <= max;
    }

    public static boolean isFractionInRange(long scaledValue, int bitLength) {
        long min = -(1L << (bitLength - 1));
        long max = (1L << (bitLength - 1)) - 1;
        return scaledValue >= min && scaledValue <= max;
    }

    private static String stripSignAndPrefix(String input) {
        String s = input.trim().toLowerCase();
        if (s.startsWith("-") || s.startsWith("+")) s = s.substring(1);
        if (s.startsWith("0x")) s = s.substring(2);
        else if (s.startsWith("0o")) s = s.substring(2);
        else if (s.startsWith("0b")) s = s.substring(2);
        else if (s.startsWith("0d")) s = s.substring(2);
        return s;
    }
}
