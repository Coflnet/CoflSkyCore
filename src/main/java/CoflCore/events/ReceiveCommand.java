package CoflCore.events;

import CoflCore.commands.JsonStringCommand;

public class ReceiveCommand {
    public final JsonStringCommand command;

    public ReceiveCommand(JsonStringCommand data) {
        this.command = data;
    }
}
