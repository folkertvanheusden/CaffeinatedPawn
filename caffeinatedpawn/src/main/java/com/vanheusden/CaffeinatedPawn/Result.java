package com.vanheusden.CaffeinatedPawn;

import java.util.List;

import com.github.bhlangonijr.chesslib.move.Move;

class Result
{
	short score;
	List<Move> pv;
	boolean notInTt = false;
}
