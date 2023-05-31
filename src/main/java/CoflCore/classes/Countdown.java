package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Countdown {

    @SerializedName("seconds")
    private Integer Duration;

    @SerializedName("widthPercent")
    private Integer WidthPercentage;

    @SerializedName("heightPercent")
    private Integer HeightPercentage;

    @SerializedName("scale")
    private Integer Scale;

    @SerializedName("prefix")
    private String Prefix;

    @SerializedName("maxPrecision")
    private Integer MaxPrecision;

    public Integer getDuration() {
        return Duration;
    }

    public Integer getWidthPercentage() {
        return WidthPercentage;
    }

    public Integer getHeightPercentage() {
        return HeightPercentage;
    }

    public Integer getScale() {
        return Scale;
    }

    public String getPrefix() {
        return Prefix;
    }

    public Integer getMaxPrecision() {
        return MaxPrecision;
    }

    public Countdown() {

    }

    public Countdown(Integer duration, Integer widthPercentage, Integer heightPercentage, Integer scale, String prefix, Integer maxPrecision) {
        super();
        this.Duration = duration;
        this.WidthPercentage = widthPercentage;
        this.HeightPercentage = heightPercentage;
        this.Scale = scale;
        this.Prefix = prefix;
        this.MaxPrecision = maxPrecision;
    }
}
