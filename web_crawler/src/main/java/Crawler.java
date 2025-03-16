import java.util.ArrayList;

public class Crawler {
    private String url;
    private int depth;
    private ArrayList<String> domains;

    public Crawler(String url, int depth, ArrayList<String> domains) {
        this.url = url;
        this.depth = depth;
        this.domains = domains;
    }

    public void startCrawl() {

    }
}
