package question_1.web_scraper;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import question_1.SearchEngineAnalyzer;

import java.util.concurrent.ConcurrentHashMap;

// CRIME FEATURE EXTRACTOR CLASS
public class CrimeFeatureExtractor implements Runnable {
    private String url;
    private ConcurrentHashMap<String, Integer> sharedResults;

    // 11 distinctive features of crime-reporting systems
    private static final String[] CRIME_FEATURES = {
            "anonymous reporting",
            "real-time alerts",
            "geolocation tracking",
            "incident mapping",
            "mobile-app",
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

            final Document doc = SearchEngineAnalyzer.extractDocument(url);
            saveResults(doc); // Save results after scraping

            System.out.println("Processed: " + url);

        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + url);
        } catch (Exception e) {
            System.err.println("Error processing " + url + ": " + e.getMessage());
        }
    }

    /// It checks if each feature is in the extracted text and updates the shared
    /// map.
    private void saveResults(Document doc) {
        // Extract text from relevant sections
        String fullText = extractUsefulTexts(doc).toLowerCase();

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
    }

    /// It extracts text from various sections of the web page, such as abstracts,
    /// articles, paragraphs, tables, and lists.
    private String extractUsefulTexts(Document doc) {
        // Using StringBuilder to improve performance when concatenating strings
        StringBuilder text = new StringBuilder();

        // Extract from abstract, article contents, and paragraphs
        Elements allElements = doc.select(
                "abstract, .abstract, #abstract, article, .article-content, .paper-content, p, section, table, ul, ol");

        for (Element elem : allElements)
            text.append(elem.text()).append(" ");

        return text.toString();
    }

    /// Checks if the provided text contains a specified feature.
    private boolean containsFeature(String text, String feature) {
        // Checks if the text in a possibly different form is in the text
        final boolean isPartOfFeatures = text.contains(feature) || text.contains(feature.replace(" ", "-"))
                || text.contains(feature.replace(" ", "_"));

        if (isPartOfFeatures)
            return true;
        return false; // else return false
    }
}