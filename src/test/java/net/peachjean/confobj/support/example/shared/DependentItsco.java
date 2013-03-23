package net.peachjean.confobj.support.example.shared;

import net.peachjean.confobj.ConfigObject;

@ConfigObject
public interface DependentItsco {

    String getPath();

    public static abstract class Defaults implements DependentItsco {

        private final SharedItsco sharedItsco;

        public Defaults(SharedItsco sharedItsco) {
            this.sharedItsco = sharedItsco;
        }

        @Override
        public String getPath() {
            return sharedItsco.getNamespace() + "/myFile";
        }
    }
}
