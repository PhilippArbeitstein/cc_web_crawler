package util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class WebCrawlerUtils {

    public static String normalizeUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath().replaceAll("/$", "");
            String host = url.getHost();

            if (!host.startsWith("www.")) {
                host = "www." + host;
            }

            String normalizedUrl = url.getProtocol() + "://" + host + path;
            return normalizedUrl;
        } catch (MalformedURLException e) {
            return "";
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