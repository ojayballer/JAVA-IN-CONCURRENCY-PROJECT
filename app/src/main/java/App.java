import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import question_1.SearchEngineAnalyzer;
import question_3.Board;

public class App {
    public static void main(String[] args) {
        // System.out.println("JAVA IN CONCURRENCY ASSIGNMENT");
        // System.out.println("Select a question to run (1-5):");
        System.out.println("JAVA IN CONCURRENCY ASSIGNMENT");
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
        SearchEngineAnalyzer serAnalyzer = new SearchEngineAnalyzer();
        serAnalyzer.analyzeCrimeReportingPapers();
        serAnalyzer.analyzeDeepLearningPapers();
    }
}

class Test {
    public static Document tryFetch() throws Exception {
        Document doc = Jsoup.connect("https://html.duckduckgo.com/html/")
                .data("q", "java gradle search")
                .post();
        return doc;
    }
}