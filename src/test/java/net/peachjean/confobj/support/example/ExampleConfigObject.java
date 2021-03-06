package net.peachjean.confobj.support.example;

import net.peachjean.confobj.ConfigObject;

import javax.validation.constraints.Max;

@ConfigObject
public interface ExampleConfigObject {
    String getValue1();

    String getValue2();

    @Max(100)
    Integer getIntValue();

    public static abstract class Defaults implements ExampleConfigObject {
        public String getValue2() {
            return "secondValue";
        }

        public Integer getIntValue() {
            return 55;
        }
    }
}
