package CoflCore.commands.models;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.stream.Stream;

public class FlipData {

	@SerializedName("messages")
	public ChatMessageData[] Messages;
	@SerializedName("id")
	public String Id;
	@SerializedName("worth")
	public int Worth;
	@SerializedName("sound")
	public SoundData Sound;
	@SerializedName("render")
	public String Render;

	public FlipData() {
	}

	public FlipData(ChatMessageData[] messages, String id, int worth, SoundData sound, String render) {
		super();
		Messages = messages;
		Id = id;
		Worth = worth;
		Sound = sound;
		Render = render;
	}

	public String getMessageAsString(){
		Stream<String> stream = Arrays.stream(this.Messages).map(message -> message.Text);
		String s = String.join(",", stream.toArray(String[]::new));
		stream.close();
		return s;
	}
}
