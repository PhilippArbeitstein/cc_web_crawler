package model;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlResult {
    public String pageUrl;
    public int currentDepth;
    public List<String> headings;
    public List<String> childLinks;
    public boolean isFetchFailed;
    public Set<URL> parentUrls = ConcurrentHashMap.newKeySet();
}