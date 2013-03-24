package net.peachjean.confobj.support.example.shared;

import net.peachjean.confobj.introspection.GenericType;
import net.peachjean.confobj.support.ConfigObjectBacker;

public class DependentConfigObjectImplExample extends DependentConfigObject.Defaults {
    private final ConfigObjectBacker<DependentConfigObject> backer;

    public DependentConfigObjectImplExample(ConfigObjectBacker<DependentConfigObject> backer, SharedConfigObject sharedConfigObject) {
        super(sharedConfigObject);
        this.backer = backer;
        this.backer.setContaining(this);
    }

    @Override
    public String getPath() {
        return backer.lookup("path", GenericType.forType(String.class), super.getPath());
    }
}
