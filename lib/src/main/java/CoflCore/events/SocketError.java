package CoflCore.events;

public class SocketError {
    public final Exception error;
    public SocketError(Exception e) {
        this.error = e;
    }
}
