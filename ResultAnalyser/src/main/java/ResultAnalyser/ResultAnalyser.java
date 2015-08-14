package ResultAnalyser;

import mloss.roc.Curve;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Shaun on 11/07/2015.
 */
public class ResultAnalyser {

    public static void main(String[] args) throws Exception {
        ResultLoader loader = new ResultLoader(new File("..\\WebpageClassifier\\Results"), "*.tsv");

        SortedSet<RocSeriesData> rocData = new TreeSet<RocSeriesData>();
        List<ErrorSeriesData> errorData = new ArrayList<ErrorSeriesData>();
        List<ErrorSeriesData> errorScoreData = new ArrayList<ErrorSeriesData>();
        List<ErrorSeriesData> scoreCountsData = new ArrayList<ErrorSeriesData>();
        List<ErrorSeriesData> scorePercentData = new ArrayList<ErrorSeriesData>();
        SortedSet<Result> allResults = new TreeSet<Result>();

        for (String resultSet : loader.getSets()) {
            List<Result> results = loader.getResults(resultSet);

            int[] actualLabels = new int[results.size()];
            double[] predictedLabels = new double[results.size()];

            for (int x = 0; x < results.size(); x++) {
                actualLabels[x] = results.get(x).isTrue() ? 1 : 0;
                predictedLabels[x] = results.get(x).getConfidence();
            }

            Curve rocAnalysis = new Curve.PrimitivesBuilder()
                    .predicteds(predictedLabels)
                    .actuals(actualLabels)
                    .build();
            // Calculate the AUC ROCs
            double area = rocAnalysis.rocArea();
            // Get the points for later plotting
            double[][] rawPoints = rocAnalysis.rocPoints();
            rocData.add(new RocSeriesData(area, rawPoints, resultSet));

            // calculate the error rates for scores
            ErrorSeriesData errorSeries = new ErrorSeriesData(resultSet);
            ErrorSeriesData errorScoreSeries = new ErrorSeriesData(resultSet);
            ErrorSeriesData scoreCountSeries = new ErrorSeriesData(resultSet);
            ErrorSeriesData scorePercentSeries = new ErrorSeriesData(resultSet);
            int xCount = 0;
            for(double q = 0.5; q < 1; q = q + 0.01) {
                double totalTP = 0;
                double totalTN = 0;
                double scoreTP = 0;
                double scoreTN = 0;
                double total = 0;
                for (Result r : results) {
                    if (r.getConfidence() > q) {
                        if (r.isTrue()) {
                            totalTP = totalTP + 1;
                        } else {
                            totalTN = totalTN + 1;
                        }
                    }
                    if (r.getConfidence() >= q && r.getConfidence() < q + 0.01) {
                        if (r.isTrue()) {
                            scoreTP = scoreTP + 1;
                        } else {
                            scoreTN = scoreTN + 1;
                        }
                    }
                    total = total + 1;
                }
                double errorRate = (totalTN / (totalTN + totalTP)) * 100;
                double scoreErrorRate = (scoreTP / (scoreTN + scoreTP)) * 100;

                //System.out.print("Error Rate @" + q + ": " + scoreErrorRate + " " + (scoreTN + scoreTP) + "\r\n");

                errorSeries.addPoint(String.format("%.2f", q), errorRate);
                errorScoreSeries.addPoint(String.format("%.2f", q), scoreErrorRate);
                scoreCountSeries.addPoint(String.format("%.2f", q), (scoreTN + scoreTP));
                scorePercentSeries.addPoint(String.format("%.2f", q), (totalTN + totalTP) / total * 100);
            }
            errorData.add(errorSeries);
            errorScoreData.add(errorScoreSeries);
            scoreCountsData.add(scoreCountSeries);
            scorePercentData.add(scorePercentSeries);

            // write sorted results
            Collections.sort(results);
            FileWriter fr = new FileWriter("Sorted_" + resultSet + ".tsv");
            for (Result r : results) {
                if(allResults.contains(r) == false) {
                    allResults.add(r);
                }
                fr.write(r.getConfidence() + "\t" + (r.isTrue() ? 1 : 0) + "\t" + r.getLabel() + "\r\n");
            }
            fr.close();
        }

        // save some of the TN for review
        File dataPath = new File("Charts");
        if(dataPath.exists() == false) {
            dataPath.mkdirs();
        }
        FileWriter fr = new FileWriter("Charts\\Sorted_TN.tsv");
        int tnCount = 0;
        for(Result r: allResults) {

            if(r.isTrue() == false) {

                fr.write(r.getItemClass() + "\t" + r.getConfidence() + "\t" + r.getLabel() + "\r\n");
                tnCount++;
            }

            if(tnCount > 50) {
                break;
            }
        }
        fr.close();

        new PNGCharter().chartRoc(rocData);
        new WebCharter().chartRoc(rocData);
        new WebCharter().chartError(errorData, "results_error.json");
        new WebCharter().chartError(errorScoreData, "results_error_score.json");
        new WebCharter().chartError(scoreCountsData, "results_score_count.json");
        new WebCharter().chartError(scorePercentData, "results_score_percent.json");
        //new WebCharter().chartDistribution(allResults);
    }

    public static class DataPoint {
        private String label;
        private Double value;
        public DataPoint(String label, Double value) {
            this.label = label;
            this.value = value;
        }
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
        public void setValue(Double value) {
            this.value = value;
        }
        public Double getValue() {
            return value;
        }
    }

    public static class ErrorSeriesData {

        private String label;
        private List<DataPoint> data;

        public ErrorSeriesData(String label) {
            this.label = label;
            this.data = new ArrayList<DataPoint>();
        }

        public String getLabel() {
            return label;
        }

        public void addPoint(String pointLabel, Double value) {
            data.add(new DataPoint(pointLabel, value));
        }

        public List<DataPoint> getData() {
            return data;
        }
    }

    public static class RocSeriesData implements Comparable<RocSeriesData> {
        private double area;
        private double[][] rawPoints;
        private String label;

        public RocSeriesData(double area, double[][] rawPoints, String label) {
            this.area = area;
            this.rawPoints = rawPoints;
            this.label = label;
        }

        public double getArea() {
            return area;
        }

        public double[][] getRawPoints() {
            return rawPoints;
        }

        public String getLabel() {
            return label;
        }

        public int compareTo(ResultAnalyser.RocSeriesData other) {
            return (int) (other.area * 1000) - (int) (this.area * 1000);
        }
    }
}
