package CoflCore.events;

import CoflCore.classes.Tier;

public class OnTierRequestReceive {
    public final Tier Tier;

    public OnTierRequestReceive(CoflCore.classes.Tier tier) {
        this.Tier = tier;
    }
}
