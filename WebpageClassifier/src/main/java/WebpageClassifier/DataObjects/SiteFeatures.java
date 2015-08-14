package WebpageClassifier.DataObjects;

import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

/**
 * Created by Shaun on 10/07/2015.
 */
public class SiteFeatures {

    private static final ConstantValueEncoder interceptEncoder = new ConstantValueEncoder("intercept");
    private static final FeatureVectorEncoder featureEncoder = new StaticWordValueEncoder("feature");

    private RandomAccessSparseVector vector;

    int itemClass = 0;
    String label = "";
    String siteId;

    public SiteFeatures(String siteId, String label, int itemClass, int vectorSize) {
        this.siteId = siteId;
        this.label = label;
        this.itemClass = itemClass;
        vector = new RandomAccessSparseVector((int)(vectorSize * 1.5));
        interceptEncoder.addToVector("1", vector);
    }

    public void addFeature(String feature, Double weight) {

        featureEncoder.addToVector(feature, weight, vector);
    }

    public int getItemClass() {
        return itemClass;
    }

    public Vector getFeatureSetVector() {
        return vector;
    }

    public String getLabel() {
        return label;
    }

    public String getSiteId() {
        return siteId;
    }
}
