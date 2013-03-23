package net.peachjean.confobj.support;

interface ImplementationGenerator {
    <T> Class<? extends T> implement(Class<T> confObjType) throws ImplementationException;

    class ImplementationException extends RuntimeException {
        public ImplementationException(String message) {
            super(message);
        }

        public ImplementationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
