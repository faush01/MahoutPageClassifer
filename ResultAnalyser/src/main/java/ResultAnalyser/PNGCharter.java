package ResultAnalyser;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by jfisher on 29/07/15.
 */
public class PNGCharter implements Charter {

    public void chartDistribution(Set<Result> data) throws IOException {}
    public void chartError(java.util.List<ResultAnalyser.ErrorSeriesData> data, String fileName) throws IOException {}

    public void chartRoc(Set<ResultAnalyser.RocSeriesData> data) throws IOException {

        XYSeriesCollection xyCollection = new XYSeriesCollection();

        for (ResultAnalyser.RocSeriesData d : data) {
            XYSeries rocData = new XYSeries(d.getLabel() + " - " + d.getArea());

            for (double[] cord : d.getRawPoints()) {

                double x = cord[0];
                double y = cord[1];
                rocData.add(x, y);
            }

            xyCollection.addSeries(rocData);
        }
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                "ROC",
                "TN",
                "TP",
                xyCollection,
                PlotOrientation.VERTICAL,
                true, true, false);
        //xylineChart.setBackgroundPaint(null);
        //xylineChart.setBackgroundImageAlpha(0.0f);
        //xylineChart.getPlot().setBackgroundImage(null);
        xylineChart.getPlot().setBackgroundPaint(new Color(240, 240, 240));
        //xylineChart.getPlot().setBackgroundImageAlpha(0.0f);

        int width = 1280; /* Width of the image */
        int height = 960; /* Height of the image */
        File XYChart = new File("ROC.png");
        ChartUtilities.saveChartAsPNG(XYChart, xylineChart, width, height);
    }
}
