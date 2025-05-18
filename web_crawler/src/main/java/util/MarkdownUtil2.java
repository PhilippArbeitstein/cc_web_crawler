package util;
/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class MarkdownUtil2 {

    public static String createHeading(String text, int level, int depth) {
        return "#".repeat(level) + " \t" + "-".repeat(depth) + "> \t" + text;
    }

    public static String createLinkInfo(String url, int depth, boolean isBroken) {
        String prefix = "-".repeat(depth) + "> ";
        String type = isBroken ? "broken link" : "link to";
        return "<br>" + prefix + type + " <a>" + url + " </a>";
    }

    public static String extractHeading(String raw, int depth) {
        if (raw == null || !raw.matches("h[1-6]:.*")) return null;
        int level = Integer.parseInt(raw.substring(1, 2));
        String text = raw.substring(3);
        return createHeading(text, level, depth);
    }
}
