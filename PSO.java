import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PSO {

    private int bestNum;
    private float w;
    private int MAX_GEN;// iteration time
    private int scale;// particle num

    private int pointNum; // point num
    private int t;// current generation

    private int begin;// start point

    private int[][] distance; // matrix of distance

    private int[][] oPopulation;// particle swarm
    private ArrayList<ArrayList<SO>> listV;// swap list of each particle

    private int[][] Pd;// best solution of each particle among all generations
    private int[] vPd;// evaluation value of best solution

    private int[] Pgd;// global best solution

    public int[] getPgd() {

        return Pgd;
    }

    public int getvPgd() {

        return vPgd;
    }

    private int vPgd;// evaluation value of global best solution
    private int bestT;// best generation

    private int[] fitness;

    private Random random;

    ExecutorService executorService = Executors.newFixedThreadPool(50);

    public PSO(int cityNum, int g, int s, float w, int b) {
        this.pointNum = cityNum;
        this.MAX_GEN = g;
        this.scale = s;
        this.w = w;
        this.begin = b;
    }

    public void init(String filename) throws IOException {
        int[] x;
        int[] y;
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));
        distance = new int[pointNum][pointNum];
        x = new int[pointNum];
        y = new int[pointNum];

        distance[pointNum - 1][pointNum - 1] = 0;

        oPopulation = new int[scale][pointNum];
        fitness = new int[scale];

        // individual
        Pd = new int[scale][pointNum];
        vPd = new int[scale];

        // global
        Pgd = new int[pointNum];
        vPgd = Integer.MAX_VALUE;

        bestT = 0;
        t = 0;

        random = new Random(System.currentTimeMillis());

        for (int i = 0; i < pointNum; i++) {
            strbuff = data.readLine();
            String[] strcol = strbuff.split(" ");
            x[i] = Integer.valueOf(strcol[1]);// x
            y[i] = Integer.valueOf(strcol[2]);// y
        }

        // calculate distance between points
        for (int i = 0; i < pointNum - 1; i++) {
            distance[i][i] = 0; 
            for (int j = i + 1; j < pointNum; j++) {
                double rij = Math.sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j])) / 10.0);
                int tij = (int) Math.round(rij);
                if (tij < rij) {
                    distance[i][j] = tij + 1;
                    distance[j][i] = distance[i][j];
                } else {
                    distance[i][j] = tij;
                    distance[j][i] = distance[i][j];
                }
            }
        }
    }

    // initialize particle swarm
    void initGroup() {
        int i, j, k;
        for (k = 0; k < scale; k++) // swarm num
        {
            // start point
            oPopulation[k][0] = begin;
            for (i = 1; i < pointNum; ) // particle num
            {
                oPopulation[k][i] = random.nextInt(65535) % pointNum;
                for (j = 0; j < i; j++) {
                    if (oPopulation[k][i] == oPopulation[k][j] || oPopulation[k][i] == begin) {
                        break;
                    }
                }
                if (j == i) {
                    i++;
                }
            }
        }
    }

    // initialize swapping list of each particle
    void initListV() {
        int ra;
        int ra1;
        int ra2;

        listV = new ArrayList<ArrayList<SO>>();

        for (int i = 0; i < scale; i++) {
            ArrayList<SO> list = new ArrayList<SO>();
            ra = random.nextInt(65535) % pointNum;
            for (int j = 0; j < ra; j++) {
                ra1 = random.nextInt(65535) % pointNum;
                while (ra1 == 0) {
                    ra1 = random.nextInt(65535) % pointNum;
                }
                ra2 = random.nextInt(65535) % pointNum;
                while (ra1 == ra2 || ra2 == 0) {
                    ra2 = random.nextInt(65535) % pointNum;
                }

                SO S = new SO(ra1, ra2);
                list.add(S);
            }

            listV.add(list);
        }
    }

    public int evaluateLength(int[] chr) {
        int len = 0;
        // point 1, 2, 3...
        for (int i = 1; i < pointNum; i++) {
            len += distance[chr[i - 1]][chr[i]];
        }
        len += distance[chr[pointNum - 1]][chr[0]];
        return len;
    }

    public void add(int[] arr, ArrayList<SO> list) {
        int temp = 0;
        SO S;
        for (int i = 0; i < list.size(); i++) {
            S = list.get(i);
            temp = arr[S.getX()];
            arr[S.getX()] = arr[S.getY()];
            arr[S.getY()] = temp;
        }
    }

    // get swapping list from b to a
    public ArrayList<SO> minus(int[] a, int[] b) {
        int[] temp = b.clone();
        int index;
        // swapping unit
        SO S;
        // swapping list
        ArrayList<SO> list = new ArrayList<SO>();
        for (int i = 0; i < pointNum; i++) {
            if (a[i] != temp[i]) {
                // find the same index as a[i] in temp[]
                index = findNum(temp, a[i]);
                // change i and index in temp[]
                changeIndex(temp, i, index);
                // record swapping unit
                S = new SO(i, index);
                // save swapping unit
                list.add(S);
            }
        }
        return list;
    }

    public int findNum(int[] arr, int num) {
        int index = -1;
        for (int i = 0; i < pointNum; i++) {
            if (arr[i] == num) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void changeIndex(int[] arr, int index1, int index2) {
        int temp = arr[index1];
        arr[index1] = arr[index2];
        arr[index2] = temp;
    }

    // 二维数组拷贝
    public void copyarray(int[][] from, int[][] to) {
        for (int i = 0; i < scale; i++) {
            for (int j = 0; j < pointNum; j++) {
                to[i][j] = from[i][j];
            }
        }
    }

    // 一维数组拷贝
    public void copyarrayNum(int[] from, int[] to) {
        for (int i = 0; i < pointNum; i++) {
            to[i] = from[i];
        }
    }

    private void particle(int i) {
        ArrayList<SO> Vi;
        int len;
        int j;
        float ra;
        float rb;
        ArrayList<SO> Vii = new ArrayList<SO>();

        // refresh velocity
        // Vii=wVi+ra(Pid-Xid)+rb(Pgd-Xid)
        Vi = listV.get(i);

        // wVi+表示获取Vi中size*w取整个交换序列
        len = (int) (Vi.size() * w);

        for (j = 0; j < len; j++) {
            Vii.add(Vi.get(j));
        }

        // Pid-Xid
        ArrayList<SO> a = minus(Pd[i], oPopulation[i]);
        ra = random.nextFloat();

        // ra(Pid-Xid)
        len = (int) (a.size() * ra);

        for (j = 0; j < len; j++) {
            Vii.add(a.get(j));
        }

        // Pgd-Xid
        ArrayList<SO> b = minus(Pgd, oPopulation[i]);
        rb = random.nextFloat();

        // rb(Pgd-Xid)
        len = (int) (b.size() * rb);

        for (j = 0; j < len; j++) {
            SO tt = b.get(j);
            Vii.add(tt);
        }

        // save new Vii
        listV.set(i, Vii);

        // refresh position
        // Xid’=Xid+Vid
        add(oPopulation[i], Vii);
    }

    public void evolution() {
        int i, j, k;
        int len = 0;
        float ra = 0f;

        ArrayList<SO> Vi;

        for (t = 0; t < MAX_GEN; t++) {
            // create concurrent threads to record particles' movement
            ArrayList<Callable<Void>> runnables = new ArrayList<>();
            for (i = 0; i < scale; i++) {
                if (i == bestNum) continue;

                final int ii = i;
                runnables.add(new Callable<Void>() {
                    @Override
                    public Void call() {
                        particle(ii);
                        return null;
                    }
                });

            }
            try {
                List<Future<Void>> futures = executorService.invokeAll(runnables);
                for (Future<Void> future : futures) {
                    future.get();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            // calculate fitness value of new swarm, get best solution
            for (k = 0; k < scale; k++) {
                fitness[k] = evaluateLength(oPopulation[k]);
                if (vPd[k] > fitness[k]) {
                    vPd[k] = fitness[k];
                    copyarrayNum(oPopulation[k], Pd[k]);
                    bestNum = k;
                }
                if (vPgd > vPd[k]) {
                    System.out.println("Shortest distance: " + vPgd + " Generation: " + bestT);
                    bestT = t;
                    vPgd = vPd[k];
                    copyarrayNum(Pd[k], Pgd);
                }
            }
        }
    }

    public void solve() {
        int i;
        int k;

        initGroup();
        initListV();

        // make each particle remember its own best solution
        copyarray(oPopulation, Pd);

        for (k = 0; k < scale; k++) {
            fitness[k] = evaluateLength(oPopulation[k]);
            vPd[k] = fitness[k];
            if (vPgd > vPd[k]) {
                vPgd = vPd[k];
                copyarrayNum(Pd[k], Pgd);
                bestNum = k;
            }
        }

        System.out.println("Initial particle swarm...");
        for (k = 0; k < scale; k++) {
            for (i = 0; i < pointNum; i++) {
                System.out.print(oPopulation[k][i] + ",");
            }
            System.out.println();
            System.out.println("----" + fitness[k]);
        }

        evolution();

        System.out.println("Final particle swarm...");
        for (k = 0; k < scale; k++) {
            for (i = 0; i < pointNum; i++) {
                System.out.print(oPopulation[k][i] + ",");
            }
            System.out.println();
            System.out.println("----" + fitness[k]);
        }

        System.out.println("Best generation: ");
        System.out.println(bestT);
        System.out.println("Shortest distance: ");
        System.out.println(vPgd);
        System.out.println("Best path: ");
        for (i = 0; i < pointNum; i++) {
            System.out.print(Pgd[i] + ",");
        }

    }

}
