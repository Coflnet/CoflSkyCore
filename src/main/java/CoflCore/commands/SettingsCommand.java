package CoflCore.commands;

import CoflCore.commands.models.SettingsData;

public class SettingsCommand extends Command<SettingsData> {

    public SettingsCommand() {
        super();
    }

    public SettingsCommand(CommandType type, SettingsData data) {
        super(type, data);
    }

    public SettingsCommand(SettingsData data) {
        super(CommandType.Settings, data);
    }
}
