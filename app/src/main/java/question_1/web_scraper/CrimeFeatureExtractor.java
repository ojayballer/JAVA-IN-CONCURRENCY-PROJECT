package question_1.web_scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import question_1.WebScraper;

import java.util.concurrent.ConcurrentHashMap;

/// DEFINITIONS AND DECLARATIONS UNDER THE CLASS
/// - The `CrimeFeatureExtractor` class implements `Runnable`, allowing it to be executed by a thread.
/// - It has a constructor that takes a URL and a shared `ConcurrentHashMap` to store results.
/// - The `CRIME_FEATURES` array contains the 11 distinctive features of crime-reporting systems that we want to check for in the web pages.
/// - The `run` method is the entry point for the thread, where it connects to the URL, extracts relevant text, 
//      and checks for the presence of each feature, updating the shared map accordingly.

/// ALGORITHM ON HOW THE BELOW CLASS WORKS
/// 1. The `run` method starts by adding a random delay to reduce the chances of being rate-limited by the server.
/// 2. It then uses Jsoup to connect to the given URL, setting a user agent and timeout, and follows redirects if necessary.
/// 3. The `extractRelevantText` method is called to gather text from various sections
///   of the page, such as abstracts, articles, paragraphs, tables, and lists.
/// 4. The extracted text is converted to lowercase for case-insensitive searching.
/// 5. The method iterates over the `CRIME_FEATURES` array, checking
///   if each feature is present in the extracted text using the `containsFeature` method, which checks for multiple variants of the feature name.
/// 6. If a feature is found, the shared `ConcurrentHashMap` is updated in a thread-safe manner to count the occurrences of each feature across all processed pages.
/// 7. The method handles exceptions gracefully, printing error messages if any issues arise during the connection or processing of the page.
/// 8. Finally, it prints a success message for each processed URL.

public class CrimeFeatureExtractor implements Runnable {
    private String url;
    private ConcurrentHashMap<String, Integer> sharedResults;

    // 11 distinctive features of crime-reporting systems
    private static final String[] CRIME_FEATURES = {
            "anonymous reporting",
            "real-time alerts",
            "geolocation tracking",
            "incident mapping",
            "mobile app",
            "digital evidence",
            "encryption",
            "authentication",
            "dashboard analytics",
            "case management",
            "data visualization",
    };

    public CrimeFeatureExtractor(String url, ConcurrentHashMap<String, Integer> sharedResults) {
        this.url = url;
        this.sharedResults = sharedResults;
    }

    @Override
    public void run() {
        try {
            // Add artificial delay to reduce rate-limiting issues
            Thread.sleep((long) (Math.random() * 2000 + 1000));

            // Extract text from relevant sections
            String fullText = extractUsefulTexts(WebScraper.customDoc(url)).toLowerCase();

            // Check for each feature and update shared map
            for (String feature : CRIME_FEATURES) {
                if (containsFeature(fullText, feature)) {
                    // ConcurrentHashMap handles synchronization internally
                    Integer oldValue = sharedResults.putIfAbsent(feature, 1);
                    if (oldValue != null) {
                        sharedResults.put(feature, oldValue + 1);
                    }
                }
            }

            System.out.println("✓ Processed: " + url);

        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + url);
            Thread.currentThread().interrupt(); // Restore interrupt status
        } catch (Exception e) {
            System.err.println("✗ Error processing " + url + ": " + e.getMessage());
        }
    }

    /// It extracts text from various sections of the web page, such as abstracts,
    /// articles, paragraphs, tables, and lists.
    private String extractUsefulTexts(Document doc) {
        // Using StringBuilder to improve performance when concatenating strings
        StringBuilder text = new StringBuilder();

        // Extract from abstract, article contents, and paragraphs
        Elements abstracts = doc.select("abstract, .abstract, #abstract");
        for (Element elem : abstracts) {
            text.append(elem.text()).append(" ");
        }

        Elements articles = doc.select("article, .article-content, .paper-content");
        for (Element elem : articles) {
            text.append(elem.text()).append(" ");
        }

        Elements paragraphs = doc.select("p, section");
        for (Element elem : paragraphs) {
            text.append(elem.text()).append(" ");
        }

        // Also check tables and lists
        Elements tables = doc.select("table, ul, ol");
        for (Element elem : tables) {
            text.append(elem.text()).append(" ");
        }

        return text.toString();
    }

    /// Checks if the provided text contains a specified feature.
    private boolean containsFeature(String text, String feature) {

        if (text.contains(feature)) {
            return true;
        }
        if (text.contains(feature.replace(" ", "-"))) {
            return true;
        }
        if (text.contains(feature.replace(" ", "_"))) {
            return true;
        }
        return false;
    }
}
