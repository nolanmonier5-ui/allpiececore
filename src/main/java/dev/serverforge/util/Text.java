package dev.serverforge.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/** Conversion des codes couleur '&' (et MiniMessage) en Component Adventure. */
public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private Text() {}

    public static Component item(String s) {
        return color(s).decoration(TextDecoration.ITALIC, false);
    }
    public static Component color(String s) {
        if (s == null) s = "";
        if (s.contains("<") && s.contains(">")) {
            try { return MM.deserialize(s); } catch (Exception ignored) {}
        }
        return LEGACY.deserialize(s);
    }
}
