package com.vanheusden.CaffeinatedPawn;

import com.github.bhlangonijr.chesslib.move.Move;

enum ttFlag { EXACT, LOWERBOUND, UPPERBOUND };

class TtElement
{
	long hash;
	short score;
	ttFlag f;
	short age;
	short depth;
	Move m;  // TODO replace by int to reduce pressure on garbage collector
}
