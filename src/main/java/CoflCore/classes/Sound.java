package CoflCore.classes;

import com.google.gson.annotations.SerializedName;

public class Sound {
    public String getSoundName() {
        return SoundName;
    }

    public Float getSoundPitch() {
        return SoundPitch;
    }

    @SerializedName("name")
    private String SoundName;

    @SerializedName("pitch")
    private Float SoundPitch;

    public Sound() {

    }

    public Sound(String soundName, Float pitch) {
        super();
        this.SoundName = soundName;
        this.SoundPitch = pitch;
    }
}
