package net.peachjean.itsco.support.example;

import net.peachjean.itsco.Itsco;

import javax.validation.constraints.Max;

@Itsco
public interface ExampleItsco {
    String getValue1();

    String getValue2();

    @Max(100)
    Integer getIntValue();

    public static abstract class Defaults implements ExampleItsco {
        public String getValue2() {
            return "secondValue";
        }

        public Integer getIntValue() {
            return 55;
        }
    }
}
