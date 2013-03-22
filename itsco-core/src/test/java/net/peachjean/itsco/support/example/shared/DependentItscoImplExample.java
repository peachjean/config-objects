package net.peachjean.itsco.support.example.shared;

import net.peachjean.itsco.support.ItscoBacker;

public class DependentItscoImplExample extends DependentItsco.Defaults {
    private final ItscoBacker<DependentItsco> backer;

    public DependentItscoImplExample(ItscoBacker<DependentItsco> backer, SharedItsco sharedItsco) {
        super(sharedItsco);
        this.backer = backer;
        this.backer.setContaining(this);
    }

    @Override
    public String getPath() {
        return backer.lookup("path", String.class, super.getPath());
    }
}
