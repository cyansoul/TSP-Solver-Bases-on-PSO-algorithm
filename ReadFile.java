import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadFile {

    public static int[] getX (int pointNum, String filename) throws IOException {
        int[] x = new int[pointNum];
        String str;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        for (int i = 0; i < pointNum; i++) {
            str = bufferedReader.readLine();
            String[] strcol = str.split(" ");
            x[i] = Integer.valueOf(strcol[1]);
        }

        return x;
    }

    public static int[] getY (int cityNum, String filename) throws IOException {
        int[] y = new int[cityNum];
        String str;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        for (int i = 0; i < cityNum; i++) {
            str = bufferedReader.readLine();
            String[] strcol = str.split(" ");
            y[i] = Integer.valueOf(strcol[2]);
        }

        return y;
    }
}
