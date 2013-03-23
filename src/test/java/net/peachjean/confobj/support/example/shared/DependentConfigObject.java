package net.peachjean.confobj.support.example.shared;

import net.peachjean.confobj.ConfigObject;

@ConfigObject
public interface DependentConfigObject {

    String getPath();

    public static abstract class Defaults implements DependentConfigObject {

        private final SharedConfigObject sharedConfigObject;

        public Defaults(SharedConfigObject sharedConfigObject) {
            this.sharedConfigObject = sharedConfigObject;
        }

        @Override
        public String getPath() {
            return sharedConfigObject.getNamespace() + "/myFile";
        }
    }
}
