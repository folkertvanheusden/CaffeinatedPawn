import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.MoveBackup;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import java.lang.Math.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

class CaffeinatedPawn {
	AtomicBoolean to = new AtomicBoolean(false);
	Tt tt = new Tt();

	void timeoutThread(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch(InterruptedException ex) {
			System.out.println("# sleep interrupted");
		}

		to.set(true);
	}

	short evalPieceType(PieceType p) {
		switch(p) {
			case KING:
				return 10000;
			case QUEEN:
				return 900;
			case ROOK:
				return 500;
			case BISHOP:
			case KNIGHT:
				return 300;
			case PAWN:
				return 100;
		}

		return -1;
	}

	short evaluate(Board b) {
		short score = 0;

		List<Square> whiteQueenSquares = b.getPieceLocation(Piece.WHITE_QUEEN);
		score += whiteQueenSquares.size() * 900;

		List<Square> blackQueenSquares = b.getPieceLocation(Piece.BLACK_QUEEN);
		score -= blackQueenSquares.size() * 900;

		List<Square> whiteRookSquares = b.getPieceLocation(Piece.WHITE_ROOK);
		score += whiteRookSquares.size() * 500;

		List<Square> blackRookSquares = b.getPieceLocation(Piece.BLACK_ROOK);
		score -= blackRookSquares.size() * 500;

		List<Square> whiteBishopSquares = b.getPieceLocation(Piece.WHITE_BISHOP);
		score += whiteBishopSquares.size() * 300;

		List<Square> blackBishopSquares = b.getPieceLocation(Piece.BLACK_BISHOP);
		score -= blackBishopSquares.size() * 300;

		List<Square> whiteKnightSquares = b.getPieceLocation(Piece.WHITE_KNIGHT);
		score += whiteKnightSquares.size() * 300;

		List<Square> blackKnightSquares = b.getPieceLocation(Piece.BLACK_KNIGHT);
		score -= blackKnightSquares.size() * 300;

		List<Square> whitePawnSquares = b.getPieceLocation(Piece.WHITE_PAWN);
		score += whitePawnSquares.size() * 100;

		List<Square> blackPawnSquares = b.getPieceLocation(Piece.BLACK_PAWN);
		score -= blackPawnSquares.size() * 100;

		if (b.getSideToMove() == Side.WHITE) {
			score += b.pseudoLegalMoves().size();

			b.doNullMove();
			score -= b.pseudoLegalMoves().size();
			b.undoMove();
		}
		else {
			score -= b.pseudoLegalMoves().size();

			b.doNullMove();
			score += b.pseudoLegalMoves().size();
			b.undoMove();
		}

		score += PSQ.psq(b);

		if (b.getSideToMove() == Side.BLACK)
			score = (short)-score;

		return score;
	}

	boolean isValidMove(Board b, Move m) {
		Piece fromPiece = b.getPiece(m.getFrom());
		if (fromPiece == null)
			return false;

		return b.isMoveLegal(m, true);
	}

	boolean underAttackByOpponent(Board b, Square to) {
		List<Square> squares = new ArrayList<Square>();
		squares.add(to);

		return b.isSquareAttackedBy(squares, b.getSideToMove().flip());
	}

	Result quiescenceSearch(Board b, short alpha, short beta, short qsDepth, short maxDepth, Stats s) {
		if (to.get())
			return null;

		s.nodeCount++;

		Result r = new Result();

		if (b.isMated()) {
			r.score = -9999;
			return r;
		}

		if (b.isDraw() || b.isInsufficientMaterial() || b.isStaleMate()) {
			r.score = 0;
			return r;
		}

		r.score = -32767;

		boolean inCheck = b.isKingAttacked();

		if (!inCheck) {
			r.score = evaluate(b);

			if (r.score > alpha && r.score >= beta)
				return r;

			short bigDelta = 975;
			LinkedList<MoveBackup> moveBackups = b.getBackup();

			if (moveBackups.get(moveBackups.size() - 1).getMove().getPromotion().getPieceType() != PieceType.NONE)
				bigDelta += 975;

			if (r.score < alpha - bigDelta) {
				r.score = alpha;
				return r;
			}

			if (alpha < r.score)
				alpha = r.score;
		}

		List<Move> moves = inCheck ? orderMoves(b, b.pseudoLegalMoves(), null) : b.pseudoLegalCaptures();

		int n_moves_tried = 0;

		for(Move move : moves) {
			if (b.isMoveLegal(move, false) == false)
				continue;

			if (!inCheck) {
				PieceType attacker = b.getPiece(move.getFrom()).getPieceType();
				Piece victimPiece = null;
				boolean isEnPassant = false;

				if (attacker == PieceType.PAWN && !move.getFrom().getFile().equals(move.getTo().getFile()))  // en-passant
					isEnPassant = true;
				else
					victimPiece = b.getPiece(move.getTo());

				if (victimPiece != null || isEnPassant) {
					PieceType victim = isEnPassant ? PieceType.PAWN : victimPiece.getPieceType();

					short attackerValue = evalPieceType(attacker);
					short victimValue = evalPieceType(victim);

					if (attackerValue > victimValue && underAttackByOpponent(b, move.getTo()))
						continue;
				}
			}

			b.doMove(move);
			n_moves_tried++;

			Result child = quiescenceSearch(b, (short)-beta, (short)-alpha, (short)(qsDepth + 1), maxDepth, s);
			if (child == null) {
				b.undoMove();

				return null;
			}

			short score = (short)-child.score;

			b.undoMove();

			if (score > r.score) {
				r.score = score;
				r.m = move;

				if (score > alpha) {
					alpha = score;

					if (score >= beta)
						break;
				}
			}
		}

		if (n_moves_tried == 0) {
			if (inCheck)
				r.score = (short)(-10000 + maxDepth + qsDepth);
			else if (r.score == -32767)
				r.score = evaluate(b);
		}

		return r;
	}

	List<Move> orderMoves(Board b, final List<Move> in, Move ttMove) {
		ArrayList<Move> out1 = new ArrayList<Move>();
		ArrayList<Move> out2 = new ArrayList<Move>();

		for(Move m : in) {
			if (m == ttMove)
				out1.add(0, m);
			else if (m.getPromotion().getPieceType() != PieceType.NONE || b.isAttackedBy(m))
				out1.add(m);
			else
				out2.add(m);
		}

		out1.addAll(out2);

		return out1;
	}

	Result search(Board b, short depth, short alpha, short beta, short maxDepth, Stats s, boolean isNullMove) {
		if (to.get())
			return null;

		s.nodeCount++;

		Result r = new Result();

		if (b.isMated()) {
			r.score = -9999;
			return r;
		}

		if (depth == 0)
			return quiescenceSearch(b, alpha, beta, (short)0, maxDepth, s);

		if (b.isDraw() || b.isInsufficientMaterial() || b.isStaleMate()) {
			r.score = 0;
			return r;
		}

		Move ttMove = null;  // used later on for sorting
		TtElement te = tt.lookup(b.hashCode());
		if (te != null && isValidMove(b, te.m)) {
			ttMove = te.m;

			if (te.depth >= depth) {
				boolean use = false;

				short csd = (short)(maxDepth - depth);
				short score = te.score;
				short workScore = (short)(Math.abs(score) > 9800 ? (score < 0 ? score + csd : score - csd) : score);

				if (te.f == ttFlag.EXACT)
					use = true;
				else if (te.f == ttFlag.LOWERBOUND && workScore >= beta)
					use = true;
				else if (te.f == ttFlag.UPPERBOUND && workScore <= alpha)
					use = true;

				if (use) {
					r.score = score;
					r.m     = te.m;

					return r;
				}
			}
		}

		int startAlpha = alpha;

		r.score = -32767;

		boolean isRootPosition = maxDepth == depth;
		boolean inCheck = b.isKingAttacked();
		int nmReduceDepth = depth > 6 ? 4 : 3;
		if (depth >= nmReduceDepth && !inCheck && !isRootPosition && !isNullMove) {
			b.doNullMove();

			Result nm = search(b, (short)(depth - nmReduceDepth), (short)-beta, (short)(-beta + 1), maxDepth, s, true);

			b.undoMove();

			if (nm == null)
				return null;

			int score = -nm.score;

			if (score >= beta) {
				Result verification = search(b, (short)(depth - nmReduceDepth), (short)(beta - 1), (short)beta, maxDepth, s, false);

				if (verification.score >= beta) {
					r.score = beta;
					return r;
				}
			}
		}

		List<Move> moves = b.pseudoLegalMoves();

		moves = orderMoves(b, moves, ttMove);

		int n_moves_tried = 0;

		for(Move move : moves) {
			if (b.isMoveLegal(move, false) == false)
				continue;

			b.doMove(move);
			n_moves_tried++;

			Result child = search(b, (short)(depth - 1), (short)-beta, (short)-alpha, maxDepth, s, false);
			if (child == null) {
				b.undoMove();

				return null;
			}

			short score = (short)-child.score;

			b.undoMove();

			if (score > r.score) {
				r.score = score;
				r.m = move;

				if (score > alpha) {
					alpha = score;

					if (score >= beta)
						break;
				}
			}
		}

		if (n_moves_tried == 0)
			r.score = 0;

                ttFlag flag = ttFlag.EXACT;

                if (r.score <= startAlpha)
                        flag = ttFlag.UPPERBOUND;
                else if (r.score >= beta)
                        flag = ttFlag.LOWERBOUND;

                tt.store(b.hashCode(), flag, depth, r.score, r.score > startAlpha || ttMove == null ? r.m : ttMove);

		return r;
	}

	Result doSearch(int maxThinkTime, Board b) {
		long start = new Date().getTime();

		Thread toThread = new Thread(() -> { if (maxThinkTime >= 0) { timeoutThread(maxThinkTime); } });
		toThread.start();

		int cores = Runtime.getRuntime().availableProcessors();

		List<Thread> threads = new ArrayList<Thread>();
		/*
		for(int i=0; i<cores - 1; i++) {
			Thread cur = new Thread(() -> { 
				Board bLocal = b.clone();

				short depth = 2;
				while(!bLocal.isMated() && to.get() == false) {
					search(bLocal, depth, (short)-32767, (short)32767, depth, false);

					depth++;
				}
		       
			});
			cur.start();

			threads.add(cur);
		}*/

		Stats s = new Stats();
		Result chosen = null;
		short alpha = -32768, beta = 32767;
		short add_alpha = 75, add_beta = 75;
		short depth = 1;
		while(!b.isMated() && to.get() == false) {
			Result r = search(b, depth, alpha, beta, depth, s, false);
			if (r == null || r.m == null)
				break;

			if (r.score <= alpha) {
				beta = (short)((alpha + beta) / 2);
				alpha = (short)(r.score - add_alpha);
				if (alpha < -10000)
					alpha = -10000;
				add_alpha += add_alpha / 15 + 1;
			}
			else if (r.score >= beta) {
				alpha = (short)((alpha + beta) / 2);
				beta = (short)(r.score + add_beta);
				if (beta > 10000)
					beta = 10000;
				add_beta += add_beta / 15 + 1;
			}
			else {
				alpha = (short)(r.score - add_alpha);
				if (alpha < -10000)
					alpha = -10000;

				beta = (short)(r.score + add_beta);
				if (beta > 10000)
					beta = 10000;

				long now = new Date().getTime();

				long timeDiff = now - start;

				int nps = (int)(s.nodeCount * 1000 / timeDiff);

				System.out.printf("info depth %d score cp %d time %d nodes %d nps %d pv %s\n", depth, r.score, timeDiff, s.nodeCount, nps, r.m);

				chosen = r;

				if (timeDiff > maxThinkTime / 2)
					break;

				depth++;
			}
		}

		try {
			toThread.interrupt();
			toThread.join();
		}
		catch(InterruptedException ie) {
		}

		for(Thread t : threads) {
			try {
				t.interrupt();
				t.join();
			}
			catch(InterruptedException ie) {
			}
		}

		return chosen;
	}

	public static void main(String [] args) {
		CaffeinatedPawn cp = new CaffeinatedPawn();

		Board b = new Board();

		Result r = cp.doSearch(10000, b);

		if (r != null && r.m != null) {
			b.doMove(r.m);

			System.out.print(r.score);
			System.out.print(' ');
			System.out.println(r.m);
		}
	}
}
