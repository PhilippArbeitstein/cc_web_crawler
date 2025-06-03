package util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownTest {

    @Test
    void createHeadingGeneratesCorrectFormat() {
        String result = Markdown.createHeading("Test", 2, 1);
        assertEquals("## \t-> \tTest", result);
    }

    @Test
    void createLinkInfoReturnsNormalLink() {
        String result = Markdown.createLinkInfo("https://example.com", 2, false);
        assertEquals("<br>--> link to <a>https://example.com </a>", result);
    }

    @Test
    void createLinkInfoReturnsBrokenLink() {
        String result = Markdown.createLinkInfo("https://fail.com", 3, true);
        assertEquals("<br>---> broken link <a>https://fail.com </a>", result);
    }

    @Test
    void extractHeadingParsesValidHeading() {
        Optional<String> heading = Markdown.extractHeading("h2:Title", 1);
        assertTrue(heading.isPresent());
        assertEquals("## \t-> \tTitle", heading.get());
    }

    @Test
    void extractHeadingReturnsEmptyIfInvalidFormat() {
        assertTrue(Markdown.extractHeading("invalid", 1).isEmpty());
        assertTrue(Markdown.extractHeading(null, 1).isEmpty());
        assertTrue(Markdown.extractHeading("h7:TooHigh", 1).isEmpty());
    }

    @Test
    void isInvalidHeadingFormatDetectsInvalid() {
        assertTrue(Markdown.isInvalidHeadingFormat(null));
        assertTrue(Markdown.isInvalidHeadingFormat("noPrefix"));
        assertTrue(Markdown.isInvalidHeadingFormat("h9:Invalid"));
    }

    @Test
    void isInvalidHeadingFormatDetectsValid() {
        assertFalse(Markdown.isInvalidHeadingFormat("h1:Headline"));
    }
}

