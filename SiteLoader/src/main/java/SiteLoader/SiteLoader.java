package SiteLoader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by Shaun on 8/07/2015.
 */
public class SiteLoader {

    public static void main(String[] args) throws Exception {

        File dataPath = new File("SiteDataStore");
        if(dataPath.exists() == false) {
            dataPath.mkdirs();
        }

        FileInputStream fis = new FileInputStream("WebSiteDataSet\\SiteDataSet.csv");
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        int count = 0;
        String line = br.readLine();
        while(line != null) {

            String[] tokens = line.split("\\|");
            String siteUrl = tokens[0];
            saveSite(siteUrl, count);
            count++;

            line = br.readLine();
        }

        br.close();
        isr.close();
        fis.close();
    }

    private static void saveSite(String siteUrl, int count) throws Exception {

        System.out.print(count + " - Getting Site Data For : " + siteUrl);

        String fileName = siteUrl.replace('/', '_');
        File dataFile = new File("SiteDataStore\\" + fileName + ".html");
        if(dataFile.exists()){
            System.out.println(" Already Done");
            return;
        }

        Document doc = null;
        try {
            Connection conn = Jsoup.connect("http://" + siteUrl);
            doc = conn.get();
            int status = conn.response().statusCode();
            if(status != 200) {
                System.out.println(" Responce code not correct  : " + status);
                return;
            }
        }
        catch(Exception e) {
            System.out.println(" Error loading page : " + e.getMessage());
            return;
        }

        String pageData = doc.outerHtml();

        if(pageData.length() < 500) {
            System.out.println(" Not enough page data");
            return;
        }

        FileOutputStream fos = new FileOutputStream(dataFile.getAbsolutePath());
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(doc.outerHtml());
        osw.close();
        fos.close();

        System.out.println(" SAVED");
    }
}
