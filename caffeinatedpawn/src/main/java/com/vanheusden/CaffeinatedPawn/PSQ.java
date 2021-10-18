package com.vanheusden.CaffeinatedPawn;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class PSQ
{
	// taken from dorpsgek
	static int PawnPSTMG[] = {
	//A1                                    H1
	      0,   0,   0,   0,   0,   0,   0,   0,
	     -1,  -7, -11, -35, -13,   5,   3,  -5,
	      1,  1,   -6, -19,  -6,  -7,  -4,  10,
	      1,  14,   8,   4,   5,   4,  10,   7,
	      9,  30,  23,  31,  31,  23,  17,  11,
	     21,  54,  72,  56,  77,  95,  71,  11,
	    118, 121, 173, 168, 107,  82, -16,  22,
	      0,   0,   0,   0,   0,   0,   0,   0
	//A8                                    H8
	};

	static int PawnPSTEG[] = {
	      0,   0,   0,   0,   0,   0,   0,   0,
	    -17, -17, -17, -17, -17, -17, -17, -17,
	    -11, -11, -11, -11, -11, -11, -11, -11,
	     -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,
	     16,  16,  16,  16,  16,  16,  16,  16,
	     55,  55,  55,  55,  55,  55,  55,  55,
	     82,  82,  82,  82,  82,  82,  82,  82,
	      0,   0,   0,   0,   0,   0,   0,   0
	};

	static int KnightPSTMG[] = {
	    -99, -30, -66, -64, -29, -19, -61, -81,
	    -56, -31, -28,  -1,  -7, -20, -42, -11,
	    -38, -16,   0,  14,   8,   3,   3, -42,
	    -14,   0,   2,   3,  19,  12,  33,  -7,
	    -14,  -4,  25,  33,  10,  33,  14,  43,
	    -22,  18,  60,  64, 124, 143,  55,   6,
	    -34,  24,  54,  74,  60, 122,   2,  29,
	    -60,   0,   0,   0,   0,   0,   0,   0
	};

	static int KnightPSTEG[] = {
	    -99, -99, -94, -88, -88, -94, -99, -99,
	    -81, -62, -49, -43, -43, -49, -62, -81,
	    -46, -27, -15,  -9,  -9, -15, -27, -46,
	    -22,  -3,  10,  16,  16,  10,  -3, -22,
	     -7,  12,  25,  31,  31,  25,  12,  -7,
	     -2,  17,  30,  36,  36,  30,  17,  -2,
	     -7,  12,  25,  31,  31,  25,  12,  -7,
	    -21,  -3,  10,  16,  16,  10,  -3, -21
	};

	static int BishopPSTMG[] = {
	     -7,  12,  -8, -37, -31,  -8, -45, -67,
	     15,   5,  13, -10,   1,   2,   0,  15,
	      5,  12,  14,  13,  10,  -1,   3,   4,
	      1,   5,  23,  32,  21,   8,  17,   4,
	     -1,  16,  29,  27,  37,  27,  17,   4,
	      7,  27,  20,  56,  91, 108,  53,  44,
	    -24, -23,  30,  58,  65,  61,  69,  11,
	      0,   0,   0,   0,   0,   0,   0,   0
	};

	static int BishopPSTEG[] = {
	    -27, -21, -17, -15, -15, -17, -21, -27,
	    -10,  -4,   0,   2,   2,   0,  -4, -10,
	      2,   8,  12,  14,  14,  12,   8,   2,
	     11,  17,  21,  23,  23,  21,  17,  11,
	     14,  20,  24,  26,  26,  24,  20,  14,
	     13,  19,  23,  25,  25,  23,  19,  13,
	      8,  14,  18,  20,  20,  18,  14,   8,
	     -2,   4,   8,  10,  10,   8,   4,  -2
	};

	static int RookPSTMG[] = {
	     -2,  -1,   3,   1,   2,   1,   4,  -8,
	    -26,  -6,   2,  -2,   2, -10,  -1, -29,
	    -16,   0,   3,  -3,   8,  -1,  12,   3,
	     -9,  -5,   8,  14,  18, -17,  13, -13,
	     19,  33,  46,  57,  53,  39,  53,  16,
	     24,  83,  54,  75, 134, 144,  85,  75,
	     46,  33,  64,  62,  91,  89,  70, 104,
	     84,   0,   0,  37, 124,   0,   0, 153
	};

	static int RookPSTEG[] = {
	    -32, -31, -30, -29, -29, -30, -31, -32,
	    -27, -25, -24, -24, -24, -24, -25, -27,
	    -15, -13, -12, -12, -12, -12, -13, -15,
	      1,   2,   3,   4,   4,   3,   2,   1,
	     15,  17,  18,  18,  18,  18,  17,  15,
	     25,  27,  28,  28,  28,  28,  27,  25,
	     27,  28,  29,  30,  30,  29,  28,  27,
	     16,  17,  18,  19,  19,  18,  17,  16
	};

	static int QueenPSTMG[] = {
	      1, -10, -11,   3, -15, -51, -83, -13,
	     -7,   3,   2,   5,  -1, -10,  -7,  -2,
	    -11,   0,  12,   2,   8,  11,   7,  -6,
	     -9,   5,   7,   9,  18,  17,  26,   4,
	     -6,   0,  15,  25,  32,   9,  26,  12,
	    -16,  10,  13,  25,  37,  30,  15,  26,
	      1,  11,  35,   0,  16,  55,  39,  57,
	    -13,   6, -42,   0,  29,   0,   0, 102
	};

	static int QueenPSTEG[] = {
	    -61, -55, -52, -50, -50, -52, -55, -61,
	    -31, -26, -22, -21, -21, -22, -26, -31,
	     -8,  -3,   1,   3,   3,   1,  -3,  -8,
	      9,  14,  17,  19,  19,  17,  14,   9,
	     19,  24,  28,  30,  30,  28,  24,  19,
	     23,  28,  32,  34,  34,  32,  28,  23,
	     21,  26,  30,  31,  31,  30,  26,  21,
	     12,  17,  21,  23,  23,  21,  17,  12
	};

	static int KingPSTMG[] = {
	      0,   0,   0,  -9,   0,  -9,  25,   0,
	     -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,
	     -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,
	     -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,
	     -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,
	     -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,
	     -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,
	     -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9
	};

	static int KingPSTEG[] = {
	    -34, -30, -28, -27, -27, -28, -30, -34,
	    -17, -13, -11, -10, -10, -11, -13, -17,
	     -2,   2,   4,   5,   5,   4,   2,  -2,
	     11,  15,  17,  18,  18,  17,  15,  11,
	     22,  26,  28,  29,  29,  28,  26,  22,
	     31,  34,  37,  38,  38,  37,  34,  31,
	     38,  41,  44,  45,  45,  44,  41,  38,
	     42,  46,  48,  50,  50,  48,  46,  42
	};

	static Map<Piece, int []> map = new TreeMap<Piece, int []>();

	static {
		map.put(Piece.WHITE_KING, KingPSTMG);
		map.put(Piece.BLACK_KING, KingPSTMG);

		map.put(Piece.WHITE_QUEEN, QueenPSTMG);
		map.put(Piece.BLACK_QUEEN, QueenPSTMG);

		map.put(Piece.WHITE_ROOK, RookPSTMG);
		map.put(Piece.BLACK_ROOK, RookPSTMG);

		map.put(Piece.WHITE_BISHOP, BishopPSTMG);
		map.put(Piece.BLACK_BISHOP, BishopPSTMG);

		map.put(Piece.WHITE_KNIGHT, KnightPSTMG);
		map.put(Piece.BLACK_KNIGHT, KnightPSTMG);

		map.put(Piece.WHITE_PAWN, PawnPSTMG);
		map.put(Piece.BLACK_PAWN, PawnPSTMG);
	}

	static int processPsq(List<Square> squares, int values[], int xor) {
		int score = 0;

		for(Square sq : squares)
			score += values[sq.ordinal() ^ xor];

		return score;
	}

	static int psq(Square sq, Piece p) {
		return map.get(p)[sq.ordinal() ^ (p.getPieceSide().equals(Side.BLACK) ? 56 : 0)];
	}

	static int psq(Board b) {
		int score = 0;

                List<Square> whiteKingSquares = b.getPieceLocation(Piece.WHITE_KING);
		score += processPsq(whiteKingSquares, KingPSTMG, 0);

                List<Square> blackKingSquares = b.getPieceLocation(Piece.BLACK_KING);
		score -= processPsq(blackKingSquares, KingPSTMG, 56);

                List<Square> whiteQueenSquares = b.getPieceLocation(Piece.WHITE_QUEEN);
		score += processPsq(whiteQueenSquares, QueenPSTMG, 0);

                List<Square> blackQueenSquares = b.getPieceLocation(Piece.BLACK_QUEEN);
		score -= processPsq(blackQueenSquares, QueenPSTMG, 56);

                List<Square> whiteRookSquares = b.getPieceLocation(Piece.WHITE_ROOK);
		score += processPsq(whiteRookSquares, RookPSTMG, 0);

                List<Square> blackRookSquares = b.getPieceLocation(Piece.BLACK_ROOK);
		score -= processPsq(blackRookSquares, RookPSTMG, 56);

                List<Square> whiteBishopSquares = b.getPieceLocation(Piece.WHITE_BISHOP);
		score += processPsq(whiteBishopSquares, BishopPSTMG, 0);

                List<Square> blackBishopSquares = b.getPieceLocation(Piece.BLACK_BISHOP);
		score -= processPsq(blackBishopSquares, BishopPSTMG, 56);

                List<Square> whiteKnightSquares = b.getPieceLocation(Piece.WHITE_KNIGHT);
		score += processPsq(whiteKnightSquares, KnightPSTMG, 0);

                List<Square> blackKnightSquares = b.getPieceLocation(Piece.BLACK_KNIGHT);
		score -= processPsq(blackKnightSquares, KnightPSTMG, 56);

                List<Square> whitePawnSquares = b.getPieceLocation(Piece.WHITE_PAWN);
		score += processPsq(whitePawnSquares, PawnPSTMG, 0);

                List<Square> blackPawnSquares = b.getPieceLocation(Piece.BLACK_PAWN);
		score -= processPsq(blackPawnSquares, PawnPSTMG, 56);

		return score;
	}
}
