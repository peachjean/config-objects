package net.peachjean.itsco.support;

public class StringResolutionStrategy implements FieldResolutionStrategy {

    public static final StringResolutionStrategy INSTANCE = new StringResolutionStrategy();

    @Override
    public <T,C> T resolve(final String name, final Class<T> lookupType, final C context, final ContextAccessor<C> contextAccessor) {
        if(!this.supports(lookupType))
        {
            throw new IllegalArgumentException("This strategy only supports Strings.");
        }
        if(!contextAccessor.contains(context, name))
        {
            return null;
        }
        else
        {
            return lookupType.cast(contextAccessor.contextLookup(context, name));
        }
    }

    @Override
    public boolean supports(final Class<?> lookupType) {
        return String.class.equals(lookupType);
    }

    @Override
    public boolean handlesReloading() {
        return false;
    }
}
