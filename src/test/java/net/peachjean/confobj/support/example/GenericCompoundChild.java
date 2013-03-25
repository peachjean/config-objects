package net.peachjean.confobj.support.example;

import net.peachjean.confobj.ConfigObject;

import java.util.List;

@ConfigObject
public interface GenericCompoundChild {
    String getCombinedRoles();

    Integer getMaxLimit();

    public static abstract class Defaults implements GenericCompoundChild {
        private final List<String> roles;
        private final List<Integer> limits;

        @Override
        public String getCombinedRoles() {
            StringBuilder sb = new StringBuilder();
            boolean forst = true;
            for(String s: roles) {
                if(!forst) {
                    sb.append(",");
                } else {
                    forst = false;
                }
                sb.append(s);
            }
            return sb.toString();
        }

        @Override
        public Integer getMaxLimit() {
            int sum = 0;
            for(Integer limit: limits) {
                sum += limit;
            }
            return sum;
        }

        public Defaults(List<String> roles, List<Integer> limits) {
            this.roles = roles;
            this.limits = limits;
        }
    }
}
