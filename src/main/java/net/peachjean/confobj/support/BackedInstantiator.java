package net.peachjean.confobj.support;

interface BackedInstantiator<T> {
    T instantiate(ConfigObjectBacker backer);

    T instantiate(ConfigObjectBacker backer, InstantiationContext context);

}
