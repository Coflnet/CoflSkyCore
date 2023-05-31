package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Sound {
    public String getSoundName() {
        return SoundName;
    }

    public Integer getSoundPitch() {
        return SoundPitch;
    }

    @SerializedName("name")
    private String SoundName;

    @SerializedName("pitch")
    private Integer SoundPitch;

    public Sound() {

    }

    public Sound(String soundName, Integer pitch) {
        super();
        this.SoundName = soundName;
        this.SoundPitch = pitch;
    }
}
