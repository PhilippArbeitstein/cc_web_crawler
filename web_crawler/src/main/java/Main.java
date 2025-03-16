import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length > 2) {
            Crawler crawler = generateWebCrawler(args);
            crawler.runCrawl(crawler.getUrl());
        } else {
            printHowToUseProgramm();
        }
    }

    private static void printHowToUseProgramm() {
        System.out.println("Error: Wrong parameters have been provided.");
        System.out.println("Please use the program like this: java <Program Name> <Url to Crawl>" +
                " <Depth to Crawl> <Domain 1> <Domain 2> ...");
    }

    private static Crawler generateWebCrawler(String[] args) {
        String url = args[0];
        int depth = Integer.parseInt(args[1]);
        ArrayList<String> domains = getAllDomains(args);

        return new Crawler(url, depth, domains);
    }

    private static ArrayList<String> getAllDomains(String[] args) {
        ArrayList<String> allDomains = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
        return allDomains;
    }
}
