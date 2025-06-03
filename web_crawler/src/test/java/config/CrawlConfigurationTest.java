package config;

import exceptions.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CrawlConfigurationTest {
    private URL rootUrl;
    private Set<String> validDomains;
    private Set<String> invalidDomains;

    @BeforeEach
    void setUp() throws Exception {
        rootUrl = new URL("http://example.com");
        validDomains = new HashSet<>();
        validDomains.add("http://example.com");
        validDomains.add("https://test.com");
        invalidDomains = new HashSet<>();
        invalidDomains.add("invalid-domain");
    }

    @Test
    void testConstructorValidArguments() {
        CrawlConfiguration config = new CrawlConfiguration(rootUrl, Optional.of(3), validDomains);

        assertNotNull(config);
        assertEquals(rootUrl, config.rootUrl());
        assertEquals(Optional.of(3), config.maxDepth());
        assertTrue(config.crawlableDomains().contains("example.com"));
        assertTrue(config.crawlableDomains().contains("test.com"));
    }

    @Test
    void testCrawlConfigurationConstructorWithInvalidDomains() {
        Set<String> invalidDomains = new HashSet<>(List.of("invalid-domain", "another-invalid-domain"));
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            new CrawlConfiguration(rootUrl, Optional.of(3), invalidDomains);
        });
        assertEquals("At least one valid domain must be provided.", exception.getMessage());
    }

    @Test
    void testConstructorNullRootUrl() {
        assertThrows(ConfigurationException.class, () -> {
            new CrawlConfiguration(null, Optional.of(3), validDomains);
        }, "Root URL cannot be null.");
    }

    @Test
    void testConstructorEmptyMaxDepth() {
        assertThrows(ConfigurationException.class, () -> {
            new CrawlConfiguration(rootUrl, Optional.empty(), validDomains);
        }, "Max depth must be greater than zero.");
    }

    @Test
    void testConstructorNullOrEmptyCrawlableDomains() {
        assertThrows(ConfigurationException.class, () -> {
            new CrawlConfiguration(rootUrl, Optional.of(3), null);
        }, "At least one crawlable domain must be provided.");

        assertThrows(ConfigurationException.class, () -> {
            new CrawlConfiguration(rootUrl, Optional.of(3), new HashSet<>());
        }, "At least one crawlable domain must be provided.");
    }

    @Test
    void testProcessDomainsWithValidDomains() {
        CrawlConfiguration config = new CrawlConfiguration(rootUrl, Optional.of(3), validDomains);

        Set<String> processedDomains = config.processDomains(validDomains);

        assertNotNull(processedDomains);
        assertFalse(processedDomains.isEmpty());
        assertTrue(processedDomains.contains("example.com"));
        assertTrue(processedDomains.contains("test.com"));
    }

    @Test
    void testNormalizeDomains() {
        CrawlConfiguration config = new CrawlConfiguration(rootUrl, Optional.of(3), validDomains);

        Set<String> normalizedDomains = config.normalizeDomains(validDomains);

        assertNotNull(normalizedDomains);
        assertEquals(2, normalizedDomains.size());
        assertTrue(normalizedDomains.contains("http://example.com"));
        assertTrue(normalizedDomains.contains("https://test.com"));
    }

    @Test
    void testExtractValidDomainsWithValidURLs() {
        CrawlConfiguration config = new CrawlConfiguration(rootUrl, Optional.of(3), validDomains);

        Set<String> validExtractedDomains = config.extractValidDomains(validDomains);

        assertNotNull(validExtractedDomains);
        assertEquals(2, validExtractedDomains.size());
        assertTrue(validExtractedDomains.contains("example.com"));
        assertTrue(validExtractedDomains.contains("test.com"));
    }

    @Test
    void testExtractHostFromDomainWithValidURL() {
        CrawlConfiguration config = new CrawlConfiguration(rootUrl, Optional.of(3), validDomains);

        Optional<String> host = config.extractHostFromDomain("http://example.com");

        assertTrue(host.isPresent());
        assertEquals("example.com", host.get());
    }

    @Test
    void testExtractHostFromDomainWithInvalidURL() {
        CrawlConfiguration config = new CrawlConfiguration(rootUrl, Optional.of(3), validDomains);

        Optional<String> host = config.extractHostFromDomain("invalid-domain");

        assertFalse(host.isPresent());
    }
}
