package net.peachjean.itsco.support.example.shared;

import net.peachjean.itsco.Itsco;

@Itsco
public interface MasterItsco {
    public SharedItsco getShared();

    public DependentItsco getDependent();

    public static abstract class Defaults implements MasterItsco {
        public class Dependent {

            // optionally, this can be annotated
            SharedItsco provideShared(MasterItsco masterItsco) {
                return masterItsco.getShared();
            }
        }
    }
}
