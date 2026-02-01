package CoflCore.commands.models;

public class LoggedInData {
    public String uuid;
    public boolean verified;
    
    public LoggedInData() {
    }
    
    public LoggedInData(String uuid, boolean verified) {
        this.uuid = uuid;
        this.verified = verified;
    }
}
