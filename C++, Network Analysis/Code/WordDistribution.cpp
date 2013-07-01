// WordDistribution.cpp

#include "stdafx.h"
#include "WordDistribution.h"

#include <iostream>

WordDistribution::WordDistribution(TStr filename)
{
	TFIn fin(filename);
	TStr word;
	totalWords = 0;

	while (fin.GetNextLn(word))
	{
		++totalWords;

		if (wordFrequencies.IsKey(word))
			wordFrequencies.GetDat(word)++;
		else
			wordFrequencies.AddDat(word, 1);
	}

	wordFrequencies.SortByDat(false);
}

TVec<TIntPr> WordDistribution::GetWordFrequency()
{
	THash<TInt, TInt> wordFrequencyDistribution;

	for (THash<TStr, TInt>::TIter i = wordFrequencies.BegI(); i < wordFrequencies.EndI(); i++)
	{
		int frequency = i.GetDat();

		if (wordFrequencyDistribution.IsKey(frequency))
			wordFrequencyDistribution.GetDat(frequency)++;
		else
			wordFrequencyDistribution.AddDat(frequency, 1);

	}

	TVec<TIntPr> wordFrequency;

	for (THash<TInt, TInt>::TIter i = wordFrequencyDistribution.BegI(); i < wordFrequencyDistribution.EndI(); i++)
		wordFrequency.Add(TIntPr(i.GetKey(), i.GetDat()));

	wordFrequency.QSort(0, wordFrequency.Len()-1, true);

	return wordFrequency;
}

TVec<TInt> WordDistribution::GetDistinctWordFrequency()
{
	THash<TInt, TInt> wordFrequencyDistribution;

	for (THash<TStr, TInt>::TIter i = wordFrequencies.BegI(); i < wordFrequencies.EndI(); i++)
	{
		int frequency = i.GetDat();

		if (wordFrequencyDistribution.IsKey(frequency))
			wordFrequencyDistribution.GetDat(frequency)++;
		else
			wordFrequencyDistribution.AddDat(frequency, 1);
	}

	TVec<TInt> wordFrequency;

	for (THash<TInt, TInt>::TIter i = wordFrequencyDistribution.BegI(); i < wordFrequencyDistribution.EndI(); i++)
		wordFrequency.AddUnique(i.GetKey());

	wordFrequency.QSort(0, wordFrequency.Len()-1, true);

	return wordFrequency;
}