package net.peachjean.confobj.support.example;

public interface CompoundItsco {
    ExampleItsco getSubItsco();

    String getMyString();

    Float getMyFloat();

    public static abstract class Defaults implements CompoundItsco {
//        @Override
        public String getMyString() {
            return this.getSubItsco().getValue2();
        }

//        @Override
        public Float getMyFloat() {
            return this.getSubItsco().getIntValue() * 4.5f;
        }
    }
}
