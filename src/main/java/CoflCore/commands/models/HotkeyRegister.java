package CoflCore.commands.models;

import com.google.gson.annotations.SerializedName;

public class HotkeyRegister {
	@SerializedName("name")
	public String Name;
	@SerializedName("defaultKey")
	public String DefaultKey;

	public HotkeyRegister() {
	}
}