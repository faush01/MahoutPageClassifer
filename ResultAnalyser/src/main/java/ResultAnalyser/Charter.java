package ResultAnalyser;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by jfisher on 29/07/15.
 */
public interface Charter {

    void chartRoc(Set<ResultAnalyser.RocSeriesData> data) throws IOException;
    void chartError(List<ResultAnalyser.ErrorSeriesData> data, String fileName) throws IOException;
    void chartDistribution(Set<Result> data) throws IOException;
}
