
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
    static class BitSet {
        public long[] longs;

        // ceiling division of a by 64
        private static int ce64(int a) {
            // ex) a = 128
            //    (128 + 63) / 64 + (64 % 64 == 0 ? 0 : 1)
            //    (128 + 63) / 64 = 2
            //    (64 % 64 == 0) = 0
            //   2 + 0 (return 2)

            // (a+63) -> to ensure division result goes to next whole number (round up chunks)
            // (a+63) / 64 ->
            // if (a % 64 == 0) result = 0
             // else result = 1
            // method calculates the number of 64-bit chunks needed to
            return (a + 63) / 64 + (a % 64 == 0 ? 0 : 1);
        }

        public BitSet(int n) {
            // creating bit-set array
            // n -> (each entry will represent our (x,y) set and distance
            // n -> multiple of 64
            // n = 128 (array of 2)
            // longs = new long[2]
            longs = new long[ce64(n)];
        }

        public void set(int i) {
            // [i/64] -> calculate index in longs array
            // (1L << (i % 64)) -> calculate Bit position within array element
            // ex) i = 70
               // longs[70/64] = 1 (bit is in second long element)
               // (70 % 64) -> bit at position 6 within second long element
            longs[i / 64] |= (1L << (i % 64));
        }
        // clearing the given element i in the bit-set
        public void clear(int i) {
            // [i/64] -> calculate index in longs array
            // (1L << (i % 64)) -> calculate Bit position within array element
            // ~ -> Bit Not to negate operator
            longs[i / 64] &= ~(1L << (i % 64));
        }
        // preform XOR operation between 2 bit-sets to find # of differences
        // each element in bit-set represents node (x,y) we will finding excluded/included elements in given
        // R
        public void diff(BitSet bs) {
            assert bs.longs.length == longs.length;
            // iterate through each element in bit-set to calculate bits
            for (int i = 0; i < longs.length; i++) {
                longs[i] ^= bs.longs[i];
            }
        }

        public boolean ask(int i) {
            // checking if bit at specific index set to 1
            // checking for
            return (longs[i] & (1L << (i % 64))) != 0;
        }

        public void copyInto(BitSet bs) {
            assert bs.longs.length == longs.length;
            System.arraycopy(bs.longs, 0, longs, 0, longs.length);
        }

        public BitSet copy() {
            BitSet bs = new BitSet(longs.length);
            copyInto(bs);
            return bs;
        }
    }

    // floor power of 2: find the greatest power of 2 less than or equal to l.
    static long flp2(long l) {
        l = l | (l >>> 1);
        l = l | (l >>> 2);
        l = l | (l >>> 4);
        l = l | (l >>> 8);
        l = l | (l >>> 16);
        l = l | (l >>> 32);
        return l - (l >>> 1);
    }

    // ceiling power of 2: find least power of 2 greater than or equal to l.
    static long clp2(long l) {
        l = l - 1;
        l = l | (l >>> 1);
        l = l | (l >>> 2);
        l = l | (l >>> 4);
        l = l | (l >>> 8);
        l = l | (l >>> 16);
        l = l | (l >>> 32);
        return l + 1;
    }

    // Stateful solution search
    static class S {
        long[] proximity;
        long[] coordinates;
        // points that have been visited (manages states of each tower)
        BitSet visited;
        int N;

        public S(int n, long[] prx, long[] coords) {
            proximity = prx;
            coordinates = coords;
            visited = new BitSet(n);
            N = n;
        }

        // Given C = R^2 value, is the graph robust?
        boolean test(long c) {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        var BufferedReader = new BufferedReader(new FileReader(args[0]));
        // create StringBuilder object to build string
        var build = new StringBuilder();
        // store each line in file
        var line = "";
        //reading each line from file
        while ((line = BufferedReader.readLine()) != null) {
            //appending lines
            build.append(line).append(" ");
        }
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
//            System.out.print("x: " + x + " ");
            // retrieve y-coordinate
            int y = Integer.parseInt(tokens[k + 1]);
//            System.out.println("y: " + y + " ");
            // must do bit shift of 32 bits to extract y-coordinate
            // | -> using bit-wise OR to ensure no overlap between x and y coordinates
            // ((long)) x -> cast to long to ensure 64 bits
            coordinates[i] = ((long) y << 32) | ((long) x);
        }
        System.out.println();
        // indexing: row * N + column
        // if w is an element of proximity:
        //  squared distance: high 4 bytes
        //  index of the other point: low 4 bytes
        var proximity = new long[N * N];
        for (int i = 0; i < N; i++) {
            // retrieve the x,y values stored in our array of points
            long ixy = coordinates[i];
            // & 0xffffffL ->
            long ix = ixy & 0xfffffffL; // extract x-coordinate
            long iy = ixy >>> 32; // extract y-coordinate
//            System.out.println("Point i " + i + ": (" + ix + ", " + iy + ")");
            for (int j = 0; j < N; j++) {
                long jxy = coordinates[j];
                // retrieve x2, y2 values (other pair of points)
                long jx = jxy & 0xfffffffL;
                long jy = jxy >>> 32;
                // (x1 - y1)
                long dx = ix - jx;
                // (y1 - y2)
                long dy = iy - jy;
                // calculate euclidean distance
                long ds = (long) Math.sqrt(dx * dx + dy * dy);
                // [i * N + j]
                // (i * N): to calculate starting index of ith row
                // + j: to provide column number
                // (ds << 32) -> distance of (x,y) point stored in upper half
                // j -> index stored in lower half (will be always positive)
                proximity[i * N + j] = (ds << 32) | j;
               // System.out.println("Point j " + j + ": (" + jx + ", " + jy + ")");
              //  System.out.println("Distance: " + ds);
              //  System.out.println("Proximity[" + (i * N + j) + "]: " + proximity[i * N + j]);
            }
            // sort by distance and then index
            // i * N:
            // ex) N = 5, i = 0 -> proximity[0...4] will be sorted
            Arrays.sort(proximity, i * N, (i + 1) * N);
        }
        // debug check: print out the proximity matrix to see if correctly sorts.
        // Print proximity array for debugging
       for (int i = 0; i < N; i++) {
          System.out.println("Proximity for point " + i + ":");
           for (int j = 0; j < N; j++) {
                long value = proximity[i * N + j];
                int distance = (int) (value >>> 32);
                int index = (int) (value & 0xFFFFFFFFL);
                System.out.println("  Distance: " + distance + ", Index: " + index + ", Proximity: " + value);
            }
        }
        /* your code here to calculate the answer*/
        // solution search
        // N -> number of points (for bitSet)
        // proximity -> sorted distances of (x,y) points
        // coordinates -> contains (x,y) points
        S sol = new S(N, proximity, coordinates); // TODO: plug in all the arrays and stuff
        // two-phase exponential search
        long high = 128; // some good starting point
        long low = 0;
        // phase 1: find good range
        // low is excluded and high is included, or else
        // range is length 1.
        while (high >= low) {
            if (!sol.test(high)) {
                low = high;
                high *= 2;
            } else break;
        }
        // phase 2: refine the range
        while (low < high) {
            long m = (low + high) / 2;
            if (sol.test(m)) high = m;
            else low = m;
        }
        // an answer WILL exist and the loop above WILL terminate.
        assert low == high;
        System.out.println(low);
    }
}
