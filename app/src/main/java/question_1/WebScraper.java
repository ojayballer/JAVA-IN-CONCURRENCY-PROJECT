package question_1;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import question_1.web_scraper.CrimeFeatureExtractor;
import question_1.web_scraper.ResultVisualizer;
import question_1.web_scraper.SubheadingExtractor;

public class WebScraper {
    private static final int THREAD_POOL_SIZE = 10;

    /// This stores the results from the search and extraction tasks in the ratio of
    /// the feature to frequency.
    private ConcurrentHashMap<String, Integer> sharedResults = new ConcurrentHashMap<>();

    /// To ensure only one method can update the sharedResults at a time, we use a
    /// ReentrantLock to synchronize access to the shared map
    private ReentrantLock lock = new ReentrantLock();

    /// ----------------------------------------------------------------------
    // This function runs the scraping task concurrently for a list of urls. it
    /// determines which type of task to run based on the isCrime flag.
    /// ----------------------------------------------------------------------
    /// It locks the sharedResults map to ensure thread safety, clears previous
    /// results, and then executes the appropriate extractor tasks in a thread pool.
    /// After all tasks are completed, it sorts the results, displays them in the
    /// terminal, and visualizes them using XChart.
    public void runScrapingConcurrentlyAndVisualize(List<String> urls,
            boolean isCrime) {

        try {
            lock.lock(); // Ensure only one thread can update sharedResults at a time
            sharedResults.clear(); // Clear previous results before starting new tasks

            // Create a fixed thread pool to run tasks concurrently
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (String url : urls) {
                // If isCrime is true, we run the CrimeFeatureExtractor; otherwise, we run the
                // SubheadingExtractor
                Runnable task = isCrime ? new CrimeFeatureExtractor(url, sharedResults)
                        : new SubheadingExtractor(url, sharedResults);
                executor.execute(task);
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to finish
            }

            Map<String, Integer> sortedResults = sortByValue(sharedResults);
            String title = isCrime ? "Crime Reporting Features" : "Deep Learning Paper Subheadings";
            // Sort and display results in terminal
            displayResults(sortedResults, title);

            // Visualize results using XChart
            if (isCrime) {
                ResultVisualizer.visualizeCrimeFeatures(sortedResults);
            } else {
                ResultVisualizer.visualizeSubheadings(sortedResults);
            }
        } catch (Exception e) {
            System.err.println("Error running concurrent tasks: " + e.getMessage());
        } finally {
            lock.unlock();
        }

    }

    /// This method analyzes crime reporting papers by performing a search for
    /// relevant URLs.
    public void analyzeCrimeReportingPapers() {

        // Step 1: perform a search from the query and retrieve urls
        List<String> searchResults = performSearch("crime reporting system features");

        // An executor service to run extraction tasks concurrently
        runScrapingConcurrentlyAndVisualize(searchResults, true);
    }

    /// This method analyzes deep learning papers by performing a search for
    /// relevant URLs.
    public void analyzeDeepLearningPapers() {
        // Step 1: Take user input and retrieve urls
        List<String> searchResults = performSearch("deep learning models journal");

        // An executor service to run extraction tasks concurrently
        runScrapingConcurrentlyAndVisualize(searchResults, false);
    }

    /// This method performs a search based on the query and returns a list of URLs
    /// to scrape. Using a custom hardcoded list of URLs temporarily until we
    /// implement actual web search functionality.
    private List<String> performSearch(String query) {
        List<String> urls = new ArrayList<>();

        if (query.contains("crime")) {

            urls.addAll(Arrays.asList(
                    "https://scholar.google.com/scholar?q=crime+reporting+system",
                    "https://arxiv.org/search/?query=crime+reporting&searchtype=all",
                    "https://www.semanticscholar.org/search?q=crime+reporting+system",
                    "https://www.mdpi.com/search?q=crime+reporting",
                    "https://link.springer.com/search?query=crime+reporting+system",
                    "https://dl.acm.org/search?expanded=crime+reporting+system",
                    "https://www.researchgate.net/search/publication?q=crime+reporting",
                    "https://www.sciencedirect.com/search?qs=crime%20reporting%20system",
                    "https://asistdl.onlinelibrary.wiley.com/action/doSearch?AllField=crime+reporting",
                    "https://academic.oup.com/search-results?page=1&q=crime+reporting"));
        } else {

            urls.addAll(Arrays.asList(
                    "https://scholar.google.com/scholar?q=deep+learning+models+journal",
                    "https://arxiv.org/search/?query=deep+learning+models&searchtype=all",
                    "https://www.semanticscholar.org/search?q=deep+learning+models",
                    "https://www.mdpi.com/search?q=deep+learning+models",
                    "https://link.springer.com/search?query=deep+learning+models",
                    "https://dl.acm.org/search?expanded=deep+learning+models",
                    "https://www.researchgate.net/search/publication?q=deep+learning+models",
                    "https://www.sciencedirect.com/search?qs=deep%20learning%20models",
                    "https://asistdl.onlinelibrary.wiley.com/action/doSearch?AllField=deep+learning+models",
                    "https://academic.oup.com/search-results?page=1&q=deep+learning+models"));
        }

        return new ArrayList<>(urls);
    }

    // Sorts the map by value in descending order
    private Map<String, Integer> sortByValue(ConcurrentHashMap<String, Integer> map) {
        // Convert to list for easy sorting
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        // Sort in descending order by value
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });

        // Improve access time while still maintaining order
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /// Formats the results in a readable way and prints to terminal. Only shows top
    /// 20 results
    private void displayResults(Map<String, Integer> results, String title) {
        System.out.println("\n--- " + title + " (Sorted by Frequency) ---");
        int count = 0;
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            System.out.printf("%-50s : %d%n", entry.getKey(), entry.getValue());
            count++;
            if (count >= 20)
                break; // Show top 20
        }
    }

    public static Document customDoc(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(15000) // Increased timeout
                .maxBodySize(0) // Handle large pages
                .followRedirects(true)
                .ignoreHttpErrors(true) // DON'T FAIL ON 403/404
                .get();
    }
}
