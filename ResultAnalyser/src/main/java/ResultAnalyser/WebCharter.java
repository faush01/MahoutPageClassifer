package ResultAnalyser;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jfisher on 29/07/15.
 */
public class WebCharter implements Charter {

    public void chartDistribution(Set<Result> data) throws IOException {

        List<Map<String, Object>> chartData = new ArrayList<Map<String, Object>>();

        for (Result d : data) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("label", d.getLabel());

            double score = d.getConfidence();
            //if(d.isTrue() == false) {
            //    score = score * -1;
            //}
            row.put("value", score);
            chartData.add(row);
        }

        FileWriter fr = new FileWriter("Charts\\results_distribution.json");
        new ObjectMapper().writeValue(fr, chartData);
        fr.close();
    }

    public void chartError(List<ResultAnalyser.ErrorSeriesData> data, String fileName) throws IOException {

        Map<String, List<Map<String, Object>>> chartData = new HashMap<String, List<Map<String, Object>>>();

        for (ResultAnalyser.ErrorSeriesData d : data) {

            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

            for (ResultAnalyser.DataPoint point : d.getData()) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put("label", point.getLabel());
                row.put("value", point.getValue());
                rows.add(row);
            }

            chartData.put(d.getLabel(), rows);
        }

        FileWriter fr = new FileWriter("Charts\\" + fileName);
        new ObjectMapper().writeValue(fr, chartData);
        fr.close();
    }

    public void chartRoc(Set<ResultAnalyser.RocSeriesData> data) throws IOException {

        DecimalFormat df = new DecimalFormat("##");
        Map<String, List<Map<String, Double>>> xyData = new HashMap<String, List<Map<String, Double>>>();

        for (ResultAnalyser.RocSeriesData d : data) {
            List<Map<String, Double>> rows = new ArrayList<Map<String, Double>>();
            xyData.put("(" + df.format(d.getArea() * 100) + "%) " + d.getLabel(), rows);

            for (double[] point : d.getRawPoints()) {
                Map<String, Double> row = new HashMap<String, Double>();
                row.put("x", point[0]);
                row.put("y", point[1]);
                rows.add(row);
            }
        }

        FileWriter fr = new FileWriter("Charts\\results_roc.json");
        new ObjectMapper().writeValue(fr, xyData);
        fr.close();
    }
}
