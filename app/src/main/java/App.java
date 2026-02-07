import java.util.Scanner;

import question_1.WebScraper;

public class App {
    public static void main(String[] args) {
        System.out.println("JAVA IN CONCURRENCY ASSIGNMENT");
        System.out.println("Select a question to run (1-5):");
        requestAndRunQuestion();

    }

    public static void requestAndRunQuestion() {
        Scanner scanner = new Scanner(System.in);
        final int input = scanner.nextInt();
        scanner.close();

        switch (input) {
            case 1:
                runQuestion1();
                break;

            default:
                System.out.println("Question not found. Select from question 1-5");
        }
    }

    /// Assignment 1
    public static void runQuestion1() {
        WebScraper scraper = new WebScraper();
        scraper.analyzeCrimeReportingPapers();
        scraper.analyzeDeepLearningPapers();
    }
}