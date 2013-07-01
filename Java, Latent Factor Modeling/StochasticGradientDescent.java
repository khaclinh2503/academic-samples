/*
 * Philip Scuderi
 * Stanford University
 * CS246
 * Winter 2013
 * Homework 3
 * Question 1
 */

package HW3_Q1c;

import java.io.*;
import java.util.*;
import Jama.Matrix;

public class StochasticGradientDescent
{
    protected String rTrainingPath, rTestPath;
    protected double lambda, eta, trainingError, regularizationAdjustment;
    protected Matrix p, q;
    protected int m, n, k;

    public static void main(String[] args) throws IOException
    {
        String path = "C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW3\\Q1\\";
        String rTrainingFile = "ratings.train.txt";
        String rTestFile = "ratings.val.txt";
        String rTrainingPath = path + rTrainingFile;
        String rTestPath = path + rTestFile;


        double eta = 0.02;
        int iterations = 40;


        double lambda = 0.0;

        System.out.println("lambda = " + lambda + ":");
        for (int k = 1; k <= 10; k++)
        {
            StochasticGradientDescent sgd = new StochasticGradientDescent(eta, lambda, k, rTrainingPath, rTestPath);
            sgd.Iterate(iterations);
            System.out.println(k + "\t" + sgd.GetTrainingError() + "\t" + sgd.GetTestError());
        }
        System.out.println();


        lambda = 0.2;

        System.out.println("lambda = " + lambda + ":");
        for (int k = 1; k <= 10; k++)
        {
            StochasticGradientDescent sgd = new StochasticGradientDescent(eta, lambda, k, rTrainingPath, rTestPath);
            sgd.Iterate(iterations);
            System.out.println(k + "\t" + sgd.GetTrainingError() + "\t" + sgd.GetTestError());
        }
        System.out.println();
    }

    public StochasticGradientDescent(double eta, double lambda, int k, String rTrainingPath, String rTestPath) throws FileNotFoundException, IOException
    {
        trainingError = regularizationAdjustment = Double.NaN;

        this.lambda = lambda;
        this.k = k;
        this.eta = eta;
        this.rTrainingPath = rTrainingPath;
        this.rTestPath = rTestPath;

        // initialize m and n
        BufferedReader reader = new BufferedReader(new FileReader(rTrainingPath));
        String line;
        while ( (line = reader.readLine()) != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(line);

            int u = Integer.parseInt(tokenizer.nextToken());
            int i = Integer.parseInt(tokenizer.nextToken());

            if (u > n)
                n = u;

            if (i > m)
                m = i;
        }
        reader.close();

        Random r = new Random();

        // initialize p
        p = new Matrix(n, k);
        for (int row = 0; row < n; row++)
            for (int col = 0; col < k; col++)
                p.set(row, col, r.nextDouble() * Math.pow(5.0 / (double)k, 0.5));

        // initialize q
        q = new Matrix(m, k);
        for (int row = 0; row < m; row++)
            for (int col = 0; col < k; col++)
                q.set(row, col, r.nextDouble() * Math.pow(5.0 / (double)k, 0.5));
    }

    public void Iterate(int numIterations) throws FileNotFoundException, IOException
    {
        for (int a = 0; a < numIterations; a++)
        {
            trainingError = 0.0;

            BufferedReader reader = new BufferedReader(new FileReader(rTrainingPath));
            String line;
            while ( (line = reader.readLine()) != null)
            {
                StringTokenizer tokenizer = new StringTokenizer(line);

                int u = Integer.parseInt(tokenizer.nextToken());
                int i = Integer.parseInt(tokenizer.nextToken());
                int Riu = Integer.parseInt(tokenizer.nextToken());

                Matrix Qi = GetRow(q, i-1);
                Matrix Pu = GetRow(p, u-1);

                double Eiu = Riu - DotProduct(Qi, Pu);

                Matrix nextQi = Qi.plus(Pu.times(Eiu).minus(Qi.times(lambda)).times(eta));
                Matrix nextPu = Pu.plus(Qi.times(Eiu).minus(Pu.times(lambda)).times(eta));

                SetRow(q, i-1, nextQi);
                SetRow(p, u-1, nextPu);

                trainingError += Math.pow(Eiu, 2.0);
            }
            reader.close();


            regularizationAdjustment = 0.0;

            // finish determing the error (determine right side of the equation)
            for (int i = 0; i < m; i++)
                regularizationAdjustment += lambda * Math.pow(GetRow(q, i).norm2(), 2.0);

            for (int u = 0; u < n; u++)
                regularizationAdjustment += lambda * Math.pow(GetRow(p, u).norm2(), 2.0);
        }
    }

    public double GetError()
    {
        return trainingError + regularizationAdjustment;
    }

    public double GetTrainingError()
    {
        return trainingError;
    }

    public double GetTestError() throws FileNotFoundException, IOException
    {
        double testError = 0.0;

        BufferedReader reader = new BufferedReader(new FileReader(rTestPath));
        String line;
        while ( (line = reader.readLine()) != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(line);

            int u = Integer.parseInt(tokenizer.nextToken());
            int i = Integer.parseInt(tokenizer.nextToken());
            int Riu = Integer.parseInt(tokenizer.nextToken());

            Matrix Qi = GetRow(q, i-1);
            Matrix Pu = GetRow(p, u-1);

            double Eiu = Riu - DotProduct(Qi, Pu);

            testError += Math.pow(Eiu, 2.0);
        }
        reader.close();

        return testError;
    }

    // only for same size vectors (1 row matricies of the same length)
    protected static double DotProduct(Matrix m1, Matrix m2)
    {
        double dotProduct = 0.0;

        for (int i = 0; i < m1.getColumnDimension(); i++)
            dotProduct += m1.get(0, i) * m2.get(0, i);

        return dotProduct;
    }

    protected static void SetRow(Matrix m, int row, Matrix val)
    {
        m.setMatrix(row, row, 0, m.getColumnDimension()-1, val);
    }

    protected static void SetColumn(Matrix m, int column, Matrix val)
    {
        m.setMatrix(0, m.getColumnDimension()-1, column, column, val);
    }

    protected static Matrix GetRow(Matrix m, int row)
    {
        return m.getMatrix(row, row, 0, m.getColumnDimension()-1);
    }

    protected static Matrix GetColumn(Matrix m, int column)
    {
        return m.getMatrix(0, m.getRowDimension()-1, column, column);
    }
}
