package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName("x")
    private int x;
    @SerializedName("y")
    private int y;
    @SerializedName("z")
    private int z;

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    public Position() {}

    public Position(int xp, int yp, int zp){
        x = xp;
        y = yp;
        z = zp;
    }
}
