package net.peachjean.itsco.support.example;

import net.peachjean.itsco.Itsco;

@Itsco
public interface ExampleItsco {
    String getValue1();

    String getValue2();

    Integer getIntValue();

    public static abstract class Defaults implements ExampleItsco
    {
        public String getValue2() {
            return "secondValue";
        }

        public Integer getIntValue() {
            return 55;
        }
    }
}
