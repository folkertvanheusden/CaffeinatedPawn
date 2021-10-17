package com.vanheusden.CaffeinatedPawn;

class TtStats
{
	long lookups;
	long lookupHits;
	long stores;
	long storesSkipDepth;
	long storesSkipFlag;
	long storesSkipBest;
	long storeMinDepth;
	long storeAge;

	TtStats() {
		lookups = 0;
		lookupHits = 0;
		stores = 0;
		storesSkipDepth = 0;
		storesSkipFlag = 0;
		storesSkipBest = 0;
		storeMinDepth = 0;
		storeAge = 0;
	}
}
