package net.peachjean.itsco.support.example.shared;

import net.peachjean.itsco.Itsco;

@Itsco
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
