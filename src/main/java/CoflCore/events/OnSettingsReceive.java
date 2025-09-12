package CoflCore.events;

import CoflCore.classes.Settings;
import java.util.ArrayList;

public class OnSettingsReceive {
    public final ArrayList<Settings> Settings;

    public OnSettingsReceive(ArrayList<Settings> settings) {
        Settings = settings;
    }
}
