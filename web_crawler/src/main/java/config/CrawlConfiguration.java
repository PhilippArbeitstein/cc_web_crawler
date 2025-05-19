package config;

import exceptions.ConfigurationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */
public record CrawlConfiguration(URL rootUrl, int maxDepth, Set<String> crawlableDomains) {
    private static final Logger logger = Logger.getLogger(CrawlConfiguration.class.getName());

    public CrawlConfiguration(URL rootUrl, int maxDepth, Set<String> crawlableDomains) {
        if (rootUrl == null) {
            throw new ConfigurationException("Root URL cannot be null.");
        }
        if (maxDepth < 1) {
            throw new ConfigurationException("Max depth must be greater than zero.");
        }
        if (crawlableDomains == null || crawlableDomains.isEmpty()) {
            throw new ConfigurationException("At least one crawlable domain must be provided.");
        }
        this.rootUrl = rootUrl;
        this.maxDepth = maxDepth;
        this.crawlableDomains = processDomains(crawlableDomains);
    }

    public Set<String> processDomains(Set<String> rawDomains) throws IllegalArgumentException {
        Set<String> normalizedDomains = normalizeDomains(rawDomains);
        return extractValidDomains(normalizedDomains);
    }

    private Set<String> normalizeDomains(Set<String> rawDomains) {
        Set<String> normalizedDomains = new HashSet<>();
        for (String domain : rawDomains) {
            String trimmedDomain = domain.trim().toLowerCase();
            addDomainIfNotEmpty(trimmedDomain, normalizedDomains);
        }
        return normalizedDomains;
    }

    private void addDomainIfNotEmpty(String domain, Set<String> domainsSet) {
        if (!domain.isEmpty()) {
            domainsSet.add(domain);
        }
    }

    private Set<String> extractValidDomains(Set<String> normalizedDomains) throws IllegalArgumentException {
        Set<String> validDomains = new HashSet<>();
        for (String domain : normalizedDomains) {
            extractHostFromDomain(domain).ifPresent(validDomains::add);
        }

        if (validDomains.isEmpty()) {
            throw new ConfigurationException("At least one valid domain must be provided.");
        }
        return validDomains;
    }

    private Optional<String> extractHostFromDomain(String domain) {
        try {
            URL url = new URL(domain);
            return Optional.of(url.getHost());
        } catch (MalformedURLException e) {
            logger.warning("Malformed Domain-URL encountered: " + domain + " - " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Root URL       : %s%nMax Depth      : %d%nAllowed Domains: %s",
                rootUrl, maxDepth, String.join(", ", crawlableDomains)
        );
    }
}