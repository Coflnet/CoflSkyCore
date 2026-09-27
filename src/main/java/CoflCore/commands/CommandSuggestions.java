package CoflCore.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CommandSuggestions {
    private CommandSuggestions() {
    }

    public static List<Map.Entry<String, String>> matching(Map<String, String> commands, String arguments) {
        List<Map.Entry<String, String>> matches = new ArrayList<>();
        if (commands == null) {
            return matches;
        }
        String prefix = arguments.toLowerCase(Locale.ROOT);
        boolean hasArguments = prefix.contains(" ");
        for (Map.Entry<String, String> command : commands.entrySet()) {
            String name = command.getKey();
            if (name != null && (hasArguments || !name.contains(" "))
                    && name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(new java.util.AbstractMap.SimpleImmutableEntry<>(command));
            }
        }
        return matches;
    }
}
