package ResultAnalyser;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jfisher on 29/07/15.
 */
public class ResultLoader {

    private Map<String, List<Result>> resultSets;

    public ResultLoader(File dir, String filePattern) throws Exception {
        resultSets = new HashMap<String, List<Result>>();
        FileFilter fileFilter = new WildcardFileFilter(filePattern);
        File[] files = dir.listFiles(fileFilter);
        for (File file : files) {
            List<Result> results = loadResults(file);
            System.out.println(file.getName());
            String key = file.getName().substring(0, file.getName().lastIndexOf("."));
            resultSets.put(key, results);
        }
    }

    public int size() {
        return this.resultSets.size();
    }

    public Set<String> getSets() {
        return resultSets.keySet();
    }

    public List<Result> getResults(String set) {
        return resultSets.get(set);
    }

    private List<Result> loadResults(File path) throws Exception {

        List<Result> results = new ArrayList<Result>();

        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while (line != null) {

            String[] tokens = line.split("\t");

            results.add(new Result(Integer.parseInt(tokens[0]),
                    Double.parseDouble(tokens[1]),
                    Double.parseDouble(tokens[2]),
                    tokens[3]));

            line = br.readLine();
        }

        br.close();
        fr.close();

        return results;
    }
}
