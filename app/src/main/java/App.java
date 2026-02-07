
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    public static void main(String[] args) {

        String[] crimeLinks = { "https://example-crime-system.com" };

        // Create a thread pool of 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(crimeLinks.length);

        for (String url : crimeLinks) {
            executor.execute(new WebScraper());
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            /* Wait for threads to finish */ }

    }
}