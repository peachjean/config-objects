package net.peachjean.confobj.support.example;

public interface CompoundConfigObject {
    ExampleConfigObject getSubConfigObject();

    String getMyString();

    Float getMyFloat();

    public static abstract class Defaults implements CompoundConfigObject {
//        @Override
        public String getMyString() {
            return this.getSubConfigObject().getValue2();
        }

//        @Override
        public Float getMyFloat() {
            return this.getSubConfigObject().getIntValue() * 4.5f;
        }
    }
}
