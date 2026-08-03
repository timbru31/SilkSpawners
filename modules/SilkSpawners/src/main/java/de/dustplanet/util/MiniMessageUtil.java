package de.dustplanet.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Turns the configured messages into Adventure components. Every message supports the full MiniMessage syntax, the legacy ampersand codes
 * are still understood and converted to their MiniMessage equivalent beforehand.
 *
 * @author timbru31
 */
public class MiniMessageUtil {
    /**
     * MiniMessage instance used to parse all messages.
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Serializer used whenever Bukkit only accepts a legacy string, e.g. for item names or the boss bar.
     */
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('§').hexColors()
            .useUnusualXRepeatedCharacterHexFormat().build();

    /**
     * Matches the legacy color and formatting codes, both with an ampersand and with a section sign, including the hex variants.
     */
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[&§](#[0-9a-fA-F]{6}|[0-9a-fA-Fk-orK-OR])");

    /**
     * MiniMessage tag of every legacy code, indexed by the lowercase code character.
     */
    private static final String[] LEGACY_TAGS = new String[128];

    static {
        LEGACY_TAGS['0'] = "black";
        LEGACY_TAGS['1'] = "dark_blue";
        LEGACY_TAGS['2'] = "dark_green";
        LEGACY_TAGS['3'] = "dark_aqua";
        LEGACY_TAGS['4'] = "dark_red";
        LEGACY_TAGS['5'] = "dark_purple";
        LEGACY_TAGS['6'] = "gold";
        LEGACY_TAGS['7'] = "gray";
        LEGACY_TAGS['8'] = "dark_gray";
        LEGACY_TAGS['9'] = "blue";
        LEGACY_TAGS['a'] = "green";
        LEGACY_TAGS['b'] = "aqua";
        LEGACY_TAGS['c'] = "red";
        LEGACY_TAGS['d'] = "light_purple";
        LEGACY_TAGS['e'] = "yellow";
        LEGACY_TAGS['f'] = "white";
        LEGACY_TAGS['k'] = "obfuscated";
        LEGACY_TAGS['l'] = "bold";
        LEGACY_TAGS['m'] = "strikethrough";
        LEGACY_TAGS['n'] = "underlined";
        LEGACY_TAGS['o'] = "italic";
        LEGACY_TAGS['r'] = "reset";
    }

    /**
     * Audience provider, needed because Spigot has no native component support. Shared between all instances so that API users creating
     * their own SilkUtil do not register a second provider.
     */
    private static BukkitAudiences audiences;

    /**
     * Constructor, creates the audience provider for the given plugin once.
     *
     * @param plugin the (enabled) plugin instance
     */
    public MiniMessageUtil(final Plugin plugin) {
        if (audiences == null) {
            audiences = BukkitAudiences.create(plugin);
        }
    }

    /**
     * Parses a message into a component. Legacy codes are converted to MiniMessage tags first, so both syntaxes - and a mixture of them -
     * are supported.
     *
     * @param message the raw message, may be null
     * @return the parsed component, an empty component for blank input
     */
    public Component parse(final String message) {
        if (message == null || message.trim().isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(legacyToMiniMessage(message));
    }

    /**
     * Parses a message and serializes it back into a legacy string. Needed wherever Bukkit only accepts a string, e.g. item display names.
     *
     * @param message the raw message, may be null
     * @return the legacy representation, an empty string for blank input
     */
    public String toLegacy(final String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(parse(message));
    }

    /**
     * Parses and sends a message to a player or the console.
     *
     * @param receiver the receiver of the message
     * @param message the raw message
     */
    public void send(final CommandSender receiver, final String message) {
        if (receiver == null || message == null || message.trim().isEmpty()) {
            return;
        }
        audiences.sender(receiver).sendMessage(parse(message));
    }

    /**
     * Closes the audience provider, has to be called when the plugin is disabled.
     */
    public void close() {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    /**
     * Rewrites every legacy color and formatting code into its MiniMessage counterpart. Unknown codes are left untouched.
     *
     * @param message the raw message
     * @return the message with MiniMessage tags only
     */
    static String legacyToMiniMessage(final String message) {
        final Matcher matcher = LEGACY_PATTERN.matcher(message);
        final StringBuffer builder = new StringBuffer(message.length());
        while (matcher.find()) {
            final String code = matcher.group(1);
            final String replacement;
            if (code.charAt(0) == '#') {
                replacement = "<" + code.toLowerCase(Locale.ENGLISH) + ">";
            } else {
                final String tag = LEGACY_TAGS[Character.toLowerCase(code.charAt(0))];
                replacement = tag == null ? matcher.group() : "<" + tag + ">";
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}
