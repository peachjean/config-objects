//package net.peachjean.itsco.cli;
//
//import net.peachjean.itsco.support.DefaultItscoFactory;
//
//public class CliDefaultItscoFactory extends DefaultItscoFactory<ParsedOptions> {
//    @Override
//    public boolean contains(final ParsedOptions context, final String key) {
//        return context.hasOption(key);
//    }
//
//    @Override
//    public String contextLookup(final ParsedOptions context, final String key) {
//        return context.getValue(key);
//    }
//
//    @Override
//    public ParsedOptions subContextLookup(final ParsedOptions context, final String key) {
//        return context.subValue(key);
//    }
//}
