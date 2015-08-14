package WebpageClassifier.DataObjects;

import org.apache.commons.collections.map.HashedMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Shaun on 10/07/2015.
 */
public class SiteDataLoader {

    private Map<String, SiteInfo> sites = new HashMap<String, SiteInfo>();
    private Map<String, Double> featureWeights = new HashMap<String, Double>();
    private Map<String, SiteFeatures> siteData = new HashMap<String, SiteFeatures>();

    public SiteDataLoader() throws Exception {

        loadSiteData("..\\FeatureExtractor\\ExtractedFeatures\\SiteList.tsv");
        loadFeatureWeights("..\\FeatureFilter\\FeatureWeights\\FeatureWeights.tsv");

        LoadData();

    }

    public List<SiteFeatures> getSiteData() {

        List<SiteFeatures> siteList = new ArrayList<SiteFeatures>();

        for(SiteFeatures site: siteData.values()) {
            siteList.add(site);
        }

        return siteList;
    }

    public int getFeatureSetSize() {
        return featureWeights.keySet().size();
    }

    public void LoadData() throws Exception {

        String path = "..\\FeatureExtractor\\ExtractedFeatures\\Features.tsv";
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while(line != null) {
            String[] tokens = line.split("\t");

            String siteId = tokens[0];
            String feature = tokens[1];

            int itemClass = Integer.parseInt(sites.get(siteId).itemClass);

            SiteFeatures featues = siteData.get(siteId);
            if(featues == null) {
                String siteUrl = sites.get(siteId).url;
                featues = new SiteFeatures(siteId, siteUrl, itemClass, getFeatureSetSize());
                siteData.put(siteId, featues);
            }

            Double weight = featureWeights.get(feature);
            if(weight == null) {
                throw new Exception("Weight for feature not found : " + feature);
            }

            if(weight > 0) {
                featues.addFeature(feature, weight);
            }

            line = br.readLine();
        }

        br.close();
        fr.close();

    }

    private void loadFeatureWeights(String path) throws Exception {

        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while(line != null) {
            String[] tokens = line.split("\t");

            String feature = tokens[0];
            String class_01 = tokens[1];
            String class_02 = tokens[2];
            Double weight = Double.parseDouble(tokens[3]);
            featureWeights.put(feature, weight);

            line = br.readLine();
        }

        br.close();
        fr.close();
    }

    private void loadSiteData(String path) throws Exception {

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
    }
}
