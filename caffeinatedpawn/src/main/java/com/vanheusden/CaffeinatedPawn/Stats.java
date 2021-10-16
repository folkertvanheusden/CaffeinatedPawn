package com.vanheusden.CaffeinatedPawn;

class Stats
{
	short deepestDepth;
	long nodeCount, qsNodeCount;

	long bcoCount, bcoIndex;
	long qsBcoCount, qsBcoIndex;

	long nmCount, nmVerifyCount;

	long lmrCount, lmrFullCount;

	Stats() {
		deepestDepth = 0;
		nodeCount = 0;

		bcoCount = bcoIndex = 0;
		qsBcoCount = qsBcoIndex = 0;

		nmCount = nmVerifyCount = 0;

		lmrCount = lmrFullCount = 0;
	}
}
