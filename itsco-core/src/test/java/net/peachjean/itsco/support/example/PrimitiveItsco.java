package net.peachjean.itsco.support.example;

public interface PrimitiveItsco {

    static final boolean DEFAULT_BOOLEAN = true;
    static final int DEFAULT_BYTE = 0x30;
    static final char DEFAULT_CHAR = 'r';
    static final int DEFAULT_SHORT = 14;
    static final int DEFAULT_INT = 42;
    static final long DEFAULT_LONG = 88;
    static final float DEFAULT_FLOAT = 3.1415926535f;
    static final double DEFAULT_DOUBLE = 2.7182818284;

    boolean getBooleanValue();

    byte getByteValue();

    char getCharValue();

    short getShortValue();

    int getIntValue();

    long getLongValue();

    float getFloatValue();

//    double getDoubleValue();

    boolean getBooleanValue2();

    byte getByteValue2();

    char getCharValue2();

    short getShortValue2();

    int getIntValue2();

    long getLongValue2();

    float getFloatValue2();

//    double getDoubleValue2();

    public static abstract class Defaults implements PrimitiveItsco {

        @Override
        public boolean getBooleanValue2() {
            return DEFAULT_BOOLEAN;
        }

        @Override
        public byte getByteValue2() {
            return DEFAULT_BYTE;
        }

        @Override
        public char getCharValue2() {
            return DEFAULT_CHAR;
        }

        @Override
        public short getShortValue2() {
            return DEFAULT_SHORT;
        }

        @Override
        public int getIntValue2() {
            return DEFAULT_INT;
        }

        @Override
        public long getLongValue2() {
            return DEFAULT_LONG;
        }

        @Override
        public float getFloatValue2() {
            return DEFAULT_FLOAT;
        }

//        @Override
//        public double getDoubleValue2() {
//            return DEFAULT_DOUBLE;
//        }
    }
}
