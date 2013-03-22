package net.peachjean.itsco.support.example.shared;

import net.peachjean.itsco.support.ItscoBacker;

public class DependentItscoImplExample extends DependentItsco.Defaults {
    private final ItscoBacker backer;

    public DependentItscoImplExample(ItscoBacker backer, SharedItsco sharedItsco) {
        super(sharedItsco);
        this.backer = backer;
    }

    @Override
    public String getPath() {
        return backer.lookup("path", String.class, super.getPath());
    }
}
