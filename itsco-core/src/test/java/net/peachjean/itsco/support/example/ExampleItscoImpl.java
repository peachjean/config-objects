package net.peachjean.itsco.support.example;

import net.peachjean.itsco.support.ItscoBacker;

public class ExampleItscoImpl extends ExampleItsco.Defaults implements ExampleItsco  {

    private final ItscoBacker backer;

    public ExampleItscoImpl(final ItscoBacker backer) {
        this.backer = backer;
    }

    public String getValue1() {
        return backer.lookup("value1", String.class);
    }

    @Override
    public String getValue2() {
        return backer.lookup("value2", String.class, super.getValue2());
    }

    @Override
    public Integer getIntValue() {
        return backer.lookup("intValue", Integer.class, super.getIntValue());
    }
}
