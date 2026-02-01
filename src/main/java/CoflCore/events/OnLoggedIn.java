package CoflCore.events;

public class OnLoggedIn {
    public final String uuid;
    public final boolean verified;

    public OnLoggedIn(String uuid, boolean verified) {
        this.uuid = uuid;
        this.verified = verified;
    }
}
