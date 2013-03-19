package net.peachjean.itsco.support;

import javassist.CannotCompileException;
import javassist.NotFoundException;

interface ImplementationGenerator {
    <T> Class<? extends T> implementor(Class<T> itscoClass) throws NotFoundException, CannotCompileException;
}
