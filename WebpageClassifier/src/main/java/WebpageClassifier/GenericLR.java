package WebpageClassifier;

import WebpageClassifier.DataObjects.SiteDataLoader;
import WebpageClassifier.DataObjects.SiteFeatures;
import org.apache.mahout.classifier.evaluation.Auc;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Shaun on 7/07/2015.
 */
public class GenericLR {

    public static final int NUM_CATEGORIES = 2;
    private static Random random;

    public static void main(String[] args) throws Exception {

        boolean useLastTestSet = false;
        SiteDataLoader dataLoader = new SiteDataLoader();

        List<SiteFeatures> fullDataSet = dataLoader.getSiteData();

        random = new Random();

        for (int y = 0; y < 5; y++) {

            Collections.shuffle(fullDataSet);

            List<SiteFeatures> trainItems = new ArrayList<SiteFeatures>();
            List<SiteFeatures> testItems = new ArrayList<SiteFeatures>();

            if (useLastTestSet) {
                Set<String> testIds = new HashSet<String>();
                FileReader fr = new FileReader("TestSet.txt");
                BufferedReader br = new BufferedReader(fr);
                String line = br.readLine();
                while (line != null) {
                    testIds.add(line);
                    line = br.readLine();
                }
                fr.close();
                for (SiteFeatures site : fullDataSet) {
                    double x = random.nextDouble();
                    if (testIds.contains(site.getSiteId())) {
                        testItems.add(site);
                    } else {
                        trainItems.add(site);
                    }
                }
            } else {
                FileWriter fw = new FileWriter("TestSet.txt");
                for (SiteFeatures site : fullDataSet) {
                    double x = random.nextDouble();
                    if (x < 0.2) {
                        testItems.add(site);
                        fw.write(site.getSiteId() + "\r\n");
                    } else {
                        trainItems.add(site);
                    }
                }
                fw.close();
            }

            OnlineLogisticRegression lr = new OnlineLogisticRegression(
                    NUM_CATEGORIES,
                    (int) (dataLoader.getFeatureSetSize() * 1.5),
                    new L1())
                    .learningRate(1)
                    .alpha(1)
                    .lambda(0.000001)
                    .stepOffset(10000)
                    .decayExponent(0.2);

            for (int pass = 0; pass < 20; pass++) {

                for (SiteFeatures observation : trainItems) {
                    lr.train(observation.getItemClass(), observation.getFeatureSetVector());
                }

                if (pass % 5 == 0) {
                    Auc eval = new Auc(0.5);
                    for (SiteFeatures testCall : testItems) {
                        eval.add(testCall.getItemClass(), lr.classifyScalar(testCall.getFeatureSetVector()));
                    }
                    System.out.printf("Pass:%d, LR:%.4f, Eval:%.4f\n", pass, lr.currentLearningRate(), eval.auc());
                }
            }


            File dataPath = new File("Results");
            if(dataPath.exists() == false) {
                dataPath.mkdirs();
            }
            FileWriter fr = new FileWriter("Results\\TestResults_" + y + ".tab");

            for (SiteFeatures testCall : testItems) {
                Vector results = lr.classifyFull(testCall.getFeatureSetVector());
                String line = "";
                for (int x = 0; x < results.size(); x++) {
                    line += "\t" + results.get(x);
                }
                fr.write(testCall.getItemClass() + line + "\t" + testCall.getLabel() + "\r\n");
            }

            fr.close();

            System.out.println("");
        }
    }
}
