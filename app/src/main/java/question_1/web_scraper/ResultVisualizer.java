package question_1.web_scraper;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
import java.util.Map;

public class ResultVisualizer {

    public static void visualizeCrimeFeatures(Map<String, Integer> features) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(1000).height(600)
                .title("Crime Reporting Features Distribution")
                .xAxisTitle("Feature")
                .yAxisTitle("Number of Systems")
                .theme(ChartTheme.XChart)
                .build();

        chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
        chart.getStyler().setXAxisLabelRotation(45);

        // Convert top 15 entries
        features.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .forEach(e -> chart.addSeries(e.getKey(),
                        java.util.Arrays.asList(e.getKey()),
                        java.util.Arrays.asList(e.getValue())));

        new SwingWrapper<>(chart).displayChart();
    }

    public static void visualizeSubheadings(Map<String, Integer> subheadings) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(1000).height(700)
                .title("Common Subheadings in Deep Learning Papers")
                .xAxisTitle("Subheading")
                .yAxisTitle("Frequency")
                .theme(ChartTheme.XChart)
                .build();

        chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
        chart.getStyler().setXAxisLabelRotation(45);

        subheadings.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .forEach(e -> chart.addSeries(e.getKey(),
                        java.util.Arrays.asList(e.getKey()),
                        java.util.Arrays.asList(e.getValue())));

        new SwingWrapper<>(chart).displayChart();
    }
}
