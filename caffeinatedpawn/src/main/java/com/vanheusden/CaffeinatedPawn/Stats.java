package com.vanheusden.CaffeinatedPawn;

class Stats
{
	short deepestDepth;
	long nodeCount, qsNodeCount;

	long bcoCount, bcoIndex;
	long qsBcoCount, qsBcoIndex;

	long nmCount, nmVerifyCount;

	long lmrCount, lmrFullCount;

	long ttInvoked, ttHit, ttHitGood, ttUse;

	long nullMoveNodeCount;

	long iidCount, iidCountHit;

	long itDeepAlpha, itDeepBeta, itDeepOk;

	Stats() {
		deepestDepth = 0;
		nodeCount = 0;

		bcoCount = bcoIndex = 0;
		qsBcoCount = qsBcoIndex = 0;

		nmCount = nmVerifyCount = 0;

		lmrCount = lmrFullCount = 0;

		ttInvoked = ttHit = ttHitGood = ttUse = 0;

		nullMoveNodeCount = 0;

		iidCount = iidCountHit = 0;

		itDeepAlpha = itDeepBeta = itDeepOk = 0;
	}
}
