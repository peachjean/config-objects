package net.peachjean.confobj.support.example;

import net.peachjean.confobj.introspection.GenericType;
import net.peachjean.confobj.support.ConfigObjectBacker;

public class PrimitiveConfigObjectImpl extends PrimitiveConfigObject.Defaults implements PrimitiveConfigObject {
    private final ConfigObjectBacker<PrimitiveConfigObject> backer;

    private boolean booleanValue;
    private byte byteValue;
    private char charValue;
    private short shortValue;
    private int intValue;
    private long longValue;
    private float floatValue;
    private double doubleValue;

    public PrimitiveConfigObjectImpl(ConfigObjectBacker<PrimitiveConfigObject> backer) {
        this.backer = backer;
        backer.setContaining(this);
    }

    //@Override
    public boolean getBooleanValue() {
        return backer.lookup("booleanValue", GenericType.forType(Boolean.class));
    }

    //@Override
    public byte getByteValue() {
        return backer.lookup("byteValue", GenericType.forType(Byte.class));
    }

    //@Override
    public char getCharValue() {
        return backer.lookup("charValue", GenericType.forType(Character.class));
    }

    //@Override
    public short getShortValue() {
        return backer.lookup("shortValue", GenericType.forType(Short.class));
    }

    //@Override
    public int getIntValue() {
        return backer.lookup("intValue", GenericType.forType(Integer.class));
    }

    //@Override
    public long getLongValue() {
        return backer.lookup("longValue", GenericType.forType(Long.class));
    }

    //@Override
    public float getFloatValue() {
        return backer.lookup("floatValue", GenericType.forType(Float.class));
    }

    //@Override
    public double getDoubleValue() {
        return backer.lookup("doubleValue", GenericType.forType(Double.class));
    }

    //@Override
    public boolean getBooleanValue2() {
        return backer.lookup("booleanValue", GenericType.forType(Boolean.class), super.getBooleanValue2());
    }

    //@Override
    public byte getByteValue2() {
        return backer.lookup("byteValue", GenericType.forType(Byte.class), super.getByteValue2());
    }

    //@Override
    public char getCharValue2() {
        return backer.lookup("charValue", GenericType.forType(Character.class), super.getCharValue2());
    }

    //@Override
    public short getShortValue2() {
        return backer.lookup("shortValue", GenericType.forType(Short.class), super.getShortValue2());
    }

    //@Override
    public int getIntValue2() {
        return backer.lookup("intValue", GenericType.forType(Integer.class), super.getIntValue2());
    }

    //@Override
    public long getLongValue2() {
        return backer.lookup("longValue", GenericType.forType(Long.class), super.getLongValue2());
    }

    //@Override
    public float getFloatValue2() {
        return backer.lookup("floatValue", GenericType.forType(Float.class), super.getFloatValue2());
    }

    @Override
    public double getDoubleValue2() {
        return backer.lookup("doubleValue", GenericType.forType(Double.class), super.getDoubleValue2());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveConfigObjectImpl that = (PrimitiveConfigObjectImpl) o;

        if (getBooleanValue() != that.getBooleanValue()) return false;
        if (getByteValue() != that.getByteValue()) return false;
        if (getCharValue() != that.getCharValue()) return false;
        if (Double.compare(that.getDoubleValue(), getDoubleValue()) != 0) return false;
        if (Float.compare(that.getFloatValue(), getFloatValue()) != 0) return false;
        if (getIntValue() != that.getIntValue()) return false;
        if (getLongValue() != that.getLongValue()) return false;
        if (getShortValue() != that.getShortValue()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (this.getBooleanValue() ? 1 : 0);
        result = 31 * result + (int) getByteValue();
        result = 31 * result + (int) getCharValue();
        result = 31 * result + (int) getShortValue();
        result = 31 * result + getIntValue();
        result = 31 * result + (int) (getLongValue() ^ (getLongValue() >>> 32));
        result = 31 * result + (getFloatValue() != +0.0f ? Float.floatToIntBits(getFloatValue()) : 0);
        temp = getDoubleValue() != +0.0d ? Double.doubleToLongBits(getDoubleValue()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PrimitiveConfigObject");
        sb.append("{");
        sb.append("booleanValue=").append(getBooleanValue());
        sb.append(", ").append("byteValue=").append(getByteValue());
        sb.append(", ").append("charValue=").append(getCharValue());
        sb.append(", ").append("shortValue=").append(getShortValue());
        sb.append(", ").append("intValue=").append(getIntValue());
        sb.append(", ").append("longValue=").append(getLongValue());
        sb.append(", ").append("floatValue=").append(getFloatValue());
        sb.append(", ").append("doubleValue=").append(getDoubleValue());
        sb.append("}");
        return sb.toString();
    }
}
