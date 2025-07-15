package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName("x")
    private Integer x;
    @SerializedName("y")
    private Integer y;
    @SerializedName("z")
    private Integer z;

    public Integer getX() { return x; }
    public Integer getY() { return y; }
    public Integer getZ() { return z; }

    public Position() {}

    public Position(Integer xp, Integer yp, Integer zp){
        super();
        x = xp;
        y = yp;
        z = zp;
    }
}
