package dev.serverforge.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Formatage / lecture de durees ("24h", "1d12h", "30m", "90s", nombre = secondes). */
public final class TimeUtil {
    private TimeUtil() {}
    private static final Pattern TOKEN = Pattern.compile("(\\d+)\\s*([dhms]?)", Pattern.CASE_INSENSITIVE);

    public static long parseSeconds(String input) {
        if (input == null) return 0;
        String s = input.trim().toLowerCase();
        if (s.isEmpty() || s.equals("0") || s.equals("none") || s.equals("off") || s.equals("aucun")) return 0;
        if (s.matches("\\d+")) { try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0; } }
        s = s.replace("j", "d");
        long total = 0; boolean matched = false;
        Matcher m = TOKEN.matcher(s);
        while (m.find()) {
            String num = m.group(1), unit = m.group(2);
            if (num.isEmpty()) continue;
            long v; try { v = Long.parseLong(num); } catch (NumberFormatException e) { continue; }
            switch (unit) {
                case "d" -> total += v * 86400L;
                case "h" -> total += v * 3600L;
                case "m" -> total += v * 60L;
                case "s", "" -> total += v;
                default -> {}
            }
            matched = true;
        }
        return matched ? total : 0;
    }

    public static String format(long millis) {
        if (millis <= 0) return "0s";
        long s = millis / 1000;
        long d = s / 86400; s %= 86400;
        long h = s / 3600;  s %= 3600;
        long m = s / 60;    s %= 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("j ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (d == 0 && h == 0 && (s > 0 || m == 0)) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
