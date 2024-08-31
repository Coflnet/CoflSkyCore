package CoflCore.events;

import CoflCore.commands.models.FlipData;

public class OnOpenAuctionGUI {
    public String openAuctionCommand;
    public FlipData flip;

    public OnOpenAuctionGUI(String openAuctionCommand, FlipData flip) {
        this.openAuctionCommand = openAuctionCommand;
        this.flip = flip;
    }
}
