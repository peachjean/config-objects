package net.peachjean.itsco.support.example;

import com.google.common.base.Objects;
import net.peachjean.itsco.support.ItscoBacker;

public class ExampleItscoImpl extends ExampleItsco.Defaults implements ExampleItsco {

    private final ItscoBacker<ExampleItsco> backer;

    private String value1;
    private String value2;
    private Integer intValue;

    public ExampleItscoImpl(final ItscoBacker<ExampleItsco> backer) {
        this.backer = backer;
        backer.setContaining(this);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (! (o instanceof ExampleItsco)) return false;

        ExampleItsco that = (ExampleItsco) o;

        if (getIntValue() != null ? !getIntValue().equals(that.getIntValue()) : that.getIntValue() != null) return false;
        if (getValue1() != null ? !getValue1().equals(that.getValue1()) : that.getValue1() != null) return false;
        if (getValue2() != null ? !getValue2().equals(that.getValue2()) : that.getValue2() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (getValue1() != null ? getValue1().hashCode() : 0);
        result = 31 * result + (getValue2() != null ? getValue2().hashCode() : 0);
        result = 31 * result + (getIntValue() != null ? getIntValue().hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ExampleItsco");
        sb.append("{");
        sb.append("intValue=").append(getIntValue());
        sb.append(", ").append("value1=").append(getValue1());
        sb.append(", ").append("value2=").append(getValue2());
        sb.append("}");
        return sb.toString();
    }
}
