import edu.stanford.nlp.classify.ColumnDataClassifier;
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

    public static void main(String[] args) throws IOException {
        writeFilePath();
        if (args.length > 0) {
            where = args[0] + File.separator;
        }
        System.out.println("OS-Name : "+ getOsName());
        System.out.print("Enter cross-validation number: ");
        Scanner in = new Scanner(System.in);
        int cv_number = in.nextInt();
        boolean validate1 = validateInput1(cv_number);
        System.out.println("Choose segregation scenario :");
        System.out.println("- Leave-One-Out CV \"1\"");
        System.out.println("- Stratified K-Fold CV \"2\"");
        System.out.print("choose :");
        Scanner in2 = new Scanner(System.in);
        int segregationScenario = in2.nextInt();
        boolean validate2 = validateInput2(segregationScenario);
        if(validate1){
            if (validate2){
                int py_process = 0;
                //int py_process = runPreprocessor(cv_number);
                if (py_process != 220) {
                    writeFilePath2(cv_number, segregationScenario);
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
        file.delete();
        PropFileMaker propFileMaker = new PropFileMaker();
        file.createNewFile();
        propFileMaker.makeProp(classificationFeature, trainPath, testPath);

        FileWriter fileWriter = new FileWriter(propPath);
        fileWriter.write(propFileMaker.toString());
        fileWriter.close();
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

    private static void writeFilePath2(int cv_number, int segregationScenario) {
        int new_segregation = segregationScenario;
        System.out.println(new_segregation);
        if(segregationScenario==1){
            System.out.print("do you wanna shuffle train-data? (y/n) : " );
            Scanner inShuffle = new Scanner(System.in);
            String shuffled = inShuffle.nextLine();

            if(shuffled.equalsIgnoreCase("y")){
                new_segregation++;
                new_segregation++;
            }
        }
        System.out.println(new_segregation);
        if(getOsName().startsWith("Windows")){
            csv_source = ClassifierConstant.Unix.csv_source.replace("/", "\\\\");
            pyPath = ClassifierConstant.Unix.pythonPath.replace("/", "\\\\");
            trainPath = ("./data/splited/splited"+new_segregation+"/"+cv_number+"/trainFile.train").replace("/", "\\\\");
            testPath = ("./data/splited/splited"+new_segregation+"/"+cv_number+"/testFile.test").replace("/", "\\\\");
            propPath = ClassifierConstant.Unix.propFilePath.replace("/", "\\\\");
        } else {
            csv_source = ClassifierConstant.Unix.csv_source;
            pyPath = ClassifierConstant.Unix.pythonPath;
            trainPath = "./data/splited/splited"+new_segregation+"/"+cv_number+"/trainFile.train";
            testPath = "./data/splited/splited"+new_segregation+"/"+cv_number+"/testFile.test";
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
