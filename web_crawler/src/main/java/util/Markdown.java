package util;

import java.util.Optional;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public class Markdown {

    public static String createHeading(String text, int level, int depth) {
        return "#".repeat(level) + " \t" + "-".repeat(depth) + "> \t" + text;
    }

    public static String createLinkInfo(String url, int depth, boolean isBroken) {
        String prefix = "-".repeat(depth) + "> ";
        String type = isBroken ? "broken link" : "link to";
        return "<br>" + prefix + type + " <a>" + url + " </a>";
    }

    public static Optional<String> extractHeading(String raw, int depth) {
        if (isInvalidHeadingFormat(raw)) {
            return Optional.empty();
        }        int level = Integer.parseInt(raw.substring(1, 2));
        String text = raw.substring(3);
        return Optional.of(createHeading(text, level, depth));
    }
    private static boolean isInvalidHeadingFormat(String raw) {
        return raw == null || !raw.matches("h[1-6]:.*");
    }
}
