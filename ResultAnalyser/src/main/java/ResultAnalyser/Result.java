package ResultAnalyser;

/**
 * Created by Shaun on 11/07/2015.
 */
public class Result implements Comparable<Result>{

    private int itemClass;
    private double result0;
    private double result1;
    private String label;

    private boolean trueValue;
    private Double trueScore;

    public Result(int itemClass, double result0, double result1, String label) throws Exception {
        this.itemClass = itemClass;
        this.result0 = result0;
        this.result1 = result1;
        this.label = label;

        if((this.result0 > this.result1) && itemClass == 0) {
            trueValue = true;
            trueScore = this.result0;
        }
        else if((this.result1 > this.result0) && itemClass == 1) {
            trueValue = true;
            trueScore = this.result1;
        }
        else if((this.result1 > this.result0) && itemClass == 0) {
            trueValue = false;
            trueScore = this.result1;
        }
        else if((this.result0 > this.result1) && itemClass == 1) {
            trueValue = false;
            trueScore = this.result0;
        }
        else {
            throw new Exception("matrix not correct");
        }
    }

    @Override
    public boolean equals(Object r) {
        return ((Result)r).getLabel().equals(this.getLabel());
    }

    public int getItemClass() {
        return itemClass;
    }

    public boolean isTrue() {
        return this.trueValue;
    }

    public double getConfidence() {
        return this.trueScore;
    }

    public String getLabel() {
        return this.label;
    }

    public int hashCode() {
        return trueScore.hashCode();
    }

    public int compareTo(Result o) {
        return o.trueScore.compareTo(trueScore);
    }
}
