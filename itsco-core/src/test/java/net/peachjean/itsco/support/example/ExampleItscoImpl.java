package net.peachjean.itsco.support.example;

import com.google.common.base.Objects;
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

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue1(), getValue2(), getIntValue());
    }

    @Override
    public boolean equals(final Object obj) {
        if(!(obj instanceof ExampleItsco))
        {
            return false;
        }
        ExampleItsco other = (ExampleItsco) obj;
        if(!Objects.equal(this.getValue1(), other.getValue1()))
        {
            return false;
        }
        if(!Objects.equal(this.getValue2(), other.getValue2()))
        {
            return false;
        }
        if(!Objects.equal(this.getIntValue(), other.getIntValue()))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(ExampleItsco.class)
                .add("intValue", this.getIntValue())
                .add("value1", this.getValue1())
                .add("value2", this.getValue2())
                .toString();
    }
}
