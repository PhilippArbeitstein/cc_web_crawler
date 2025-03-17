import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int argumentCount = args.length;
        if (argumentCount > 2) {
            launchCrawler(args); // TODO: Maybe createAndRunCrawl() instead?
        } else {
            printHowToUseProgram();
        }
    }

    private static void launchCrawler(String[] args) {
        Crawler crawler = createCrawler(args);
        crawler.runCrawl(crawler.getUrl());
    }

    private static void printHowToUseProgram() {
        System.out.println("Error: Wrong parameters have been provided.");
        System.out.println("Please use the program like this: java <Program Name> <Url to Crawl>" +
                " <Depth to Crawl> <Domain 1> <Domain 2> ...");
    }

    private static Crawler createCrawler(String[] args) {
        String url = args[0];
        int maxDepth = Integer.parseInt(args[1]);
        ArrayList<String> domains = extractDomainsFromArgs(args);

        return new Crawler(url, maxDepth, domains);
    }

    private static ArrayList<String> extractDomainsFromArgs(String[] args) {
        return new ArrayList<>(Arrays.asList(args).subList(2, args.length));
    }
}
