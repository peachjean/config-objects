package net.peachjean.confobj.support;

public interface FieldResolution<T> {
    T resolve() throws MissingConfigurationException;

    T resolve(T defaultValue);

    public static abstract class Simple<T> implements FieldResolution<T> {
        private final String configurationName;

        protected Simple(String configurationName) {
            this.configurationName = configurationName;
        }

        protected abstract T doResolve();

        @Override
        public T resolve() throws MissingConfigurationException {
            T resolved = this.doResolve();
            if(resolved == null) {
                throw new MissingConfigurationException(configurationName);
            }
            return resolved;
        }

        @Override
        public T resolve(T defaultValue) {
            T resolved = this.doResolve();
            return resolved != null ? resolved : defaultValue;
        }
    }

    public static class Resolved<T> implements FieldResolution<T> {
        private final String configurationName;
        private final T resolved;

        public Resolved(String configurationName, T resolved) {
            this.configurationName = configurationName;
            this.resolved = resolved;
        }

        @Override
        public T resolve() throws MissingConfigurationException {
            if(resolved == null) {
                throw new MissingConfigurationException(configurationName);
            }
            return resolved;
        }

        @Override
        public T resolve(T defaultValue) {
            return resolved != null ? resolved : defaultValue;
        }
    }
}
