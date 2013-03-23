package net.peachjean.confobj.support.example.shared;

import net.peachjean.confobj.support.ConfigObjectBacker;

public class DependentItscoImplExample extends DependentItsco.Defaults {
    private final ConfigObjectBacker<DependentItsco> backer;

    public DependentItscoImplExample(ConfigObjectBacker<DependentItsco> backer, SharedItsco sharedItsco) {
        super(sharedItsco);
        this.backer = backer;
        this.backer.setContaining(this);
    }

    @Override
    public String getPath() {
        return backer.lookup("path", String.class, super.getPath());
    }
}
