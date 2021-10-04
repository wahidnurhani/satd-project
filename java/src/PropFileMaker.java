import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PropFileMaker {

    ClassificationFeature classificationFeature;
    boolean useClassFeature=true;
    boolean useNGrams=false;
    boolean usePrefixSuffixNGrams=false;
    int maxNGramleng=3;
    int minNGramleng=1;
    List<Integer> binnedLengths = new ArrayList<>(Arrays.asList(10, 20, 30));
    boolean useSplitWords=false;
    String splitWordRegexp= "[ ]";
    int printClassifierParam=200;

    int goldAnswerColumn = 0;
    int displayedColumn = 1;

    boolean intern= true;
    int sigma=3;
    boolean useQN =true;
    int QNsize=15;
    String tolerance= "1e-4";

    String trainFile;
    String testFile;

    public void makeProp(ClassificationFeature classificationFeature, String trainFile, String testFile) {
        this.trainFile = trainFile;
        this.testFile = testFile;
        this.classificationFeature = classificationFeature;

        if(classificationFeature.equals(ClassificationFeature.BAG_OF_WORDS)){
            this.useSplitWords = true;
        } else {
            this.useNGrams = true;
            this.usePrefixSuffixNGrams = true;
            System.out.print("maxNGrams = ");
            Scanner inNGrams = new Scanner(System.in);
            this.maxNGramleng = inNGrams.nextInt();

            System.out.print("binnedLegths (ex: 10,20,30) = ");
            Scanner inBinnedLengths = new Scanner(System.in);
            String[] binnedList = inBinnedLengths.nextLine().split(",");
            int binned1 = Integer.parseInt(binnedList[0].trim());
            int binned2 = Integer.parseInt(binnedList[1].trim());
            int binned3 = Integer.parseInt(binnedList[2].trim());
            this.binnedLengths = new ArrayList<>(Arrays.asList(binned1, binned2, binned3));
        }
        System.out.print("do you wanna change sigma or QNsize? (y/n) : " );
        Scanner inSelectedOptimization = new Scanner(System.in);
        String selectedChangeOptimization = inSelectedOptimization.nextLine();

        if(selectedChangeOptimization.equals("y")){
            System.out.print("sigma = ");
            Scanner inSigma = new Scanner(System.in);
            this.sigma=inSigma.nextInt();
            System.out.print("QNsize = ");
            Scanner inQNsize = new Scanner(System.in);
            this.QNsize=inQNsize.nextInt();
        }
    }

    public enum ClassificationFeature{
        NGRAMS, BAG_OF_WORDS
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("useClassFeature=").append(useClassFeature).append("\n");
        if(this.classificationFeature.equals(ClassificationFeature.NGRAMS)){
            stringBuilder.append("1.useNGrams=").append(useNGrams).append("\n");
            stringBuilder.append("1.usePrefixSuffixNGrams=").append(useNGrams).append("\n");
            stringBuilder.append("1.maxNGramLeng=").append(maxNGramleng).append("\n");
            stringBuilder.append("1.minNGramLeng=").append(minNGramleng).append("\n");
            stringBuilder.append("1.binnedLengths=")
                    .append(binnedLengths.get(0)).append(",")
                    .append(binnedLengths.get(1)).append(",")
                    .append(binnedLengths.get(2)).append("\n");
        } else {
            stringBuilder.append("1.useSplitWords=").append(useSplitWords).append("\n");
            stringBuilder.append("1.splitWordsRegexp=").append(splitWordRegexp).append("\n");
        }
        stringBuilder.append("printClassifierParam=").append(printClassifierParam).append("\n");
        stringBuilder.append("goldAnswerColumn=").append(goldAnswerColumn).append("\n");
        stringBuilder.append("displayedColumn=").append(displayedColumn).append("\n");
        stringBuilder.append("intern=").append(intern).append("\n");
        stringBuilder.append("sigma=").append(sigma).append("\n");
        stringBuilder.append("useQN=").append(useQN).append("\n");
        stringBuilder.append("QNsize=").append(QNsize).append("\n");
        stringBuilder.append("tolerance=").append(tolerance).append("\n");
        stringBuilder.append("trainfile=").append(trainFile).append("\n");
        stringBuilder.append("testfile=").append(testFile).append("\n");

        return stringBuilder.toString();
    }
}
