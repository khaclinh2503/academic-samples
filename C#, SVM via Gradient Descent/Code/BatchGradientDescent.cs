/*
 * Philip Scuderi
 * Stanford University
 * CS246
 * Winter 2013
 * Homework 4
 * Question 1
 * 
 * BatchGradientDescent.cs
 * This file defines the BatchGradientDescent class, which implements GradientDescent.
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
    public class BatchGradientDescent : GradientDescent
    {
        public BatchGradientDescent(string featuresPath, string targetPath, double c = 100.0) : base(featuresPath, targetPath, 0.0000003, 0.25, c)
        { }

        override public IList<Tuple<int, double, TimeSpan>> Iterate()
        {
            IList<Tuple<int, double, TimeSpan>> results = new List<Tuple<int, double, TimeSpan>>();

            int k = 1;
            double lastCost = double.NaN;
            double currentCost;

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.Start();

            do
            {
                currentCost = GetCost();

                results.Add(new Tuple<int, double, TimeSpan>(k, currentCost, stopwatch.Elapsed));

                if (!double.IsNaN(lastCost))
                {
                    double percentCostChange = (Math.Abs(lastCost - currentCost) * 100.0) / lastCost;

                    if (percentCostChange < epsilon)
                        break;
                }

                for (int j = 0; j < d; j++)
                {
                    w[j] -= eta * GradientWrtWj(j);
                }

                b -= eta * GradientWrtB();

                ++k;
                lastCost = currentCost;
            } while (true);

            return results;
        }

        protected double GradientWrtB()
        {
            double sum = 0.0;

            for (int i = 0; i < n; i++)
            {
                if (y[i] * (Vector.DotProduct(x.Rows[i], w) + b) < 1.0)
                    sum += -y[i];
            }

            return c * sum;
        }

        protected double GradientWrtWj(int j)
        {
            double sum = 0.0;

            for (int i = 0; i < n; i++)
            {
                if (y[i] * (Vector.DotProduct(x.Rows[i], w) + b) < 1.0)
                    sum += -y[i] * x[i, j];
            }

            return w[j] + (c * sum);
        }
    }
}
