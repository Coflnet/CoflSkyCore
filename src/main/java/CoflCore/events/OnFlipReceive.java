package CoflCore.events;

import CoflCore.classes.Flip;

public class OnFlipReceive {
    public final Flip FlipData;

    public OnFlipReceive(Flip flipData) {
        this.FlipData = flipData;
    }
}
