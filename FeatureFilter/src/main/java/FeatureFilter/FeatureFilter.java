package FeatureFilter;

import jdk.nashorn.internal.runtime.ECMAException;

import java.io.*;
import java.util.*;

/**
 * Created by Shaun on 9/07/2015.
 */
public class FeatureFilter {

    public static void main(String[] args) throws Exception {

        Map<String, FeatureCounts> featureCounts = new HashMap<String, FeatureCounts>();

        Map<String, SiteInfo> sites = loadSiteList("..\\FeatureExtractor\\ExtractedFeatures\\SiteList.tsv");

        Set<String> sitesClass01 = new HashSet<String>();
        Set<String> sitesClass02 = new HashSet<String>();

        FileReader fr = new FileReader("..\\FeatureExtractor\\ExtractedFeatures\\Features.tsv");
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while(line != null) {

            String[] tokens = line.split("\t");

            String siteId = tokens[0];
            String feature = tokens[1];
            int docCount = Integer.parseInt(tokens[2]);

            SiteInfo info = sites.get(siteId);

            if(info.itemClass.equals("0")) {
                if(sitesClass01.contains(siteId) == false) {
                    sitesClass01.add(siteId);
                }
            }
            else {
                if(sitesClass02.contains(siteId) == false) {
                    sitesClass02.add(siteId);
                }
            }

            FeatureCounts counts = featureCounts.get(feature);
            if(counts == null) {
                counts = new FeatureCounts();
                counts.docCountClass01 = 0;
                counts.docCountClass02 = 0;
                counts.totalCountClass01 = 0;
                counts.totalCountClass02 = 0;
                featureCounts.put(feature, counts);
            }

            if (info.itemClass.equals("0")) {
                counts.totalCountClass01 = counts.totalCountClass01 + docCount;
                counts.docCountClass01 = counts.docCountClass01 + 1;
            }
            else {
                counts.totalCountClass02 = counts.totalCountClass02 + docCount;
                counts.docCountClass02 = counts.docCountClass02 + 1;
            }

            line = br.readLine();
        }

        br.close();
        fr.close();

        List<FeatureInfo> weightedFeatures = new LinkedList<FeatureInfo>();

        int class01SiteCount = sitesClass01.size();
        int class02SiteCount = sitesClass02.size();

        // extract the features
        //FileWriter fw = new FileWriter("FeatureWeights\\FeatureWeights.tab");
        for(String feature: featureCounts.keySet()) {

            FeatureCounts counts = featureCounts.get(feature);

            /*
            double tf_idf_01 =  Math.log10(class01SiteCount / (Double.MIN_VALUE + (double) counts.totalCountClass01));
            tf_idf_01 = tf_idf_01 * (double)counts.totalCountClass01;
            if(Double.isNaN(tf_idf_01)) {
                tf_idf_01 = Double.MIN_VALUE;
            }

            double tf_idf_02 =  Math.log10(class02SiteCount / (Double.MIN_VALUE + counts.totalCountClass02));
            tf_idf_02 = tf_idf_02 * (double)counts.totalCountClass02;
            if(Double.isNaN(tf_idf_02)) {
                tf_idf_02 = Double.MIN_VALUE;
            }

            double weight = ( (double)Math.abs(tf_idf_01 - tf_idf_02) / ((double)(tf_idf_01 + tf_idf_02) / 2) ) / 2;
            */

            int V1 = counts.totalCountClass01;
            int V2 = counts.totalCountClass02;

            //double weight01 = Math.log10((double)Math.abs(V1 - V2) + 1);
            double weight02 = ( (double)Math.abs(V1 - V2) / ((double)(V1 + V2) / 2) ) / 2;
            //double weight = weight01 * weight02;
            double weight = weight02;

            if(V1 == 1 && V2 == 0) {
                weight = 0;
            }
            if(V2 == 1 && V1 == 0) {
                weight = 0;
            }

            //fw.write(feature + "\t" + V1 + "\t" + V2 + "\t" + weight + "\r\n");

            FeatureInfo fi = new FeatureInfo();
            fi.count01 = V1;
            fi.count02 = V2;
            fi.weight = weight;
            fi.feature = feature;

            weightedFeatures.add(fi);
        }

        //fw.close();

        weightedFeatures.sort(new FeatureSorter());

        File dataPath = new File("FeatureWeights");
        if(dataPath.exists() == false) {
            dataPath.mkdirs();
        }
        FileWriter fw = new FileWriter("FeatureWeights\\FeatureWeights.tsv");
        for(FeatureInfo fi: weightedFeatures) {

            fw.write(fi.feature + "\t" + fi.count01 + "\t" + fi.count02 + "\t" + fi.weight + "\r\n");

        }
        fw.close();

    }

    private static Map<String, SiteInfo> loadSiteList(String path) throws Exception {

        Map<String, SiteInfo> sites = new HashMap<String, SiteInfo>();

        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while(line != null) {

            String[] tokens = line.split("\t");

            SiteInfo info = new SiteInfo();
            info.id = tokens[0];
            info.url = tokens[1];
            info.itemClass = tokens[2];

            sites.put(info.id, info);
            line = br.readLine();
        }

        br.close();
        fr.close();

        return sites;
    }

}
