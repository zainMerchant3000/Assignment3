
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/*
 Test your code with the provided test cases in \tests
 The command should look like:
    java [your_java_file] [path_to_test_file]

 You can compare your output with the key in tests/key* files
*/

public class FindMinCost {
    public static void main(String[] args) throws IOException {
        BufferedReader f = new BufferedReader(new FileReader(args[0]));
        StringTokenizer st = new StringTokenizer(f.readLine());
        int n = Integer.parseInt(st.nextToken());

        for (int j = 0; j < n; j++) {
            st = new StringTokenizer(f.readLine());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
        }

        long answer= 0;
        /* your code here to calculate the answer*/

        System.out.println(answer);
    }

    }
