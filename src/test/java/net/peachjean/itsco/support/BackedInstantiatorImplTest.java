package net.peachjean.itsco.support;

import net.peachjean.tater.utils.AnnotationInvocationHandler;
import org.easymock.EasyMock;
import org.junit.Test;

import javax.inject.Named;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BackedInstantiatorImplTest {
    @Test
    public void testInstantiate() throws Exception {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);


        EasyMock.replay(mockBacker);
        BackedInstantiator<Simple> underTest = new BackedInstantiatorImpl<Simple>(Simple.class, Simple.class);

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
        BackedInstantiator<Complex> underTest = new BackedInstantiatorImpl<Complex>(Complex.class, Complex.class);

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

    @Test
    public void testAnnotated() {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);
        InstantiationContext mockContext = EasyMock.createMock(InstantiationContext.class);
        Named first = AnnotationInvocationHandler.implement(Named.class).withMemberValue("value", "first").build();
        EasyMock.expect(mockContext.lookup(GenericType.forType(String.class), first)).andReturn("stringNumberOne");
        Named second = AnnotationInvocationHandler.implement(Named.class).withMemberValue("value", "second").build();
        EasyMock.expect(mockContext.lookup(GenericType.forType(String.class), second)).andReturn("stringNumberTwo");

        EasyMock.replay(mockBacker, mockContext);
        BackedInstantiator<Annotated> underTest = new BackedInstantiatorImpl<Annotated>(Annotated.class, Annotated.class);

        Annotated value = underTest.instantiate(mockBacker, mockContext);
        assertEquals("stringNumberOne", value.getFirst());
        assertEquals("stringNumberTwo", value.getSecond());

        EasyMock.verify(mockBacker);
    }

    public static class Annotated {
        private final String first;
        private final String second;

        public Annotated(ItscoBacker backer, @Named("first") String first, @Named("second") String second) {

            this.first = first;
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNoArgConstructor() {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);
        InstantiationContext mockContext = EasyMock.createMock(InstantiationContext.class);

        EasyMock.replay(mockBacker, mockContext);
        BackedInstantiator<NoArgConstructor> underTest = new BackedInstantiatorImpl<NoArgConstructor>(NoArgConstructor.class, NoArgConstructor.class);

        NoArgConstructor value = underTest.instantiate(mockBacker, mockContext);

        EasyMock.verify(mockBacker);
    }

    public static class NoArgConstructor {

    }

    @Test(expected = IllegalStateException.class)
    public void testNoBackerInConstructor() {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);
        InstantiationContext mockContext = EasyMock.createMock(InstantiationContext.class);

        EasyMock.replay(mockBacker, mockContext);
        BackedInstantiator<NoBackerInConstructor> underTest = new BackedInstantiatorImpl<NoBackerInConstructor>(NoBackerInConstructor.class, NoBackerInConstructor.class);

        NoBackerInConstructor value = underTest.instantiate(mockBacker, mockContext);

        EasyMock.verify(mockBacker);

    }

    public static class NoBackerInConstructor {
        public NoBackerInConstructor(String str, Integer intVal) {

        }
    }
}
