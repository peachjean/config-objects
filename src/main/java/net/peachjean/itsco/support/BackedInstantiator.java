package net.peachjean.itsco.support;

interface BackedInstantiator<T> {
    T instantiate(ItscoBacker backer);

    T instantiate(ItscoBacker backer, InstantiationContext context);

}
