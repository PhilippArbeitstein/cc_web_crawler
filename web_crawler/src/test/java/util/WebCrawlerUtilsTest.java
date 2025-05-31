package util;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WebCrawlerUtilsTest {

    @Test
    void normalizeUrlReturnsExpectedFormat() {
        Optional<String> normalized = WebCrawlerUtils.normalizeUrl("https://example.com/test/");
        assertTrue(normalized.isPresent());
        assertEquals("https://www.example.com/test", normalized.get());
    }

    @Test
    void normalizeUrlAddsWwwIfMissing() {
        Optional<String> normalized = WebCrawlerUtils.normalizeUrl("https://domain.org/page");
        assertTrue(normalized.isPresent());
        assertEquals("https://www.domain.org/page", normalized.get());
    }

    @Test
    void normalizeUrlReturnsEmptyOnMalformedUrl() {
        Optional<String> normalized = WebCrawlerUtils.normalizeUrl("not-a-valid-url");
        assertTrue(normalized.isEmpty());
    }

    @Test
    void isDomainAllowedReturnsTrueForAllowedDomain() {
        boolean allowed = WebCrawlerUtils.isDomainAllowed("https://www.example.com/page", Set.of("www.example.com"));
        assertTrue(allowed);
    }

    @Test
    void isDomainAllowedReturnsFalseForDisallowedDomain() {
        boolean allowed = WebCrawlerUtils.isDomainAllowed("https://www.other.com/page", Set.of("www.example.com"));
        assertFalse(allowed);
    }

    @Test
    void isDomainAllowedReturnsFalseOnMalformedUrl() {
        boolean allowed = WebCrawlerUtils.isDomainAllowed("://invalid-url", Set.of("any.com"));
        assertFalse(allowed);
    }
    @Test
    void normalizeUrlKeepsHostWithWww() {
        Optional<String> normalized = WebCrawlerUtils.normalizeUrl("https://www.example.com/page/");
        assertTrue(normalized.isPresent());
        assertEquals("https://www.example.com/page", normalized.get());
    }

}
