// WordDistribution.h

#include "stdafx.h"
#include <Snap.h>

class WordDistribution
{
private:
	THash<TStr, TInt> wordFrequencies;
	int totalWords;

public:
	WordDistribution(TStr filename);
	TVec<TIntPr> GetWordFrequency();
	TVec<TInt> GetDistinctWordFrequency();
};