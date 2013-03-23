package net.peachjean.confobj.support.example.shared;

import net.peachjean.confobj.ConfigObject;

@ConfigObject
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
