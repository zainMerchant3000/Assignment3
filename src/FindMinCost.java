import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/*
 Test your code with the provided test cases in \tests
 The command should look like:
    java [your_java_file] [path_to_test_file]

 You can compare your output with the key in tests/key* files
*/

public class FindMinCost {
    public static void quickSort(short[] arr, int low, int high, long[] sqdists, int offset) {
        if (low < high) {
            // partition the array
            var pi = partition(arr, low, high, sqdists, offset);
            // Recursively sort the two sub-arrays:
            quickSort(arr, low, pi - 1, sqdists, offset); // left subarray
            quickSort(arr, pi + 1, high, sqdists, offset); // right subarray
        }
    }

    public static int partition(short[] arr, int low, int high, long[] sqdists, int offset) {
        // choose the pivot:
        var pivot = arr[high];
        // Pointer to smaller element
        int i = low - 1;

        // Traverse the array and partition:
        for (int j = low; j < high; j++) {
            // if current element smaller than pivot swap:
            if (sqdists[offset + arr[j]] < sqdists[offset + pivot]) {
                i++;
                // swap arr[i] and arr[j]
                var temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        // Place the pivot element in the correct position
        var temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;  // Return the index of the pivot
    }

    static class BitSet {
        public long[] longs;

        public BitSet(int n) {
            // ceiling division of n by 64
            longs = new long[(n + 63) / 64];
        }

        public void set(int i) {
            longs[i / 64] |= (1L << (i % 64));
        }

        public void clearAll() {
            Arrays.fill(longs, 0);
        }

        public boolean ask(int i) {
            return (longs[i / 64] & (1L << (i % 64))) != 0;
        }
    }

    // Stateful solution search
    static class S {
        // definition of graph
        final int N;
        final long[] sqdists, coordinates;
        final short[] proximity;

        // existence of cut points (DFS)
        final short[] depth, lowpoints, parents;

        // connectedness check (DFS)
        final short[] stack;
        final BitSet visited; // used for both DFS algorithms

        public S(int n, long[] dists, short[] prox, long[] coords) {
            N = n;
            sqdists = dists;
            coordinates = coords;
            proximity = prox;
            depth = new short[n];
            lowpoints = new short[n];
            parents = new short[n];
            stack = new short[n * n];
            visited = new BitSet(n);
        }

        // given radius (given by squared radius), robust?
        boolean test(long sqr) {
            // check for connectedness and then for robustness (biconnectedness).
            return dfs(sqr, visited) == N && !cutpointExists(sqr);
        }

        int dfs(long sqr, BitSet visited) {
            visited.clearAll();
            short nvisited = 0, start = 0;
            int stacklen = 1;
            stack[0] = start;
            while (stacklen > 0) {
                var current = stack[--stacklen];
                if (current == (short) -1 || visited.ask(current)) continue;
                visited.set(current);
                nvisited++;
                short low = 1, high = (short) N;
                while (low < high) {
                    var m = (short) ((low + high) / 2);
                    if (inRange(current, m, sqr)) low = (short) (m + 1);
                    else high = m;
                }
                if (low == N) return N;
                for (int i = 1; i < low; i++) {
                    var n = proximity[current * N + i];
                    if (visited.ask(n)) continue;
                    stack[stacklen++] = n;
                }
            }
            return nvisited;
        }

        boolean inRange(short c, short i, long sqr) {
            return sqdists[c * N + proximity[c * N + i]] <= sqr;
        }

        boolean cutpointExists(long sqr) {
            parents[0] = -1;
            visited.clearAll();
            return cutpointExistsGo((short) 0, (short) 0, sqr);
        }

        boolean cutpointExistsGo(short c, short d, long sqr) {
            visited.set(c);
            depth[c] = d;
            lowpoints[c] = d;
            var cut = false;
            short children = 0, low = 1, high = (short) N;
            while (low < high) {
                var m = (short) ((low + high) / 2);
                if (inRange(c, m, sqr)) low = (short) (m + 1);
                else high = m;
            }
            for (var i = 1; i < low; i++) {
                var n = proximity[c * N + i];
                if (!visited.ask(n)) {
                    parents[n] = c;
                    if (cutpointExistsGo(n, (short) (d + 1), sqr)) return true;
                    children++;
                    if (lowpoints[n] >= depth[c]) cut = true;
                    lowpoints[c] = (short) Math.min(lowpoints[c], lowpoints[n]);
                } else if (n != parents[c]) lowpoints[c] = (short) Math.min(lowpoints[c], depth[n]);
            }
            return (parents[c] != -1 && cut) || (parents[c] == -1 && children > 1);
        }
    }

    public static void main(String[] args) throws IOException {
        var BufferedReader = new BufferedReader(new FileReader(args[0]));
        var build = new StringBuilder();
        var line = "";
        while ((line = BufferedReader.readLine()) != null) build.append(line).append(" ");
        BufferedReader.close();
        var tokens = build.toString().split(" ");
        var N = Integer.parseInt(tokens[0]);
        // create 2D array points
        // components (if w inside coordinates):
        //  x = low 4 bytes of w (32 bits)
        //  y = high 4 bytes of w (32 bits)
        var coordinates = new long[N];
        for (int i = 0, k = 1; i < N; i++, k += 2) {
            int x = Integer.parseInt(tokens[k]), y = Integer.parseInt(tokens[k + 1]);
            coordinates[i] = ((long) y << 32) | ((long) x);
        }
        // indexing: row * N + column
        var sqdists = new long[N * N];
        var proximity = new short[N * N];
        var prefilled = new short[N];
        // populating list of indices
        for (int i = 0; i < N; i++) prefilled[i] = (short) i;
        var largestsqdist = 0L;
        for (int i = 0; i < N; i++) {
            // retrieve the x,y values stored in our array of points
            long ixy = coordinates[i];
            long ix = ixy & 0xffffffffL; // extract x-coordinate
            long iy = ixy >>> 32; // extract y-coordinate
            for (int j = 0; j < N; j++) {
                long jxy = coordinates[j];
                long jx = jxy & 0xffffffffL;
                long jy = jxy >>> 32;
                long dx = ix - jx;
                long dy = iy - jy;
                long di = dx * dx + dy * dy;
                largestsqdist = Math.max(largestsqdist, di);
                sqdists[i * N + j] = di;
            }
            // fill up the proximity row
            System.arraycopy(prefilled, 0, proximity, i * N, N);
            quickSort(proximity, i * N, (i + 1) * N - 1, sqdists, i * N);
        }
        var sol = new S(N, sqdists, proximity, coordinates);
        long low = 0, high = largestsqdist;
        while (low < high) {
            long m = (low & high) + ((low ^ high) >> 1);
            if (sol.test(m)) high = m;
            else low = m + 1;
        }
        System.out.println(low);
    }
}
