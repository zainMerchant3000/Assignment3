
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
    static int N; // size of thing
    static int[] ss, qs;

    // Class that represents edge in given adjancency list
    class Edge {
        // v,w (two vertices that represent the edge)
        private final int v;
        private final int w;
        private final double weight; // weight of each edge

        public Edge(int v, int w, double weight) {
            this.v = v;
            this.w = w;
            this.weight = weight;
        }
        //returns one of the vertices in the edge
        public int either() {
            return v;
        }
        //returns the other vertex in the edge
        // ex:
        // Edge edge = new Edge(2,3,1.5)
        public int other(int vertex) {
            // return other vertice in the edge
            if (vertex == v) return w;
            // must be opposite case
            else if (vertex == w) return v;
            //otherwise vertex is not valid
            else return -1;
        }

        public double weight() {
            return weight;
        }
    }

    class Bag<Item> implements Iterable<Item> {
        // storing items in bag in LinkedList
        private LinkedList<Item> items = new LinkedList<Item>();

        //Constructor to initialize LinkedList
        public Bag() {
            items = new LinkedList<>();
        }

        public void add(Item item) {
            items.add(item);
        }
        public boolean isEmpty() {
            return items.isEmpty();
        }
        //getting number of items in bag
        public int size() {
            return items.size();
        }

        @Override
        public Iterator<Item> iterator() {
            return items.iterator();
        }
    }
    // create Graph Class that stores
    // V: number of vertices (#of towers?)
    //
    class EdgeWeightedGraph {
        private final int V; // Number of vertices
        private Bag<Edge>[]adj; // adjancency list
        private int E;

        // Constructor to
        public EdgeWeightedGraph(int V) {
            this.V = V;
            adj = (Bag<Edge>[]) new Bag[V];
            for (int v = 0; v < V; v++) {
                adj[v] = new Bag<Edge>();
            }
        }

        public void addEdge(Edge e) {
            int v = e.either();
            int w = e.other(v);
            adj[v].add(e);
            adj[w].add(e);
            E++;
        }

        public Iterable<Edge> edges() {
            return adj[V];
        }

        public int V() {
            return V;
        }


    }


    // returns: [rank] -> [value in ps]
    static HashMap<Integer, Integer> compressx(int[][] ps, int coord) {
        for (int i = 0; i < N; i++)
            // ps[i][0] -> retrieving x coordinate of each point
            // ps[i][1] -> retrieving y coordinate of each point
            // ex) ps[0][0] = 0, ps[1][0] = 2 ... etc)
            qs[i] = ps[i][coord];

        // //System.out.println("qs[i]: " + Arrays.toString(qs));
        // sort the x and y coordinates
        Arrays.sort(qs);
        //  //System.out.println(" Sorted qs[i]: " + Arrays.toString(qs));
        // create mapping of coordinates
        // ex) m = {0: 0, 1:1}
        var m = new HashMap<Integer, Integer>();
        for (int i = 0; i < N; i++) {
            // (populate map with sorted x and y coordinates)
            Integer previousValue =  m.put(qs[i], i);
            // //System.out.println("Inserted (" + qs[i] + ", " + i + "), previous value: " + previousValue);
        }
        return m;
    }

    // .[0] -> the scalar; .[1] -> order as found in original order
    static void compress(int[][] points) {
        qs = new int[N];
        // compress both x and y coordinates
        var xs = compressx(points, 0);
        var ys = compressx(points, 1);
        // Print xs
        /*
        //System.out.println("xs:");
        for (Map.Entry<Integer, Integer> entry : xs.entrySet()) {
            //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

         */


        /*
        // Print ys
        //System.out.println("ys:");
        for (Map.Entry<Integer, Integer> entry : ys.entrySet()) {
            //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

         */


        for (int i = 0; i < N; i++) {
            // iterate and update each point to compressed x-y values
            // i = 0
            // xs.get(points[i][0]) -> look up x-coordinate in map to find value
            // xs.get(points[0][i]) -> look up y-coordinate in map to find value
            //  //System.out.println("points[" + i + "][0]: " + points[i][0]);
            //  //System.out.println("points[" + i + "][1]: " + points[i][1]);
            Integer xValue = xs.get(points[i][0]);
            Integer yValue = ys.get(points[i][1]);
            //  //System.out.println("compressed x-point: " + xValue + ", compressed y-point: " + yValue);
            points[i][0] = xValue;
            points[i][1] = yValue;
        }
        /*
        for (int i = 0; i < N; i++) {
            //System.out.println("Index: " + i + ", points[i][0]: " + points[i][0] + ", points[i][1]: " + points[i][1]);
        }
        //System.out.println();
        */


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
        N = Integer.parseInt(tokens[0]);
        // create 2D array points
        var points = new int[N][N];
        // k = 1 -> start at line 1 to process each pair of points
        // Integer.parseInt(tokens[k]) -> provides us value of points in given line
        for (int i = 0, k = 1; i < N; i++) {
            for (int j = 0; j < 2; j++, k++) {
                // points[0][0] = 0
                // points[0][1] = 3
                points[i][j] = Integer.parseInt(tokens[k]);
            }
        }
        compress(points);
        // call edge-Weighted Graph:
        // N -> number of points
       // Graph g = new Graph(N);

        long answer= 0;
        /* your code here to calculate the answer*/

        System.out.println(answer);
    }

    /// Plan: need a method that calculates the distance between 2 towers
    ///  Tower a, Tower b
    ///  Math.sqrt((a.x - b.x)) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
