/*
 * Philip Scuderi
 * Stanford University
 * CS246
 * Winter 2013
 * Homework 4
 * Question 1
 * 
 * GradientDescent.cs
 * This file defines the abstract class GradientDescent.
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;
using Extreme.Mathematics;
using Extreme.Mathematics.LinearAlgebra;

namespace HW4
{
    public abstract class GradientDescent
    {
        protected Matrix x;
        protected Vector w, y;
        protected double b, eta, epsilon, c;
        protected int n, d;

        public abstract IList<Tuple<int, double, TimeSpan>> Iterate();

        public GradientDescent(string featuresPath, string targetPath, double eta, double epsilon, double c)
        {
            x = ReadMatrix(featuresPath);
            y = ReadVector(targetPath);

            n = x.RowCount;
            d = x.ColumnCount;

            w = Vector.Create(d);

            Reset();

            this.c = c;
            this.eta = eta;
            this.epsilon = epsilon;
        }

        public void Reset()
        {
            for (int i = 0; i < d; i++)
                w[i] = 0.0;

            b = 0.0;
        }

        public double C
        {
            get { return c; }
        }

        public double GetError(string testFeaturesPath, string testTargetPath)
        {
            Matrix testFeatures = ReadMatrix(testFeaturesPath);
            Vector testTargets = ReadVector(testTargetPath);

            int numSuccessful = 0;

            for (int i = 0; i < testFeatures.Rows.Count; i++)
            {
                double prediction = GetPrediction(testFeatures.Rows[i]);

                if (prediction <= 0.0 && testTargets[i] <= 0.0)
                    ++numSuccessful;
                else
                {
                    if (prediction >= 0.0 && testTargets[i] >= 0.0)
                        ++numSuccessful;
                }
            }

            return 1.0 - ((double)numSuccessful / (double)testFeatures.Rows.Count);
        }

        protected double GetPrediction(Vector xi)
        {
            return Vector.DotProduct(w, xi) + b;
        }

        protected double GetCost()
        {
            double left = 0.0;
            for (int j = 0; j < d; j++)
            {
                left += Math.Pow(w[j], 2.0);
            }
            left *= 0.5;

            double right = 0.0;
            for (int i = 0; i < n; i++)
            {
                right += Math.Max(0.0, 1.0 - (y[i] * (Vector.DotProduct(w, x.Rows[i]) + b)));
            }
            right *= c;


            return left + right;
        }

        protected static Matrix ReadMatrix(string filePath)
        {
            int n = 0;
            int d = 0;

            using (StreamReader reader = new StreamReader(filePath))
            {
                string line;
                while ((line = reader.ReadLine()) != null)
                {
                    d = Math.Max(d, line.Split(',').Length);
                    ++n;
                }
            }

            Matrix m = Matrix.Create(n, d);

            int lineNum = 0;
            using (StreamReader reader = new StreamReader(filePath))
            {
                string line;
                while ((line = reader.ReadLine()) != null)
                {
                    double[] numbers = Array.ConvertAll<string, double>(line.Split(','), delegate(string s) { return double.Parse(s); });

                    for (int i = 0; i < numbers.Length; i++)
                        m[lineNum, i] = numbers[i];

                    ++lineNum;
                }
            }

            return m;
        }

        protected static Vector ReadVector(string filePath)
        {
            int n = 0;

            using (StreamReader reader = new StreamReader(filePath))
            {
                string line;
                while ((line = reader.ReadLine()) != null)
                    ++n;
            }

            Vector v = Vector.Create(n);

            int lineNum = 0;
            using (StreamReader reader = new StreamReader(filePath))
            {
                string line;
                while ((line = reader.ReadLine()) != null)
                {
                    v[lineNum] = double.Parse(line);

                    ++lineNum;
                }
            }

            return v;
        }

        protected static Permutation GenerateRandomPermutation(int n)
        {
            var list = new List<int>(n);

            for (int i = 0; i < n; i++)
                list.Add(i);

            var randomizedList = new List<int>(n);
            var rnd = new Random();
            while (list.Count != 0)
            {
                var index = rnd.Next(0, list.Count);
                randomizedList.Add(list[index]);
                list.RemoveAt(index);
            }

            int[] randomizedIndicies = randomizedList.ToArray();
            Permutation p = new Permutation(randomizedIndicies);
            return p;
        }
    }
}
