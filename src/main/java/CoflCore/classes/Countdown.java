package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Countdown {

    @SerializedName("seconds")
    private Double Duration;

    @SerializedName("widthPercent")
    private Integer WidthPercentage;

    @SerializedName("heightPercent")
    private Integer HeightPercentage;

    @SerializedName("scale")
    private Double Scale;

    @SerializedName("prefix")
    private String Prefix;

    @SerializedName("maxPrecision")
    private Integer MaxPrecision;

    public Double getDuration() {
        return Duration;
    }

    public Integer getWidthPercentage() {
        return WidthPercentage;
    }

    public Integer getHeightPercentage() {
        return HeightPercentage;
    }

    public Double getScale() {
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

    public Countdown(Double duration, Integer widthPercentage, Integer heightPercentage, Double scale, String prefix, Integer maxPrecision) {
        super();
        this.Duration = duration;
        this.WidthPercentage = widthPercentage;
        this.HeightPercentage = heightPercentage;
        this.Scale = scale;
        this.Prefix = prefix;
        this.MaxPrecision = maxPrecision;
    }
}
