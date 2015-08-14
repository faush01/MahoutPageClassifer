package FeatureFilter;

import java.util.Comparator;

/**
 * Created by Shaun on 7/08/2015.
 */
public class FeatureSorter implements Comparator<FeatureInfo> {

    public int compare(FeatureInfo o1, FeatureInfo o2) {
        return o2.weight.compareTo(o1.weight);
    }
}
