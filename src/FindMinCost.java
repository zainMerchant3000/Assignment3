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
            int pi = partition(arr, low, high, sqdists, offset);

            // Recursively sort the two subarrays:
            quickSort(arr, low, pi - 1, sqdists, offset); // left subarray
            quickSort(arr, pi + 1, high, sqdists, offset); // right subarray
        }
    }

    public static int partition(short[]arr, int low, int high, long[] sqdists, int offset) {
        // choose the pivot:
        int pivot = arr[high];
        // Pointer to smaller element
        int i = low - 1;

        // Traverse the array and partition:
        for (int j = low; j < high; j++) {
            // if current element smaller than pivot swap:
            if(sqdists[offset + arr[j]] < sqdists[offset + pivot]) {
                i++;
                // swap arr[i] and arr[j]
                swap(arr, i, j);

            }
        }
        // Place the pivot element in the correct position
        swap(arr, i + 1, high);
        return i + 1;  // Return the index of the pivot

    }
    // Function to swap two elements
    private static void swap(short[] arr, int i, int j) {
        short temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    static class BitSet {
        public long[] longs;

        public BitSet(int n) {
            // ceiling division of n by 64
            longs = new long[(n + 63) / 64];
        }

        public void set(int i) {
            // [i/64] -> calculate index in longs array
            // (1L << (i % 64)) -> calculate Bit position within array element
            // ex) i = 70
            // longs[70/64] = 1 (bit is in second long element)
            // (70 % 64) -> bit at position 6 within second long element
            // ex) i = 0
            // longs[0/64] = 0 (bit is in 0th long element)
            // (0 % 64) -> bit at position 0 within 0th long element
            longs[i / 64] |= (1L << (i % 64));
        }

        public void clearAll() {
            Arrays.fill(longs, 0);
        }

        // preform XOR operation between 2 bit-sets to find # of differences
        // each element in bit-set represents node (x,y) we will finding excluded/included elements in given
        // R
        public void xor(BitSet bs) {
            assert bs.longs.length == longs.length;
            // iterate through each element in bit-set to calculate bits
            for (int i = 0; i < longs.length; i++) {
                longs[i] ^= bs.longs[i];
            }
        }

        public boolean ask(int i) {
            return (longs[i / 64] & (1L << (i % 64))) != 0;
        }
    }

    // Stateful solution search
    static class S {
        final long[] sqdists;
        final short[] proximity;
        final long[] coordinates;
        final short[] stack;

        final BitSet visited, robustVs, robustVs2;
        final int N;
        int lastLowRobustCount = 0;

        public S(int n, long[] dists, short[] prox, long[] coords) {
            sqdists = dists;
            proximity = prox;
            coordinates = coords;
            stack = new short[n * n];
            visited = new BitSet(n);
            robustVs = new BitSet(n);
            robustVs2 = new BitSet(n);
            N = n;
        }

        // given radius (given by squared radius), robust?
        boolean test(long sqr) {
            // check if graph is connected
            // if this check fails, the graph is not robust
            // because it's not connected. the binary search
            // will increase the low number, so let 'visited'
            // be mutated (it always tracks the state of the last low update).
            if (isDisconnected(-1, sqr)) return false;
            // it's connected, so is it robust?
            robustVs2.clearAll();
            int robustCount2 = 0;
            for (int i = 0; i < robustVs.longs.length; i++) {
                var l = robustVs.longs[i];
                var b = 1L;
                for (int j = 0; j < 64; j++) {
                    // check each vertex within the
                    // difference bit set d.
                    var v = i * 64 + j;
                    // checking out-of bounds
                    if (v >= N) break;
                    // checking if
                    if ((l & b) != 0) {
                        b <<= 1;
                        continue;
                    }
                    //
                    b <<= 1;
                    if (!isDisconnected(v, sqr)) {
                        // v is not an articulation point any more
                        // since last low bound
                        robustVs2.set(v);
                        robustCount2++;
                    }
                }
            }
            if (lastLowRobustCount + robustCount2 != N) {
                // we're NOT robust.
                lastLowRobustCount += robustCount2;
                robustVs.xor(robustVs2);
                return false;
            }
            // if it's robust, discard.
            // '1111...1111' isn't so useful.
            return true;
        }

        // method to check if tower is connected when removing a tower
        boolean isDisconnected(int exclude, long sqr) {
            visited.clearAll();
            int bc = dfs((short) exclude, sqr, visited);
            if (exclude >= 0) return bc < N - 1;
            else return bc < N;
        }

        int dfs(short exclude, long sqr, BitSet visited) {
            int nvisited = 0;
            int stacklen = 1;
            // node where DFS will begin
            var start = (short) (exclude == 0 ? 1 : 0);
            // place starting node in DFS stack
            stack[0] = start;
            while (stacklen > 0) {
                // current -> node being currently processed
                // pop off from stack
                var current = stack[--stacklen];
                // current == exclude -> is the node being excluded (skip it)
                // visited.ask(current) -> check if current node been visited
                // skip if current node is excluded or already visited
                if (current == exclude || visited.ask(current)) continue;
                // mark current node as visited
                visited.set(current);
                nvisited++;
                // binary search on neighbors to find closest out of
                // range neighbor (or prove it does not exist).
                short low = 1;
                // high -> total number of nodes
                short high = (short) N;
                while (low < high) {
                    var m = (short) ((low + high) / 2);
                    if (inRange(current, m, sqr)) low = (short) (m + 1);
                    else high = m;
                }
                if (high == N) return N;
                // process each neighbor in the range
                for (short i = 1; i < low; i++) {
                    var n = proximity[current * N + i];
                    if (visited.ask(n)) continue;
                    stack[stacklen++] = n;
                }
            }
            return nvisited;
        }

        // checks if given node is within the range
        boolean inRange(short c, short i, long sqr) {
            var n = proximity[c * N + i];
            return sqdists[c * N + n] <= sqr;
        }
    }

    public static void main(String[] args) throws IOException {
        var BufferedReader = new BufferedReader(new FileReader(args[0]));
        // create StringBuilder object to build string
        var build = new StringBuilder();
        // store each line in file
        var line = "";
        //reading each line from file
        while ((line = BufferedReader.readLine()) != null) build.append(line).append(" ");
        BufferedReader.close();
        //storing input as string of tokens
        // ex ["5", "0", "3", "2","2","1","1","3","0","4","4"]
        var tokens = build.toString().split(" ");
        // //System.out.println(tokens[0]);
        // tokens[0] = "5"
        // parsing token (line) as Integer
        // ex) 5 -> tells us number of points
        var N = Integer.parseInt(tokens[0]);
        // create 2D array points
        // components (if w inside coordinates):
        //  x = low 4 bytes of w (32 bits)
        //  y = high 4 bytes of w (32 bits)
        var coordinates = new long[N];
        // k = 1 -> start at line 1 to process each pair of points
        // Integer.parseInt(tokens[k]) -> provides us value of points in given line (converts string to int)
        for (int i = 0, k = 1; i < N; i++, k += 2) {
            // retrieve x-coordinate
            int x = Integer.parseInt(tokens[k]);
            // retrieve y-coordinate
            int y = Integer.parseInt(tokens[k + 1]);
            // must do bit shift of 32 bits to extract y-coordinate
            // | -> using bit-wise OR to ensure no overlap between x and y coordinates
            // ((long)) x -> cast to long to ensure 64 bits
            coordinates[i] = ((long) y << 32) | ((long) x);
        }
        // indexing: row * N + column
        var sqdists = new long[N * N];
        var proximity = new short[N * N];
        var prefilled = new short[N];
        // populating list of indices
        for (int i = 0; i < N; i++) prefilled[i]=(short)i;
        for (int i = 0; i < N; i++) {
            // retrieve the x,y values stored in our array of points
            long ixy = coordinates[i];
            long ix = ixy & 0xfffffffL; // extract x-coordinate
            long iy = ixy >>> 32; // extract y-coordinate
            for (int j = 0; j < N; j++) {
                long jxy = coordinates[j];
                // retrieve x2, y2 values (other pair of points)
                long jx = jxy & 0xfffffffL;
                long jy = jxy >>> 32;
                long dx = ix - jx;
                long dy = iy - jy;
                sqdists[i * N + j] = dx * dx + dy * dy;
            }

            /*
            // fill up the proximity row
            System.arraycopy(prefilled, 0, proximity, i * N, N);
            // Quick-Sort implementation:
            quickSort(proximity, i * N, (i+1) * N - 1, sqdists, i * N);

             */


            System.arraycopy(prefilled, 0, proximity, i * N, N);
            // insertion sort
            for (int j = 1; j < N; j++) {
                // calculate starting index of of ith row in proximity array
                var offset = i * N;
                // stores the value of the jth element (point being considered for insertion)
                var x = proximity[offset + j];
                var k = j - 1;
                while (k >= 0 && sqdists[offset + proximity[offset + k]] > sqdists[offset + x]) {
                    proximity[offset + k + 1] = proximity[offset + k];
                    k--;
                }
                proximity[offset + k + 1] = x;
            }

        }





        // solution search
        // N -> number of points/Towers (for bitSet)
        // proximity -> sorted distances of (x,y) points
        // coordinates -> contains (x,y) points
        S sol = new S(N, sqdists, proximity, coordinates);
        // two-phase exponential search
        long high = 128; // some good starting point
        long low = 0;
        // phase 1: find good range
        // low is excluded and high is included, or else
        // range is length 1.
        // high >= low
        while (high >= low) {
            if (sol.test(high)) { // is robust?
                break;
            } else {
                low = high;
                high *= 2;
            }
        }
        // phase 2: refine the range and find minimum cost C
        System.out.println("Phase 2: - Refining the range to find minimum cost C from C = " + high);
        while (low < high) {
            long m = (low & high) + ((low ^ high) >> 1); // signed midpoint without overflow
            if (sol.test(m)) { // is robust? -> go down
                high = m;
            } else { // is not robust? -> go up
                System.out.println("at m = " + m + ", we'll be going up");
                low = m + 1;
            }
        }

        // an answer WILL exist and the loop above WILL terminate.
        //  proof that low == high:
        //      1. we can never make low > high
        //      2. we know that NOT (low < high).
        //      3. by elimination, low == high.
        assert low == high;
        System.out.println(low);
    }
}
