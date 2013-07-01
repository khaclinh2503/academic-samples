// Homework3.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "PreferentialAttachmentModel.h"
#include "WordDistribution.h"

#include <iostream>
#include <Snap.h>

double GenPowerLawPdf(double alpha, double xMin)
{
	return xMin * pow(1 - (double)rand()/RAND_MAX, -1.0/(alpha-1.0));
}

double GenPowerLawCcdf(double x, double alpha, double xMin)
{
	return pow(x / xMin, 1 - alpha);
}

double MaximumLiklihood(TVec<TFlt> d, double xMin)
{
	double sum = 0.0;

	for (TVec<TFlt>::TIter di = d.BegI(); di < d.EndI(); di++)
		sum += log(*di/xMin);

	return 1.0 + (double)d.Len() * pow(sum, -1.0);
}

void Q2()
{
	////////////////////////
	double multiplier = 2.0;
	double C = 1.0/3.0;
	int numSamples = 100000;
	////////////////////////

	srand((unsigned int)time(NULL));

	TVec<TFlt> pdf;
	TVec<TFltPr> normalizedPdfHistogram, ccdf;
	double alpha = 2.0;
	double xMin = 1.0;

	// create PDF
	for (int i = 0; i < numSamples; i++)
		pdf.Add(GenPowerLawPdf(alpha, xMin));


	// sort PDF
	pdf.QSort(0, pdf.Len()-1, true);


	// create CCDF
	int n = 0;
	for (TVec<TFlt>::TIter i = pdf.BegI(); i < pdf.EndI(); i++)
		ccdf.Add(TFltPr(*i, (TFlt)(numSamples-(++n))/(TFlt)numSamples));


	// create Histogram of PDF
	int iterationNum = 1;
	double x1 = xMin;
	double x2 = xMin + multiplier * C;

	TVec<TFlt>::TIter iter = pdf.BegI();

	while (iter != pdf.EndI())
	{
		if (*iter <= x2)
		{
			int numValuesInRange = 0;

			// find the number of values between x1 and x2
			do
			{
				++numValuesInRange;
				++iter;
			} while (*iter <= x2 && iter != pdf.EndI());

			normalizedPdfHistogram.Add(TFltPr((x1 + x2)/2.0, (TFlt)numValuesInRange/(TFlt)numSamples));
		}

		x1 = x2;
		x2 += pow(multiplier, ++iterationNum) * C;
	}

	//plot Histogram of PDF
	TGnuPlot gnuPlot1("Q2b");
	gnuPlot1.AddPlot(normalizedPdfHistogram, gpwBoxes, "Normalized PDF Histogram");
	gnuPlot1.SetXYLabel("x", "P(X = x)");
	gnuPlot1.SetScale(gpsLog2XY);
	gnuPlot1.SavePng();

	//plot CCDF
	TGnuPlot gnuPlot2("Q2_CCDF");
	gnuPlot2.AddPlot(ccdf, gpwLinesPoints, "CCDF");
	gnuPlot2.SetXYLabel("x", "P(X > x)");
	gnuPlot2.SetScale(gpsLog2XY);
	gnuPlot2.SavePng();


	TFOut pdfHistogramFile("Q2_Normalized_Pdf_Histogram.xml");
	normalizedPdfHistogram.SaveXml(pdfHistogramFile, "Q2_Normalized_Pdf_Histogram");

	TFOut ccdfFile("Q2_Ccdf.xml");
	ccdf.SaveXml(ccdfFile, "Q2_Ccdf");


	std::cout << "Maximum Liklihood Alpha Estimation = " << MaximumLiklihood(pdf, xMin) << std::endl;
}

void WriteToFile(TVec<TInt> vec, TStr filename)
{
	TFOut fout(filename);

	for (TVec<TInt>::TIter i = vec.BegI(); i < vec.EndI(); i++)
	{
		fout.PutInt(*i);
		fout.PutLn();
	}

	fout.Flush();
}

void Q3()
{
	WordDistribution distDonQuijote("donquijote.txt");
	WordDistribution distMobyDick("mobydick.txt");

	TGnuPlot gnuPlot("Q3a");

	gnuPlot.AddPlot(distDonQuijote.GetWordFrequency(), gpwLinesPoints, "Don Quijote");
	gnuPlot.AddPlot(distMobyDick.GetWordFrequency(), gpwLinesPoints, "Moby Dick");

	gnuPlot.SetScale(gpsLog10XY);
	gnuPlot.SetXYLabel("Word Frquency", "Distinct Words");
	gnuPlot.SavePng();

	WriteToFile(distDonQuijote.GetDistinctWordFrequency(), "DonQuijoteDistinctWordFrequency.txt");
	WriteToFile(distMobyDick.GetDistinctWordFrequency(), "MobyDickDistinctWordFrequency.txt");
}

TVec<TFltPr> Q4a_AttackVsDiameter(NetworkRobustnessModel & network, TInt x, TFlt y)
{
	// attack in batches of x
	// until y (a percentage) of nodes have been deleted

	double initNodes = (double)network.GetNodes();
	TFlt percentRemoved;
	TVec<TFltPr> plot;

	do
	{
		network.Attack(x);
		percentRemoved = 1.0 - ((double)network.GetNodes() / initNodes);
		plot.Add( TFltPr(percentRemoved, (TFlt)network.GetDiameter(20)) );
	} while (percentRemoved < y);

	return plot;
}

TVec<TFltPr> Q4a_FailureVsDiameter(NetworkRobustnessModel & network, TInt x, TFlt y)
{
	// failures occur in batches of x
	// until y% of nodes have been deleted

	double initNodes = (double)network.GetNodes();
	TFlt percentRemoved;
	TVec<TFltPr> plot;

	do
	{
		network.Failure(x);
		percentRemoved = 1.0 - ((double)network.GetNodes() / initNodes);
		plot.Add( TFltPr(percentRemoved, (TFlt)network.GetDiameter(20)) );
	} while (percentRemoved < y);

	return plot;
}

void Q4aScenario(TInt xDenominator, TFlt y, TStr filename, TStr title)
{
	NetworkRobustnessModel randomNetwork1(TSnap::GenRndGnm<PUNGraph>(10670, 22002, false));
	NetworkRobustnessModel randomNetwork2(TSnap::GenRndGnm<PUNGraph>(10670, 22002, false));
	NetworkRobustnessModel autonomousNetwork1(TSnap::LoadEdgeList<PUNGraph>("oregon1_010331.txt", 0, 1));
	NetworkRobustnessModel autonomousNetwork2(TSnap::LoadEdgeList<PUNGraph>("oregon1_010331.txt", 0, 1));
	PreferentialAttachmentModel prefAttachNetwork1(TSnap::GenFull<PUNGraph>(40), 10670-40, 2);
	PreferentialAttachmentModel prefAttachNetwork2(TSnap::GenFull<PUNGraph>(40), 10670-40, 2);

	TGnuPlot gnuPlot(filename);

	gnuPlot.AddPlot(Q4a_AttackVsDiameter(randomNetwork1, randomNetwork1.GetNodes() / xDenominator, y), gpwLinesPoints, "Random Network Attack");
	gnuPlot.AddPlot(Q4a_AttackVsDiameter(autonomousNetwork1, autonomousNetwork1.GetNodes() / xDenominator, y), gpwLinesPoints, "Autonomous Network Attack");
	gnuPlot.AddPlot(Q4a_AttackVsDiameter(prefAttachNetwork1, prefAttachNetwork1.GetNodes() / xDenominator, y), gpwLinesPoints, "Preferential Attachment Network Attack");

	gnuPlot.AddPlot(Q4a_FailureVsDiameter(randomNetwork2, randomNetwork2.GetNodes() / xDenominator, y), gpwLinesPoints, "Random Network Failure");
	gnuPlot.AddPlot(Q4a_FailureVsDiameter(autonomousNetwork2, autonomousNetwork2.GetNodes() / xDenominator, y), gpwLinesPoints, "Autonomous Network Failure");
	gnuPlot.AddPlot(Q4a_FailureVsDiameter(prefAttachNetwork2, prefAttachNetwork2.GetNodes() / xDenominator, y), gpwLinesPoints, "Preferential Attachment Network Failure");

	gnuPlot.SetTitle(title);
	gnuPlot.SetXYLabel("% of Nodes Removed", "Network Diameter");
	gnuPlot.SavePng();
}

void Q4a()
{
	Q4aScenario(100, 0.50, "Q4a1", "4(a): X = |N|/100 and Y = 50");
	Q4aScenario(1000, 0.02, "Q4a2", "4(a): X = |N|/1000 and Y = 2");
}

TVec<TFltPr> Q4b_AttackVsMxScc(NetworkRobustnessModel & network, TInt x, TFlt y)
{
	// attack in batches of x
	// until y% of nodes have been deleted

	double initNodes = (double)network.GetNodes();
	TFlt percentRemoved;
	TVec<TFltPr> plot;

	do
	{
		network.Attack(x);
		percentRemoved = 1.0 - ((double)network.GetNodes() / initNodes);
		plot.Add( TFltPr(percentRemoved, (TFlt)network.GetNodesInMxScc()/(TFlt)network.GetNodes()) );
	} while (percentRemoved < y);

	return plot;
}

TVec<TFltPr> Q4b_FailureVsMxScc(NetworkRobustnessModel & network, TInt x, TFlt y)
{
	// failures occur in batches of x
	// until y% of nodes have been deleted

	double initNodes = (double)network.GetNodes();
	TFlt percentRemoved;
	TVec<TFltPr> plot;

	do
	{
		network.Failure(x);
		percentRemoved = 1.0 - ((double)network.GetNodes() / initNodes);
		plot.Add( TFltPr(percentRemoved, (TFlt)network.GetNodesInMxScc()/(TFlt)network.GetNodes()) );
	} while (percentRemoved < y);

	return plot;
}

void Q4b()
{
	NetworkRobustnessModel randomNetwork1(TSnap::GenRndGnm<PUNGraph>(10670, 22002, false));
	NetworkRobustnessModel randomNetwork2(TSnap::GenRndGnm<PUNGraph>(10670, 22002, false));
	NetworkRobustnessModel autonomousNetwork1(TSnap::LoadEdgeList<PUNGraph>("oregon1_010331.txt", 0, 1));
	NetworkRobustnessModel autonomousNetwork2(TSnap::LoadEdgeList<PUNGraph>("oregon1_010331.txt", 0, 1));
	PreferentialAttachmentModel prefAttachNetwork1(TSnap::GenFull<PUNGraph>(40), 10670-40, 2);
	PreferentialAttachmentModel prefAttachNetwork2(TSnap::GenFull<PUNGraph>(40), 10670-40, 2);

	TGnuPlot gnuPlot("Q4b");

	gnuPlot.AddPlot(Q4b_AttackVsMxScc(randomNetwork1, randomNetwork1.GetNodes() / 100, 0.50), gpwLinesPoints, "Random Network Attack");
	gnuPlot.AddPlot(Q4b_AttackVsMxScc(autonomousNetwork1, autonomousNetwork1.GetNodes() / 100, 0.50), gpwLinesPoints, "Autonomous Network Attack");
	gnuPlot.AddPlot(Q4b_AttackVsMxScc(prefAttachNetwork1, prefAttachNetwork1.GetNodes() / 100, 0.50), gpwLinesPoints, "Preferential Attachment Network Attack");

	gnuPlot.AddPlot(Q4b_FailureVsMxScc(randomNetwork2, randomNetwork2.GetNodes() / 100, 0.50), gpwLinesPoints, "Random Network Failure");
	gnuPlot.AddPlot(Q4b_FailureVsMxScc(autonomousNetwork2, autonomousNetwork2.GetNodes() / 100, 0.50), gpwLinesPoints, "Autonomous Network Failure");
	gnuPlot.AddPlot(Q4b_FailureVsMxScc(prefAttachNetwork2, prefAttachNetwork2.GetNodes() / 100, 0.50), gpwLinesPoints, "Preferential Attachment Network Failure");

	gnuPlot.SetTitle("4(b): X = |N|/100 and Y = 50");
	gnuPlot.SetXYLabel("% of Nodes Removed", "% of Nodes in Largest Connected Component");
	gnuPlot.SavePng();
}

void Q4()
{
	Q4a();
	Q4b();
}

void PrintSystemTime()
{
	SYSTEMTIME * st = new SYSTEMTIME;
	GetSystemTime(st);
	std::cout << st->wHour-5 << ":" << st->wMinute << ":" << st->wSecond << "." << st->wMilliseconds << std::endl;
	delete st;
}

int _tmain(int argc, _TCHAR* argv[])
{
	PrintSystemTime();

	try
	{
		Q2();
		Q3();
		Q4();
	}
	catch(std::exception& e)
    {
		PrintSystemTime();
        std::cerr << "ERROR: " << e.what() << "\n";
		system("pause");
        return 1;
    }

	PrintSystemTime();
	system("pause");
	return 0;
}

