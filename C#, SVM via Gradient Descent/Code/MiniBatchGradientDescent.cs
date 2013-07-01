/*
 * Philip Scuderi
 * Stanford University
 * CS246
 * Winter 2013
 * Homework 4
 * Question 1
 * 
 * MiniBatchGradientDescent.cs
 * This file defines the MiniBatchGradientDescent class, which implements GradientDescent.
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
    public class MiniBatchGradientDescent : GradientDescent
    {
        protected int batchSize;

        public MiniBatchGradientDescent(string featuresPath, string targetPath, int batchSize = 10, double c = 100.0) : base(featuresPath, targetPath, 0.000001, 0.01, c)
        {
            this.batchSize = batchSize;

            // randomly permute the items
            Permutation p = GenerateRandomPermutation(n);
            x.Rows.Permute(p);
            y.Permute(p);
        }

        override public IList<Tuple<int, double, TimeSpan>> Iterate()
        {
            IList<Tuple<int, double, TimeSpan>> results = new List<Tuple<int, double, TimeSpan>>();

            int l = 1;
            int k = 1;

            double lastCost = GetCost();
            double currentCost;

            double lastDeltaCost = 0.0;
            double deltaCost;

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.Start();

            do
            {
                for (int j = 0; j < d; j++)
                {
                    w[j] -= eta * GradientWrtWj(l, j);
                }

                b -= eta * GradientWrtB(l);


                // iteration ends, update the results
                currentCost = GetCost();
                double percentCostChange = (Math.Abs(lastCost - currentCost) * 100.0) / lastCost;
                deltaCost = 0.5 * lastDeltaCost + 0.5 * percentCostChange;

                results.Add(new Tuple<int, double, TimeSpan>(k, currentCost, stopwatch.Elapsed));


                if (deltaCost < epsilon)
                    break;


                l = (l + 1) % ((n + batchSize - 2) / batchSize);
                ++k;
                lastCost = currentCost;
                lastDeltaCost = deltaCost;
            } while (true);

            return results;
        }

        protected double GradientWrtWj(int l, int j)
        {
            double sum = 0.0;

            for (int i = l*batchSize; i < Math.Min(n, (l+1)*batchSize); i++)
            {
                if (y[i] * (Vector.DotProduct(x.Rows[i], w) + b) < 1.0)
                    sum += -y[i] * x[i, j];
            }

            return w[j] + (c * sum);
        }

        protected double GradientWrtB(int l)
        {
            double sum = 0.0;

            for (int i = l * batchSize; i < Math.Min(n, (l + 1) * batchSize); i++)
            {
                if (y[i] * (Vector.DotProduct(x.Rows[i], w) + b) < 1.0)
                    sum += -y[i];
            }

            return c * sum;
        }
    }
}
