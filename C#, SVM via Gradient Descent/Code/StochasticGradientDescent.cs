/*
 * Philip Scuderi
 * Stanford University
 * CS246
 * Winter 2013
 * Homework 4
 * Question 1
 * 
 * StochasticGradientDescent.cs
 * This file defines the StochasticGradientDescent class, which implements GradientDescent.
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
    public class StochasticGradientDescent : GradientDescent
    {
        public StochasticGradientDescent(string featuresPath, string targetPath, double c = 100.0) : base(featuresPath, targetPath, 0.0001, 0.001, c)
        {
            // randomly permute the items
            Permutation p = GenerateRandomPermutation(n);
            x.Rows.Permute(p);
            y.Permute(p);
        }

        override public IList<Tuple<int, double, TimeSpan>> Iterate()
        {
            IList<Tuple<int, double, TimeSpan>> results = new List<Tuple<int, double, TimeSpan>>();

            int i = 1;
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
                    w[j] -= eta * GradientWrtWj(i-1, j);
                }

                b -= eta * GradientWrtB(i-1);
                

                // iteration ends, update the results
                currentCost = GetCost();
                double percentCostChange = (Math.Abs(lastCost - currentCost) * 100.0) / lastCost;
                deltaCost = 0.5 * lastDeltaCost + 0.5 * percentCostChange;

                results.Add(new Tuple<int, double, TimeSpan>(k, currentCost, stopwatch.Elapsed));


                if (deltaCost < epsilon)
                    break;


                i = (i % n) + 1;
                ++k;
                lastCost = currentCost;
                lastDeltaCost = deltaCost;
            } while (true);

            return results;
        }

        protected double GradientWrtB(int i)
        {
            double sum = 0.0;

            if (y[i] * (Vector.DotProduct(x.Rows[i], w) + b) < 1.0)
                sum += -y[i];

            return c * sum;
        }

        protected double GradientWrtWj(int i, int j)
        {
            double sum = 0.0;

            if (y[i] * (Vector.DotProduct(x.Rows[i], w) + b) < 1.0)
                sum += -y[i] * x[i, j];

            return w[j] + (c * sum);
        }
    }
}
