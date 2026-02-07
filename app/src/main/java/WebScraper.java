
public class WebScraper implements Runnable {
    public WebScraper() {
    }

    @Override
    public void run() {
        try {
            System.out.println("Scraping URL: ");
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println("Sleep exception");
            }
        } catch (Exception e) {
            System.out.println("Error scraping URL: " + e.getMessage());
        }
    }
}