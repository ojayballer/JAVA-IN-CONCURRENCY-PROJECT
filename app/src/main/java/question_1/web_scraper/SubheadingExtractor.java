package question_1.web_scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import question_1.WebScraper;

import java.util.concurrent.ConcurrentHashMap;

public class SubheadingExtractor implements Runnable {
    private String url;
    private ConcurrentHashMap<String, Integer> sharedResults;

    public SubheadingExtractor(String url, ConcurrentHashMap<String, Integer> sharedResults) {
        this.url = url;
        this.sharedResults = sharedResults;
    }

    @Override
    public void run() {
        try {
            Thread.sleep((long) (Math.random() * 2000 + 1000));

            final Document doc = WebScraper.customDoc(url);
            Elements headings = doc.select("h2, h3, h4, .section-title, .heading");

            for (Element heading : headings) {
                String text = heading.text().trim();

                if (isValidSubheading(text)) {
                    String normalized = normalizeSubheading(text);

                    // Thread-safe update using ConcurrentHashMap
                    Integer oldValue = sharedResults.putIfAbsent(normalized, 1);
                    if (oldValue != null) {
                        sharedResults.put(normalized, oldValue + 1);
                    }
                }
            }

            System.out.println("Extracted subheadings from: " + url);

        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + url);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error extracting from " + url + ": " + e.getMessage());
        }
    }

    private boolean isValidSubheading(String text) {
        // Basic checks to filter out misleading headings
        return text.length() > 3
                && text.length() < 100
                && !text.toLowerCase().startsWith("copyright")
                && !text.toLowerCase().contains("cookie")
                && !text.matches("^[0-9.]+$");
    }

    private String normalizeSubheading(String text) {
        return text.
        // Remove all leading numbers, dots and whitespace.
                replaceAll("^[0-9.\\s]+", "")
                // Replace multiple spaces with a single space and trim.
                .replaceAll("\\s+", " ")
                // Trim leading and ending whitespace.
                .trim();
    }
}
