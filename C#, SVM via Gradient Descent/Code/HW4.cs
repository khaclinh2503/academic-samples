/*
 * Philip Scuderi
 * Stanford University
 * CS246
 * Winter 2013
 * Homework 4
 * Question 1
 * 
 * HW4.cs
 * This file is the main (top level) program.
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
    class HW4
    {
        static void Main(string[] args)
        {
            Q1e();
            Q1f();

            Console.WriteLine("Press any key to continue...");
            Console.ReadKey();
        }

        static void Q1e()
        {
            BatchGradientDescent();
            StochasticGradientDescent();
            MiniBatchGradientDescent();
        }

        static void Q1f()
        {
            string dataDirectory = "C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW4\\Q1\\HW4-q1\\";

            Console.WriteLine("C\tTime Elapsed\t\tError");

            foreach (double c in new double[] { 1, 10, 50, 100, 200, 300, 400, 500 })
            {
                GradientDescent gd = new StochasticGradientDescent(dataDirectory + "features.train.txt", dataDirectory + "target.train.txt", c);

                var results = gd.Iterate();

                var timeElapsed = results[results.Count - 1].Item3;
                var error = gd.GetError(dataDirectory + "features.test.txt", dataDirectory + "target.test.txt");

                Console.WriteLine("{0}\t{1}\t{2}", gd.C, timeElapsed, error);
            }
        }

        static void BatchGradientDescent()
        {
            string dataDirectory = "C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW4\\Q1\\HW4-q1\\";

            GradientDescent gd = new BatchGradientDescent(dataDirectory + "features.txt", dataDirectory + "target.txt");

            using (System.IO.StreamWriter file = new System.IO.StreamWriter("C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW4\\Q1\\output\\BGD.txt"))
            {
                file.WriteLine(" k\t      Cost\t\t   Time\n--\t----------------\t----------");
                Console.WriteLine(" k\t      Cost\t\t   Time\n--\t----------------\t----------");

                foreach (var result in gd.Iterate())
                {
                    file.WriteLine("{0}\t{1}\t{2}", result.Item1.ToString().PadLeft(2), result.Item2.ToString().PadRight(16), FormatSeconds(result.Item3.TotalSeconds));
                    Console.WriteLine("{0}\t{1}\t{2}", result.Item1.ToString().PadLeft(2), result.Item2.ToString().PadRight(16), FormatSeconds(result.Item3.TotalSeconds));
                }
            }
        }

        static void StochasticGradientDescent()
        {
            string dataDirectory = "C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW4\\Q1\\HW4-q1\\";

            GradientDescent gd = new StochasticGradientDescent(dataDirectory + "features.txt", dataDirectory + "target.txt");

            Stopwatch stopwatch = new Stopwatch();
            var results = gd.Iterate();

            using (System.IO.StreamWriter file = new System.IO.StreamWriter("C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW4\\Q1\\output\\SGD.txt"))
            {
                Console.WriteLine("  k \t      Cost\t\t   Time\n----\t----------------\t----------");
                file.WriteLine("  k \t      Cost\t\t   Time\n----\t----------------\t----------");

                foreach (var result in results)
                {
                    if (result.Item1 == 1 || result.Item1 == results.Count || (result.Item1 % 100 == 0))
                    {
                        Console.WriteLine("{0}\t{1}\t{2}", result.Item1.ToString().PadLeft(4), result.Item2.ToString().PadRight(16), FormatSeconds(result.Item3.TotalSeconds));
                    
                    }

                    file.WriteLine("{0}\t{1}\t{2}", result.Item1.ToString().PadLeft(3), result.Item2.ToString().PadRight(16), FormatSeconds(result.Item3.TotalSeconds));
                }
            }

            Console.WriteLine("\nStochastic Gradient Descent Complete.\n");
        }

        static void MiniBatchGradientDescent()
        {
            string dataDirectory = "C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW4\\Q1\\HW4-q1\\";

            GradientDescent gd = new MiniBatchGradientDescent(dataDirectory + "features.txt", dataDirectory + "target.txt");

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.Start();
            var results = gd.Iterate();
            stopwatch.Stop();

            using (System.IO.StreamWriter file = new System.IO.StreamWriter("C:\\Dropbox\\Private\\Stanford\\CS246\\Homeworks\\HW4\\Q1\\output\\MBGD.txt"))
            {
                file.WriteLine(" k \t      Cost\t\t   Time\n---\t----------------\t----------");
                Console.WriteLine(" k \t      Cost\t\t   Time\n---\t----------------\t----------");

                foreach (var result in results)
                {
                    file.WriteLine("{0}\t{1}\t{2}", result.Item1.ToString().PadLeft(3), result.Item2.ToString().PadRight(16), FormatSeconds(result.Item3.TotalSeconds));

                    if (result.Item1 % 10 == results.Count % 10)
                    {
                        Console.WriteLine("{0}\t{1}\t{2}", result.Item1.ToString().PadLeft(3), result.Item2.ToString().PadRight(16), FormatSeconds(result.Item3.TotalSeconds));
                    }
                }
            }

            Console.WriteLine("\nMini Batch Gradient Descent Complete.\n# of Iterations = " + results.Count + "\nTotal Time = " + stopwatch.Elapsed + "\n");
        }

        static string FormatSeconds(double seconds)
        {
            string s = seconds.ToString();

            if (seconds < 10.0)
                s = " " + s;

            return s.PadRight(10);
        }
    }
}