package net.peachjean.itsco.cli.example;

import net.peachjean.itsco.Itsco;
import net.peachjean.itsco.cli.OptDesc;
import net.peachjean.itsco.cli.OptTogglesFalse;

@Itsco
public interface ExampleItsco {
    public String getStringness();

    @OptDesc(shortOpt = "i", description = "The integer value.")
    public Integer getMyInt();

    @OptDesc(shortOpt = "v", description = "Is this valid?")
    public Boolean getValid();

    @OptDesc(shortOpt = "q", description = "Set this to make output quiet.")
    @OptTogglesFalse
    public boolean getVerbose();

    public static abstract class Defaults implements ExampleItsco {
        @Override
        public String getStringness() {
            return "stringness!";
        }
    }
}
