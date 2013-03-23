package net.peachjean.confobj.support.example.shared;

import net.peachjean.confobj.ConfigObject;

@ConfigObject
public interface MasterConfigObject {
    public SharedConfigObject getShared();

    public DependentConfigObject getDependent();

    public static abstract class Defaults implements MasterConfigObject {
        public class Dependent {

            // optionally, this can be annotated
            SharedConfigObject provideShared(MasterConfigObject masterConfigObject) {
                return masterConfigObject.getShared();
            }
        }
    }
}
