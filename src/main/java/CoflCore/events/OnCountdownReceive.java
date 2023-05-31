package CoflCore.events;

import CoflCore.classes.Countdown;

public class OnCountdownReceive {
    public final Countdown CountdownData;

    public OnCountdownReceive(Countdown countdownData) {
        this.CountdownData = countdownData;
    }
}
