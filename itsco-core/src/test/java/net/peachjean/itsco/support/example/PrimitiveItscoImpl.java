package net.peachjean.itsco.support.example;

import net.peachjean.itsco.support.ItscoBacker;

public class PrimitiveItscoImpl extends PrimitiveItsco.Defaults implements PrimitiveItsco {
    private final ItscoBacker<PrimitiveItsco> backer;

    public PrimitiveItscoImpl(ItscoBacker<PrimitiveItsco> backer) {
        this.backer = backer;
        backer.setContaining(this);
    }

    @Override
    public boolean getBooleanValue() {
        return backer.lookup("booleanValue", Boolean.class);
    }

    @Override
    public byte getByteValue() {
        return backer.lookup("byteValue", Byte.class);
    }

    @Override
    public char getCharValue() {
        return backer.lookup("charValue", Character.class);
    }

    @Override
    public short getShortValue() {
        return backer.lookup("shortValue", Short.class);
    }

    @Override
    public int getIntValue() {
        return backer.lookup("intValue", Integer.class);
    }

    @Override
    public long getLongValue() {
        return backer.lookup("longValue", Long.class);
    }

    @Override
    public float getFloatValue() {
        return backer.lookup("floatValue", Float.class);
    }

    @Override
    public double getDoubleValue() {
        return backer.lookup("doubleValue", Double.class);
    }

    @Override
    public boolean getBooleanValue2() {
        return backer.lookup("booleanValue", Boolean.class, super.getBooleanValue2());
    }

    @Override
    public byte getByteValue2() {
        return backer.lookup("byteValue", Byte.class, super.getByteValue2());
    }

    @Override
    public char getCharValue2() {
        return backer.lookup("charValue", Character.class, super.getCharValue2());
    }

    @Override
    public short getShortValue2() {
        return backer.lookup("shortValue", Short.class, super.getShortValue2());
    }

    @Override
    public int getIntValue2() {
        return backer.lookup("intValue", Integer.class, super.getIntValue2());
    }

    @Override
    public long getLongValue2() {
        return backer.lookup("longValue", Long.class, super.getLongValue2());
    }

    @Override
    public float getFloatValue2() {
        return backer.lookup("floatValue", Float.class, super.getFloatValue2());
    }

    @Override
    public double getDoubleValue2() {
        return backer.lookup("doubleValue", Double.class, super.getDoubleValue2());
    }

}
