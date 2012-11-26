package net.peachjean.itsco;

public interface ItscoBuilder<T> {

    void validate();

    T build();
}
