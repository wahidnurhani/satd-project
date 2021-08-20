import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class SATDClassifier {
    static Runtime runtime = Runtime.getRuntime();
    private static String where = "";
    private static String testFilePath = "data/splited/testFile.test";
    private static String trainFilePath = "data/splited/trainFile.train";
    private static String propFilePath = "data/splited/propFile.prop";

    public static void main(String[] args) {
        if (args.length > 0) {
            where = args[0] + File.separator;
        }

        try {
            System.out.println("Enter a cross-validation number: ");
            Scanner in = new Scanner(System.in);
            runPreprocessor(in.nextInt());

            System.out.println();
//            System.out.println("Training ColumnDataClassifier");
//            ColumnDataClassifier cdc = new ColumnDataClassifier(where + propFilePath);
//            cdc.trainClassifier(where + trainFilePath);
//
//            System.out.println();
//            System.out.println("Testing predictions of ColumnDataClassifier");
//            for (String line : ObjectBank.getLineIterator(where + "data/examples/iris.test", "utf-8")) {
//                // instead of the method in the line below, if you have the individual elements
//                // already you can use cdc.makeDatumFromStrings(String[])
//                Datum<String,String> d = cdc.makeDatumFromLine(line);
//                System.out.printf("%s  ==>  %s (%.4f)%n", line, cdc.classOf(d), cdc.scoresOf(d).getCount(cdc.classOf(d)));
//            }
//
//            System.out.println();
//            System.out.println("Testing accuracy of ColumnDataClassifier");
//            Pair<Double, Double> performance = cdc.testClassifier(where + "data/examples/iris.test");
//            System.out.printf("Accuracy: %.3f; macro-F1: %.3f%n", performance.first(), performance.second());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }

    public static void runPreprocessor(int crossValidationNumber) throws IOException, InterruptedException {
        String csv_source = "data/src/technical_debt_dataset.csv";
        String[] cmd = {
                "python",
                "python/src/preprocessor.py",
                crossValidationNumber+"",
                csv_source
        };

        Process process1 = runtime.exec(cmd);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(process1.getInputStream()));

        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            buffer.append(line);
            buffer.append("\n");
        }
        int exitCode = process1.waitFor();
        System.out.println("All print value: \n" + buffer);
        System.out.println("Process exit value:"+exitCode);
        br.close();
    }
}
