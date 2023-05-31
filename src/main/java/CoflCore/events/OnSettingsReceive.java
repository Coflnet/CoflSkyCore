package CoflCore.events;

import CoflCore.classes.Settings;

public class OnSettingsReceive {
    public final Settings Settings;

    public OnSettingsReceive(CoflCore.classes.Settings settings) {
        Settings = settings;
    }
}
