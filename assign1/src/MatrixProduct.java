import java.util.Scanner;

public class MatrixProduct {

    static double[] pha, phb, phc;
    static long startTime, endTime;

    public static void onMult(int m_ar, int m_br) {
        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = 1.0;

        for (int i = 0; i < m_br; i++)
            for (int j = 0; j < m_br; j++)
                phb[i * m_br + j] = i + 1;

        startTime = System.nanoTime();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                double temp = 0;
                for (int k = 0; k < m_ar; k++) {
                    temp += pha[i * m_ar + k] * phb[k * m_br + j];
                }
                phc[i * m_ar + j] = temp;
            }
        }

        endTime = System.nanoTime();

        double elapsedTime = (endTime - startTime) / 1e9; // Convert to seconds
        System.out.println("Time: " + elapsedTime + " seconds");

        System.out.println("Result matrix:");
        for (int j = 0; j < Math.min(10, m_br); j++)
            System.out.print(phc[j] + " ");
        System.out.println();
    }

    public static void onMultLine(int m_ar, int m_br) {
        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = 1.0;

        for (int i = 0; i < m_br; i++)
            for (int j = 0; j < m_br; j++)
                phb[i * m_br + j] = i + 1;

        startTime = System.nanoTime();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                for (int k = 0; k < m_ar; k++) {
                    phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_br + k];
                }
            }
        }

        endTime = System.nanoTime();

        double elapsedTime = (endTime - startTime) / 1e9; // Convert to seconds
        System.out.println("Time: " + elapsedTime + " seconds");

        System.out.println("Result matrix:");
        for (int j = 0; j < Math.min(10, m_br); j++)
            System.out.print(phc[j] + " ");
        System.out.println();
    }

    public static void onMultBlock(int m_ar, int m_br, int bkSize) {

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int lin, col, blockSize;
        int op;

        do {
            System.out.println("\nJAVA VERSION");
            System.out.println("1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.print("Selection?: ");
            op = scanner.nextInt();
            if (op == 0)
                break;
            System.out.print("Dimensions: lins=cols ? ");
            lin = scanner.nextInt();
            col = lin;

            switch (op) {
                case 1:
                    onMult(lin, col);
                    break;
                case 2:
                    onMultLine(lin, col);
                    break;
                case 3:
                    System.out.print("Block Size? ");
                    blockSize = scanner.nextInt();
                    onMultBlock(lin, col, blockSize);
                    break;
            }

        } while (op != 0);

        scanner.close();
    }
}