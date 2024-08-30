package CoflCore.events;

import CoflCore.classes.Tier;

public class OnTierRequestReceive {
    public final Tier Tier;

    public OnTierRequestReceive(Tier tier) {
        this.Tier = tier;
    }
}
