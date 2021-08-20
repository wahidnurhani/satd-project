import java.io.IOException;
import java.io.OutputStream;

//just for test
public class SATDClassifier {
    static Runtime runtime = Runtime.getRuntime();

    public static void main(String[] args) {
        try {
            runPython();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runPython() throws IOException {
        String arg1 = "test argument2";
        String[] cmd = {
                "python",
                "python/src/test.py",
                arg1
        };

        Process process1 = runtime.exec(cmd);
        OutputStream out = process1.getOutputStream();
        System.out.println(out.toString());
    }
}
