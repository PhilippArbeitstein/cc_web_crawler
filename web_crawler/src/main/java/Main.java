import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    Participants:
    - Philipp Arbeitstein [12205666]
    - Philipp Kaiser [12203588]
 */

public class Main {
    private static final int ARGUMENTS_PER_CRAWLER = 2;

    public static void main(String[] args) {
        int numberOfDomains = findValidDomainCount(args);
        if (numberOfDomains > 0) {
            launchCrawlers(args, numberOfDomains);
        } else {
            printHowToUseProgram();
        }
    }
    private static int findValidDomainCount(String[] args) {
        if (args.length < 3) return 0;

        for (int domainCount = 1; domainCount <= args.length - 2; domainCount++) {
            int crawlerArgs = args.length - domainCount;
            if (crawlerArgs % ARGUMENTS_PER_CRAWLER == 0) {
                return domainCount;
            }
        }
        return 0;
    }

    private static void launchCrawlers(String[] args, int numberOfDomains) {
        int numberOfCrawlers = (args.length - numberOfDomains) / ARGUMENTS_PER_CRAWLER;
        ArrayList<String> domains = extractDomainsFromArgs(args, numberOfCrawlers);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfCrawlers);
        ReportWriter sharedWriter = new ReportWriter("report.md");

        for (int i = 0; i < numberOfCrawlers; i++) {
            String url = args[i * 2];
            int maxDepth = Integer.parseInt(args[i * 2 + 1]);

            executor.submit(() -> {
                Crawler crawler = new Crawler(url, maxDepth, domains, sharedWriter);
                crawler.runCrawl(url);
            });
        }

        executor.shutdown();
    }

    private static ArrayList<String> extractDomainsFromArgs(String[] args, int numberOfCrawlers) {
        return new ArrayList<>(Arrays.asList(args).subList(numberOfCrawlers * 2, args.length));
    }

    private static void printHowToUseProgram() {
        System.out.println("Error: Wrong parameters have been provided.");
        System.out.println("Please use the program like this: java <Program Name> <Url1> <Depth1> <Url2> <Depth2> ... <Domain1> <Domain2> ...");
    }
}

