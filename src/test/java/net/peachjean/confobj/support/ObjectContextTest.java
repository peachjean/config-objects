package net.peachjean.confobj.support;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ObjectContextTest {
    @Test
    public void test() {
        Object myObj = new Object() {
            public String getMyString() {
                return "string";
            }

            public List<String> getMyStrings() {
                return Arrays.asList("first", "second");
            }

            public List<Integer> getMyInts() {
                return Arrays.asList(42, 88);
            }
        };

        InstantiationContext underTest = new ObjectContext(myObj);
        List<String> stringList = underTest.lookup(new GenericType<List<String>>(List.class, new GenericType<String>(String.class)));
        assertEquals(Arrays.asList("first", "second"), stringList);
    }
}
