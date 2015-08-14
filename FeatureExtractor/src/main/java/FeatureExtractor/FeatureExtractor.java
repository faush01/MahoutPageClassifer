package FeatureExtractor;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.compound.DictionaryCompoundWordTokenFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by Shaun on 9/07/2015.
 */
public class FeatureExtractor {

    static EnglishAnalyzer en_an = null;
    static Set<String> dictionary = new HashSet<String>();

    public static void main(String[] args) throws Exception {

        dictionary = loadSpellingDic("Dictionary\\dictionary.txt");

        CharArraySet stopWords = loadStopWordsSet("Dictionary\\StopWords.txt");
        en_an = new EnglishAnalyzer(stopWords);

        File dataPath = new File("ExtractedFeatures");
        if(dataPath.exists() == false) {
            dataPath.mkdirs();
        }
        FileWriter fr = new FileWriter("ExtractedFeatures\\Features.tsv");
        List<SiteInfo> siteList = loadSiteList("C:\\Development\\SiteLoader\\WebSiteDataSet\\SiteDataSet.csv");

        saveSiteList("ExtractedFeatures\\SiteList.tsv", siteList);

        String baseDataPath = "C:\\Development\\SiteLoader\\SiteDataStore\\";

        for(SiteInfo site : siteList) {

            String siteUrl = site.url.replaceAll("/", "_");
            File pageDataPath = new File(baseDataPath + siteUrl + ".html");
            if(pageDataPath.exists()) {
                String pageData = loadPageData(pageDataPath.getAbsolutePath());

                //List<String> features = getSiteFeatures01(pageData);
                Map<String, FeatureCounter> features = getSiteFeatures02(pageData);

                for(String feature: features.keySet()) {
                    fr.write(site.id + "\t" + feature + "\t" + features.get(feature).get() + "\r\n");
                }
            }
        }

        fr.close();
    }
    private static Set<String> loadSpellingDic(String path) throws Exception {

        Set<String> dic = new HashSet<String>();

        FileInputStream fis = new FileInputStream(path);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String line = br.readLine();
        while(line != null) {
            dic.add(line);
            line = br.readLine();
        }

        br.close();
        isr.close();
        fis.close();

        return dic;
    }

    private static CharArraySet loadStopWordsSet(String path) throws Exception {

        CharArraySet stopWords = new CharArraySet(0, true);

        FileInputStream fis = new FileInputStream(path);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String line = br.readLine();
        while(line != null) {
            stopWords.add(line);
            line = br.readLine();
        }

        br.close();
        isr.close();
        fis.close();

        return stopWords;
    }

    private static Map<String, FeatureCounter> getSiteFeatures02(String pageData) throws Exception{

        Document doc = Jsoup.parse(pageData);

        String title = doc.title();
        String head = "";
        if(doc.head() != null) {
            head = doc.head().text();
        }
        String body = "";
        if(doc.body() != null) {
            body = doc.body().text();
        }
        //String meta = doc.getElementsByTag("meta").text();

        Map<String, FeatureCounter> featureList = new HashMap<String, FeatureCounter>();

        // extract title
        if(title != "") {
            //TokenStream stream = ngramWrapper.tokenStream("content", new StringReader(body));
            TokenStream stream = en_an.tokenStream("someField", new StringReader(title));

            stream.reset();
            while (stream.incrementToken()) {
                String featureData = stream.getAttribute(CharTermAttribute.class).toString();
                featureData = featureData.toLowerCase();
                String featureString = "1:" + featureData;
                if (featureData.length() > 2 &&
                    dictionary.contains(featureData) == true) {

                    if(featureList.containsKey(featureString) == false) {
                        featureList.put(featureString, new FeatureCounter());
                    }
                    else {
                        featureList.get(featureString).inc();
                    }
                }
            }
            stream.end();
            stream.close();
        }

        // extract body
        if(body != "") {
            //TokenStream stream = ngramWrapper.tokenStream("content", new StringReader(body));
            TokenStream stream = en_an.tokenStream("someField", new StringReader(body));

            List<String> docTokens = new ArrayList<String>();

            stream.reset();
            while (stream.incrementToken()) {
                String featureData = stream.getAttribute(CharTermAttribute.class).toString();
                featureData = featureData.toLowerCase();

                if(featureData.length() > 2 && dictionary.contains(featureData) == true) {
                    docTokens.add(featureData);
                }
            }
            stream.end();
            stream.close();

            extractGrams(docTokens, featureList);
        }

        // extract meta
        Elements eMETA = doc.select("META");
        for(Element elm: eMETA) {
            String name = elm.attr("name");
            String content = elm.attr("content");

            if(name.equalsIgnoreCase("description")) {
                //TokenStream stream = ngramWrapper.tokenStream("content", new StringReader(body));
                TokenStream stream = en_an.tokenStream("someField", new StringReader(body));
                stream.reset();
                while (stream.incrementToken()) {
                    String featureData = stream.getAttribute(CharTermAttribute.class).toString();
                    featureData = featureData.toLowerCase();
                    String featureString = "3:" + featureData;
                    if(featureData.length() > 2 &&
                       dictionary.contains(featureData) == true) {

                        if(featureList.containsKey(featureString) == false) {
                            featureList.put(featureString, new FeatureCounter());
                        }
                        else {
                            featureList.get(featureString).inc();
                        }
                    }
                }
                stream.end();
                stream.close();
            }

            if(name.equalsIgnoreCase("keywords")) {
                String[] tokens = content.split(",");
                for(String token: tokens) {
                    String keywords = token.toLowerCase().trim().replaceAll("\t", "_");
                    keywords = keywords.replaceAll(" ", "_");
                    String featureString = "4:" + keywords;
                    if(token.length() > 2) {
                        if(featureList.containsKey(featureString) == false) {
                            featureList.put(featureString, new FeatureCounter());
                        }
                        else {
                            featureList.get(featureString).inc();
                        }
                    }
                }
            }
        }

        // extract links
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");

        for (Element src : media) {
            String host = getHost(src.attr("abs:src"));
            if(host != null && host.length() > 2) {
                String featureString = "5:" + host.toLowerCase();
                if(featureList.containsKey(featureString) == false) {
                    featureList.put(featureString, new FeatureCounter());
                }
                else {
                    featureList.get(featureString).inc();
                }
            }
        }

        for (Element src : links) {
            String host = getHost(src.attr("abs:href"));
            if(host != null && host.length() > 2) {
                String featureString = "6:" + host.toLowerCase();
                if(featureList.containsKey(featureString) == false) {
                    featureList.put(featureString, new FeatureCounter());
                }
                else {
                    featureList.get(featureString).inc();
                }
            }
        }

        for (Element src : imports) {
            String host = getHost(src.attr("abs:href"));
            if(host != null && host.length() > 2) {
                String featureString = "7:" + host.toLowerCase();
                if(featureList.containsKey(featureString) == false) {
                    featureList.put(featureString, new FeatureCounter());
                }
                else {
                    featureList.get(featureString).inc();
                }
            }
        }

        return featureList;
    }

    private static void extractGrams(List<String> docTokens, Map<String, FeatureCounter> featureList) {
        for(int x = 0; x < docTokens.size(); x++) {

            String word = docTokens.get(x);
            String featureString = "2:" + word;
            if(featureList.containsKey(featureString) == false) {
                featureList.put(featureString, new FeatureCounter());
            }
            else {
                featureList.get(featureString).inc();
            }

            /*
            if(docTokens.size() > x + 1) {
                word = docTokens.get(x) + " " + docTokens.get(x+1);
                featureString = "2:" + word;
                if(featureList.containsKey(featureString) == false) {
                    featureList.put(featureString, new FeatureCounter());
                }
                else {
                    featureList.get(featureString).inc();
                }
            }

            if(docTokens.size() > x + 2) {
                word = docTokens.get(x) + " " + docTokens.get(x+1) + " " + docTokens.get(x+2);
                featureString = "2:" + word;
                if(featureList.containsKey(featureString) == false) {
                    featureList.put(featureString, new FeatureCounter());
                }
                else {
                    featureList.get(featureString).inc();
                }
            }
            */

        }
    }

    private static String getHost(String href) {
        try {
            URL url = new URL(href);
            String host = url.getHost();
            host = host.toLowerCase().trim().replaceAll("\t", "_");
            host = host.replaceAll(" ", "_");
            return host;
        }
        catch(Exception e) {
            return null;
        }
    }

    private static List<String> getSiteFeatures01(String pageData) throws Exception{

        Document doc = Jsoup.parse(pageData);

        String title = doc.title();
        String head = "";
        if(doc.head() != null) {
            head = doc.head().text();
        }
        String body = "";
        if(doc.body() != null) {
            body = doc.body().text();
        }
        //String meta = doc.getElementsByTag("meta").text();

        List<String> featureList = new ArrayList<String>();


        if(title != "") {
            TokenStream stream = en_an.tokenStream("someField", new StringReader(title));

            stream.reset();
            while (stream.incrementToken()) {
                String featureData = stream.getAttribute(CharTermAttribute.class).toString();
                String featureString = "1:" + featureData;
                if(     featureData.length() > 2 &&
                        featureList.contains(featureString) == false// &&
                        //stopWordList.contains(featureData) == false
                        ) {
                    featureList.add(featureString);
                }
            }
            stream.end();
            stream.close();
        }

        if(body != "") {
            TokenStream stream = en_an.tokenStream("someField", new StringReader(body));

            stream.reset();
            while (stream.incrementToken()) {
                String featureData = stream.getAttribute(CharTermAttribute.class).toString();
                String featureString = "2:" + featureData;
                if(     featureData.length() > 2 &&
                        featureList.contains(featureString) == false// &&
                        //stopWordList.contains(featureData) == false
                        ) {
                    featureList.add(featureString);
                }
            }
            stream.end();
            stream.close();
        }

        return featureList;
    }

    private static String loadPageData(String path) throws Exception {

        FileInputStream fis = new FileInputStream(path);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        StringBuffer sb = new StringBuffer();
        String line = br.readLine();
        while(line != null) {
            sb.append(line);
            line = br.readLine();
        }

        br.close();
        isr.close();
        fis.close();

        return sb.toString();
    }

    private static void saveSiteList(String path, List<SiteInfo> sites) throws Exception{

        FileWriter fr = new FileWriter(path);

        for(SiteInfo site: sites) {
            fr.write(site.id + "\t" + site.url + "\t" + site.siteClass + "\r\n");
        }

        fr.close();
    }

    private static List<SiteInfo> loadSiteList(String path) throws Exception {

        FileInputStream fis = new FileInputStream(path);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        int totalCount = 0;
        int count_true = 0;
        int count_false = 0;

        List<SiteInfo> siteList = new ArrayList<SiteInfo>();

        String line = br.readLine();
        while(line != null) {

            //line = line.replaceAll("\",\"", "\";\"");
            //line = line.replaceAll("\"", "");

            SiteInfo info = new SiteInfo();
            String[] tokens = line.split("\\|");
            String siteUrl = tokens[0];//.substring(1, tokens[0].length() - 1);
            info.url = siteUrl;
            String classList = tokens[1].substring(1, tokens[1].length() - 1);
            List<String> classes = Arrays.asList(classList.split(","));
            if(classes.contains("243")) { // 243 is Food and Beverage
                info.siteClass = 1;
                count_true++;
            }
            else {
                info.siteClass = 0;
                count_false++;
            }

            info.id = totalCount;
            totalCount++;
            siteList.add(info);

            line = br.readLine();
        }

        System.out.println("Class Count True : " + count_true);
        System.out.println("Class Count False : " + count_false);

        return siteList;
    }


}
