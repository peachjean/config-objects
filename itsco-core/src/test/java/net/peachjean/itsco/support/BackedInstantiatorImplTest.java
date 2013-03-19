package net.peachjean.itsco.support;

import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BackedInstantiatorImplTest {
    @Test
    public void testInstantiate() throws Exception {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);


        EasyMock.replay(mockBacker);
        BackedInstantiator<Simple> underTest = new BackedInstantiatorImpl<Simple>(Simple.class);

        Simple first = underTest.instantiate(mockBacker);
        assertSame(mockBacker, first.getBacker());

        EasyMock.verify(mockBacker);
    }

    public static class Simple {
        private final ItscoBacker backer;

        public Simple(ItscoBacker backer) {

            this.backer = backer;
        }

        public ItscoBacker getBacker() {
            return backer;
        }
    }

    @Test
    public void testInstantiateComplex() {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);
        InstantiationContext mockContext = EasyMock.createMock(InstantiationContext.class);
        EasyMock.expect(mockContext.lookup(GenericType.forType(String.class))).andReturn("strDep");
        EasyMock.expect(mockContext.lookup(GenericType.forType(Integer.class))).andReturn(42);

        EasyMock.replay(mockBacker, mockContext);
        BackedInstantiator<Complex> underTest = new BackedInstantiatorImpl<Complex>(Complex.class);

        Complex first = underTest.instantiate(mockBacker, mockContext);
        assertEquals("strDep", first.getStrDep());
        assertEquals(42, first.getIntDep().intValue());

        EasyMock.verify(mockBacker);
    }

    public static class Complex {
        private final String strDep;
        private final Integer intDep;

        public Complex(ItscoBacker backer, String strDep, Integer intDep) {
            this.strDep = strDep;
            this.intDep = intDep;
        }

        public String getStrDep() {
            return strDep;
        }

        public Integer getIntDep() {
            return intDep;
        }
    }
}
