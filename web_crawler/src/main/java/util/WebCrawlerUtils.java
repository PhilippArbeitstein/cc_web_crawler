package util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.Optional;

public class WebCrawlerUtils {

    public static Optional<String> normalizeUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath().replaceAll("/$", "");
            String host = url.getHost();

            if (!host.startsWith("www.")) {
                host = "www." + host;
            }

            String normalizedUrl = url.getProtocol() + "://" + host + path;
            return Optional.of(normalizedUrl);
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    public static boolean isDomainAllowed(String urlString, Set<String> allowedDomains) {
        try {
            URL url = new URL(urlString);
            return allowedDomains.contains(url.getHost());
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
