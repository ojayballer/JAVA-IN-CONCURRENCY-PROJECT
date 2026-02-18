package question_1;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    public void runScrapingConcurrentlyAndVisualize(List<SearchEngineResult> results,
            boolean isCrime) {

        try {
            lock.lock(); // Ensure only one thread can update sharedResults at a time
            sharedResults.clear(); // Clear previous results before starting new tasks

            // Create a fixed thread pool to run tasks concurrently
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (SearchEngineResult result : results) {
                // If isCrime is true, we run the CrimeFeatureExtractor; otherwise, we run the
                // SubheadingExtractor
                Runnable task;
                if (isCrime) {
                    task = new CrimeFeatureExtractor(result.getUrl(), sharedResults);
                } else {
                    task = new SubheadingExtractor(result.getUrl(), sharedResults);
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
        System.out.println("Performing crime search");
        // Step 1: perform a search from the query and retrieve urls
        List<SearchEngineResult> searchResults = performSearch("crime-reporting papers");

        // List<String> searchResults = performSearch("crime-reporting papers");

        // An executor service to run extraction tasks concurrently
        runScrapingConcurrentlyAndVisualize(searchResults, true);
    }

    /// This method analyzes deep learning papers by performing a search for
    /// relevant URLs.
    public void analyzeDeepLearningPapers() {
        // Step 1: Take user input and retrieve urls
        List<SearchEngineResult> searchResults = performSearch("deep learning models journal");
        System.out.println("Performing deep learning models search");
        // List<String> searchResults = performSearch("deep learning models journal");

        // An executor service to run extraction tasks concurrently
        runScrapingConcurrentlyAndVisualize(searchResults, false);
    }

    private List<SearchEngineResult> performSearch(String query) {
        List<SearchEngineResult> results = new ArrayList<>();

        String url = "https://google.serper.dev/search";

        try {

            // Execute POST request via Jsoup
            String response = Jsoup.connect(url)
                    .header("X-API-KEY", "754fd1e768094fd51e443d72a2ebe729eb65a4d4")
                    .header("Content-Type", "application/json")
                    .requestBody("{\"q\": \"" + query + "\"}")
                    .ignoreContentType(true)
                    .method(org.jsoup.Connection.Method.POST)
                    .execute()
                    .body();

            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

            if (jsonObject.has("organic")) {
                JsonArray organicResults = jsonObject.getAsJsonArray("organic");

                for (int i = 0; i < organicResults.size(); i++) {
                    JsonObject result = organicResults.get(i).getAsJsonObject();

                    String title = result.has("title") ? result.get("title").getAsString() : "";
                    String link = result.has("link") ? result.get("link").getAsString() : "";
                    String snippet = result.has("snippet") ? result.get("snippet").getAsString() : "";

                    if (!title.isEmpty()) {
                        results.add(new SearchEngineResult(title, link, snippet));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

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