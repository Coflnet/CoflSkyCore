package CoflCore.events;

import CoflCore.commands.models.FlipData;

public class OnFlipReceive {
    public final FlipData FlipData;

    public OnFlipReceive(FlipData flipData) {
        this.FlipData = flipData;
    }
}
