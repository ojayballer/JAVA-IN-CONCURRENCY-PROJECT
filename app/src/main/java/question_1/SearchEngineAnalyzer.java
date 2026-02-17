package question_1;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import question_1.web_scraper.*;

public class SearchEngineAnalyzer {
    private static final int THREAD_POOL_SIZE = 10;

    /// This stores the results from the search and extraction tasks in the ratio of
    /// the feature to frequency.
    private ConcurrentHashMap<String, Integer> sharedResults = new ConcurrentHashMap<>();

    /// To ensure only one method can update the sharedResults at a time,we use a
    /// ReentrantLock to synchronize access to the shared map.
    private ReentrantLock lock = new ReentrantLock();

    // This function runs the scraping task concurrently for a list of urls. it
    /// determines which type of task to run based on the isCrime flag.
    /// It locks the sharedResults map to ensure thread safety, clears previous
    /// results, and then executes the appropriate extractor tasks in a thread pool.
    /// After all tasks are completed, it sorts the results, displays them in the
    /// terminal, and visualizes them using XChart.
    public void runScrapingConcurrentlyAndVisualize(List<String> results,
            boolean isCrime) {

        try {
            lock.lock(); // Ensure only one thread can update sharedResults at a time
            sharedResults.clear(); // Clear previous results before starting new tasks

            // Create a fixed thread pool to run tasks concurrently
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (String result : results) {
                // If isCrime is true, we run the CrimeFeatureExtractor; otherwise, we run the
                // SubheadingExtractor
                Runnable task;
                if (isCrime) {
                    task = new CrimeFeatureExtractor(result, sharedResults);
                } else {
                    task = new SubheadingExtractor(result, sharedResults);
                }
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
        // List<SearchEngineResult> searchResults = performSearch("crime-reporting
        // papers");
        List<String> searchResults = performSearch("crime-reporting papers");

        // An executor service to run extraction tasks concurrently
        runScrapingConcurrentlyAndVisualize(searchResults, true);
    }

    /// This method analyzes deep learning papers by performing a search for
    /// relevant URLs.
    public void analyzeDeepLearningPapers() {
        // Step 1: Take user input and retrieve urls
        // List<SearchEngineResult> searchResults = performSearch("deep learning models
        // journal");
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
                    "https://arxiv.org/search/?query=crime+reporting+system&searchtype=all",
                    "https://www.semanticscholar.org/search?q=crime+reporting+system",
                    "https://ieeexplore.ieee.org/search/searchresult.jsp?queryText=crime+reporting+system",
                    "https://link.springer.com/search?query=crime+reporting+system",
                    "https://www.base-search.net/Search/Results?lookfor=crime+reporting+system",
                    "https://core.ac.uk/search?q=crime+reporting+system",
                    "https://doaj.org/search/articles?source=%7B%22query%22%3A%7B%22query_string%22%3A%7B%22query%22%3A%22crime%20reporting%20system%22%7D%7D%7D",
                    "https://app.dimensions.ai/discover/publication?search_text=crime%20reporting%20system",
                    "https://www.lens.org/lens/search/scholar/list?q=crime+reporting+system"));
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

    // /// This method builds a tailored search query by appending specific keywords
    // to
    // /// specifically select non-blocking rate limiting websites
    // private String buildTailoredQuery(String query) {
    // String academicSites = "(site:scholar.google.com OR " +
    // "site:arxiv.org OR " +
    // "site:springer.com OR " +
    // "site:sciencedirect.com OR " +
    // "site:semanticscholar.org OR " +
    // "site:researchgate.net OR " +
    // "site:dl.acm.org OR " +
    // "site:mdpi.com OR " +
    // "site:wiley.com OR " +
    // "site:oup.com)";

    // return query + " research paper journal " + academicSites;
    // }

    // // This method performs a search using DuckDuckGo and extracts the title,
    // // URL,and description for each result.
    // private List<SearchEngineResult> performSearch(String query) {
    // List<SearchEngineResult> results = new ArrayList<>();

    // try {
    // String formattedQuery = URLEncoder.encode(buildTailoredQuery(query),
    // "UTF-8");

    // String url = "https://html.duckduckgo.com/html/?q=" + formattedQuery;
    // Document doc = SearchEngineAnalyzer.extractDocument(url);

    // Elements searchResults = doc.select(".result");
    // for (Element res : searchResults) {
    // String title = res.select(".result__title").text();
    // String link = res.select(".result__url").attr("href");
    // String snippet = res.select(".result__snippet").text();

    // if (!title.isEmpty() && !link.isEmpty()) {
    // results.add(new SearchEngineResult(title, link, snippet));
    // }
    // }

    // } catch (Exception e) {
    // System.err.println("Search error: " + e.getMessage());
    // }

    // return results;
    // }

    // Sorts the map by value in descending order and returns a new LinkedHashMap to
    // maintain the sorted order.
    private Map<String, Integer> sortByValue(ConcurrentHashMap<String, Integer> map) {
        // Convert to list for easy sorting
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        // Sort in descending order by value
        Collections.sort(list, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Improve access time while still maintaining order
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list)
            sortedMap.put(entry.getKey(), entry.getValue());

        return sortedMap;
    }

    /// Formats the results in a readable way and prints to terminal. Shows top 20.
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

    /// Extracts the HTML document from the given URL using Jsoup
    public static Document extractDocument(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .get();
    }
}