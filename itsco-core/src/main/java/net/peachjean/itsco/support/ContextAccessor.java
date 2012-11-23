package net.peachjean.itsco.support;

public interface ContextAccessor<C> {
    boolean contains(C context, String key);

    String contextLookup(C context, String key);

    C subContextLookup(C context, String key);
}
