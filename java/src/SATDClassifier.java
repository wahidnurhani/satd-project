import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Pair;

import java.io.*;
import java.util.Scanner;

public class SATDClassifier {
    private static String where = "";
    private static String OS = null;

    static String csv_source;
    static String pyPath;
    static String trainPath;
    static String testPath;
    static String propPath;
    static PropFileMaker.ClassificationFeature classificationFeature;

    public static void main(String[] args) throws IOException, InterruptedException {
        writeFilePath();
        if (args.length > 0) {
            where = args[0] + File.separator;
        }
        Runtime runtime = Runtime.getRuntime();
        System.out.println("OS-Name : "+ getOsName());
        System.out.print("Enter first cross-validation number: ");
        Scanner in = new Scanner(System.in);
        int cv_number = in.nextInt();
        boolean validate1 = validateInput1(cv_number);
        System.out.println("Choose segregation scenario :");
        System.out.println("- by project type \"1\"");
        System.out.println("- classification type \"2\"");
        System.out.print("choose :");
        Scanner in2 = new Scanner(System.in);
        int segregationNumber = in2.nextInt();
        boolean validate2 = validateInput2(segregationNumber);
        if(validate1){
            if (validate2){
                int py_process = 0;
                //int py_process = runPreprocessor(cv_number);
                if (py_process != 220) {
                    writeFilePath2(cv_number, segregationNumber);
                }
            }
        }

        System.out.println("---------------------------");
        System.out.println("choose classification feature (1.NGrams, 2.Bag Of Words) ");
        System.out.print("(1/2) : ");
        Scanner in3  = new Scanner(System.in);
        makePropFile(in3);

        if(validate1){
            if (validate2){
                classify();
            }
        }
//        for (int i = cv_number; i<=cv_number+1;i++){
//            int py_process = runPreprocessor(i);
//            if(py_process ==220){
//                classify();
//            }
//        }

        //demonstrateSerialization();
        //demonstrateSerializationColumnDataClassifier();
    }

    private static void makePropFile(Scanner in3) throws IOException {
        if(in3.nextInt()==1){
            classificationFeature = PropFileMaker.ClassificationFeature.NGRAMS;
        } else {
            classificationFeature = PropFileMaker.ClassificationFeature.BAG_OF_WORDS;
        }
        File file = new File(propPath);
        if(file.delete()){
            if(file.createNewFile()){
                PropFileMaker propFileMaker = new PropFileMaker();
                propFileMaker.makeProp(classificationFeature, trainPath, testPath);
            }
        }else if(file.createNewFile()){
            PropFileMaker propFileMaker = new PropFileMaker();
            propFileMaker.makeProp(classificationFeature, trainPath, testPath);
        }
    }

    private static boolean validateInput2(int segregationNumber) {
        if (segregationNumber>=1 && segregationNumber<=2){
            return true;
        } else {
            System.out.println("pleas type number between 1 and 2 for cross-validation number");
            return false;
        }
    }

    private static boolean validateInput1(int cv_number) {
        if(cv_number>=1 && cv_number<=10){
            return true;
        } else {
            System.out.println("pleas type number between 1 and 10 for cross-validation number");
            return false;
        }

    }

    private static void writeFilePath2(int cv_number, int segregationNumber) {
        if(getOsName().startsWith("Windows")){
            csv_source = ClassifierConstant.Unix.csv_source.replace("/", "\\\\");
            pyPath = ClassifierConstant.Unix.pythonPath.replace("/", "\\\\");
            trainPath = ("./data/splited/splited"+segregationNumber+"/"+cv_number+"/trainFile.train").replace("/", "\\\\");
            testPath = ("./data/splited/splited"+segregationNumber+"/"+cv_number+"/testFile.test").replace("/", "\\\\");
            propPath = ClassifierConstant.Unix.propFilePath.replace("/", "\\\\");
        } else {
            csv_source = ClassifierConstant.Unix.csv_source;
            pyPath = ClassifierConstant.Unix.pythonPath;
            trainPath = "./data/splited/splited"+segregationNumber+"/"+cv_number+"/trainFile.train";
            testPath = "./data/splited/splited"+segregationNumber+"/"+cv_number+"/testFile.test";
            propPath = ClassifierConstant.Unix.propFilePath;
        }
    }

    private static void writeFilePath(){
        if(getOsName().startsWith("Windows")){
            csv_source = ClassifierConstant.Unix.csv_source.replace("/", "\\\\");
            pyPath = ClassifierConstant.Unix.pythonPath.replace("/", "\\\\");
            trainPath = ClassifierConstant.Unix.trainFilePath.replace("/", "\\\\");
            testPath = ClassifierConstant.Unix.testFilePath.replace("/", "\\\\");
            propPath = ClassifierConstant.Unix.propFilePath.replace("/", "\\\\");
        } else {
            csv_source = ClassifierConstant.Unix.csv_source;
            pyPath = ClassifierConstant.Unix.pythonPath;
            trainPath = ClassifierConstant.Unix.trainFilePath;
            testPath = ClassifierConstant.Unix.testFilePath;
            propPath = ClassifierConstant.Unix.propFilePath;
        }
    }

    private static void demonstrateSerialization()
            throws IOException, ClassNotFoundException {
        System.out.println();
        System.out.println("Demonstrating working with a serialized classifier");
        ColumnDataClassifier cdc = new ColumnDataClassifier(where + propPath);
        Classifier<String,String> cl =
                cdc.makeClassifier(cdc.readTrainingExamples(where + trainPath));

        // Exhibit serialization and deserialization working. Serialized to bytes in memory for simplicity
        System.out.println();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cl);
        oos.close();

        byte[] object = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(object);
        ObjectInputStream ois = new ObjectInputStream(bais);
        LinearClassifier<String,String> lc = ErasureUtils.uncheckedCast(ois.readObject());
        ois.close();
        ColumnDataClassifier cdc2 = new ColumnDataClassifier(where + propPath);

        // We compare the output of the deserialized classifier lc versus the original one cl
        // For both we use a ColumnDataClassifier to convert text lines to examples
        System.out.println();
        System.out.println("Making predictions with both classifiers");
        for (String line : ObjectBank.getLineIterator(where + testPath, "utf-8")) {
            Datum<String,String> d = cdc.makeDatumFromLine(line);
            Datum<String,String> d2 = cdc2.makeDatumFromLine(line);
            System.out.printf("%s  =origi=>  %s (%.4f)%n", line, cl.classOf(d), cl.scoresOf(d).getCount(cl.classOf(d)));
            System.out.printf("%s  =deser=>  %s (%.4f)%n", line, lc.classOf(d2), lc.scoresOf(d).getCount(lc.classOf(d)));
        }
    }

    private static void demonstrateSerializationColumnDataClassifier()
            throws IOException, ClassNotFoundException {
        System.out.println();
        System.out.println("Demonstrating working with a serialized classifier using serializeTo");
        ColumnDataClassifier cdc = new ColumnDataClassifier(where + propPath);
        cdc.trainClassifier(where + trainPath);

        // Exhibit serialization and deserialization working. Serialized to bytes in memory for simplicity
        System.out.println();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        cdc.serializeClassifier(oos);
        oos.close();

        byte[] object = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(object);
        ObjectInputStream ois = new ObjectInputStream(bais);
        ColumnDataClassifier cdc2 = ColumnDataClassifier.getClassifier(ois);
        ois.close();

        // We compare the output of the deserialized classifier cdc2 versus the original one cl
        // For both we use a ColumnDataClassifier to convert text lines to examples
        System.out.println("Making predictions with both classifiers");
        for (String line : ObjectBank.getLineIterator(where + testPath, "utf-8")) {
            Datum<String,String> d = cdc.makeDatumFromLine(line);
            Datum<String,String> d2 = cdc2.makeDatumFromLine(line);
            System.out.printf("%s  =origi=>  %s (%.4f)%n", line, cdc.classOf(d), cdc.scoresOf(d).getCount(cdc.classOf(d)));
            System.out.printf("%s  =deser=>  %s (%.4f)%n", line, cdc2.classOf(d2), cdc2.scoresOf(d).getCount(cdc2.classOf(d)));
        }
    }

    public static void classify(){
            try {
                System.out.println();
                System.out.println("Training ColumnDataClassifier");
                ColumnDataClassifier cdc = new ColumnDataClassifier(where + propPath);
                cdc.trainClassifier(where + trainPath);

                System.out.println();
                System.out.println("Testing accuracy of ColumnDataClassifier");
                Pair<Double, Double> performance = cdc.testClassifier(where + testPath);
                System.out.printf("Accuracy: %.3f; macro-F1: %.3f%n", performance.first(), performance.second());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static int runPreprocessor(int crossValidationNumber) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("python",
                System.getProperty("user.dir") + pyPath, crossValidationNumber+"",
                csv_source, trainPath,
                testPath, propPath
        );
        return executeProcess(builder.start());
    }

    private static int executeProcess(Process process) throws IOException, InterruptedException {
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader error_br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            buffer.append(line);
            buffer.append("\n");
        }
        buffer.append("Error line : ");
        while ((line = error_br.readLine()) != null){
            buffer.append(line);
            buffer.append("\n");
        }
        int exitCode = process.waitFor();
        System.out.println(" \n" + buffer);
        System.out.println("Process exit value:"+exitCode);
        br.close();
        return exitCode;
    }

    public static String getOsName()
    {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }
}
