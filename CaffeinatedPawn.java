import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.MoveBackup;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Math.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

class CaffeinatedPawn {
	AtomicBoolean to = new AtomicBoolean(false);
	Tt tt = new Tt();
	Thread ponderThread = null;

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

		return 0;  // "NONE"
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

		score += PSQ.psq(b);

		if (b.getSideToMove() == Side.BLACK)
			score = (short)-score;

		return score;
	}

	boolean isValidMove(Board b, Move m) {
		Piece fromPiece = b.getPiece(m.getFrom());
		if (Piece.NONE.equals(fromPiece))
			return false;

		return b.isMoveLegal(m, true);
	}

	boolean underAttackByOpponent(Board b, Square to) {
		List<Square> squares = new ArrayList<Square>();
		squares.add(to);

		return b.isSquareAttackedBy(squares, b.getSideToMove().flip());
	}

	boolean isCaptureMove(Board b, Move move) {
		PieceType attacker = b.getPiece(move.getFrom()).getPieceType();

		if (attacker == PieceType.PAWN && !move.getFrom().getFile().equals(move.getTo().getFile()))  // en-passant
			return true;

		if (Piece.NONE.equals(b.getPiece(move.getTo())) == false)
			return true;

		return false;
	}

	Result quiescenceSearch(Board b, short alpha, short beta, short qsDepth, short maxDepth, Stats s) {
		if (to.get())
			return null;

		s.nodeCount++;
		s.qsNodeCount++;

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

			if (Piece.NONE.equals(moveBackups.get(moveBackups.size() - 1).getMove().getPromotion()) == false)
				bigDelta += 975;

			if (r.score < alpha - bigDelta) {
				r.score = alpha;
				return r;
			}

			if (alpha < r.score)
				alpha = r.score;
		}

		List<MyMove> moves = orderMoves(b, inCheck ? b.pseudoLegalMoves() : b.pseudoLegalCaptures(), null);

		int nMovesTried = 0;

		for(MyMove mmove : moves) {
			Move move = mmove.getMove();

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
			nMovesTried++;

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

		if (nMovesTried == 0) {
			if (inCheck)
				r.score = (short)(-10000 + maxDepth + qsDepth);
			else if (r.score == -32767)
				r.score = evaluate(b);
		}

		return r;
	}

	List<MyMove> orderMoves(Board b, final List<Move> in, Move ttMove) {
		ArrayList<MyMove> work = new ArrayList<MyMove>();

		for(Move move : in) {
			int score = 0;

			if (move.equals(ttMove))
				score = 10000;
			else {
				PieceType attacker = b.getPiece(move.getFrom()).getPieceType();
				int victimValue = 0;

				if (attacker == PieceType.PAWN && !move.getFrom().getFile().equals(move.getTo().getFile()))  // en-passant
					victimValue = 100;
				else if (Piece.NONE.equals(b.getPiece(move.getTo())) == false)
					victimValue = evalPieceType(b.getPiece(move.getTo()).getPieceType());

				score += victimValue - evalPieceType(attacker);

				PieceType promoPieceType = move.getPromotion().getPieceType();
				if (promoPieceType != null)
					score += evalPieceType(promoPieceType);
			}

			work.add(new MyMove(move, score));
		}

		Collections.sort(work);

		return work;
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

		boolean isRootPosition = maxDepth == depth;

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

				if (use && (!isRootPosition || te.m != null)) {
					r.score = workScore;
					r.m     = te.m;

					return r;
				}
			}
		}

		int startAlpha = alpha;

		r.score = -32767;

		boolean inCheck = b.isKingAttacked();
		int nmReduceDepth = depth > 6 ? 4 : 3;
		if (depth >= nmReduceDepth && !inCheck && !isRootPosition && !isNullMove) {
			b.doNullMove();

			Result nm = search(b, (short)(depth - nmReduceDepth), (short)-beta, (short)(-beta + 1), maxDepth, s, true);

			b.undoMove();

			if (nm == null)
				return null;

			int nmscore = -nm.score;

			if (nmscore >= beta) {
				Result verification = search(b, (short)(depth - nmReduceDepth), (short)(beta - 1), (short)beta, maxDepth, s, false);

				if (verification == null)
					return null;

				if (verification.score >= beta) {
					r.score = beta;
					return r;
				}
			}
		}

		List<MyMove> moves = orderMoves(b, b.pseudoLegalMoves(), ttMove);

		int lmrStart = !inCheck && depth >= 2 ? 4 : 999;

		int nMovesTried = 0;

		for(MyMove mmove : moves) {
			Move move = mmove.getMove();

			if (b.isMoveLegal(move, false) == false)
				continue;

			b.doMove(move);
			nMovesTried++;

			Result child = null;

			if (inCheck) {
				child = search(b, (short)(depth - 1), (short)-beta, (short)-alpha, maxDepth, s, isNullMove);
			}
			else {
				boolean isLMR = false;

				short newDepth = (short)(depth - 1);
				if (nMovesTried >= lmrStart && Piece.NONE.equals(move.getPromotion()) && isCaptureMove(b, move) == false) {
					isLMR = true;

					if (nMovesTried >= lmrStart + 2)
						newDepth = (short)((depth - 1) * 2 / 3);
					else
						newDepth = (short)(depth - 2);
				}

				boolean checkAfterMove = b.isKingAttacked();

				if (!checkAfterMove)
					child = search(b, (short)newDepth, (short)-beta, (short)-alpha, maxDepth, s, isNullMove);

				if (checkAfterMove || (child != null && isLMR && -child.score > alpha))
					child = search(b, (short)(depth - 1), (short)-beta, (short)-alpha, maxDepth, s, isNullMove);
			}

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

		if (nMovesTried == 0) {
			if (inCheck)
				r.score = (short)(-10000 + maxDepth - depth);
			else
				r.score = 0;
		}

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

		to.set(false);

		Thread toThread = new Thread(() -> { if (maxThinkTime >= 0) { timeoutThread(maxThinkTime); } });
		toThread.start();

		tt.incAge();

		int cores = 1; // TODO Runtime.getRuntime().availableProcessors();

		List<Thread> threads = new ArrayList<Thread>();

		for(int i=0; i<cores - 1; i++) {
			Thread cur = new Thread(() -> { 
				Board bLocal = b.clone();
				Stats s = new Stats();

				short depth = 2;
				while(!bLocal.isMated() && to.get() == false) {
					search(bLocal, depth, (short)-32767, (short)32767, depth, s, false);

					depth++;
				}
		       
			});
			cur.start();

			threads.add(cur);
		}

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
				if (timeDiff == 0)
					timeDiff = 1;

				int nps = (int)(s.nodeCount * 1000 / timeDiff);

				System.out.printf("info depth %d score cp %d time %d nodes %d nps %d pv %s\n", depth, r.score, timeDiff, s.nodeCount, nps, r.m);

				chosen = r;

				if (timeDiff > maxThinkTime / 2)
					break;

				depth++;
			}
		}

		System.out.printf("# QS: %.2f%%\n", s.qsNodeCount * 100.0 / s.nodeCount);

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

	public void startPonder(Board bIn) {
		Board bLocal = bIn.clone();

		to.set(false);

		ponderThread = new Thread(() -> {
			Stats s = new Stats();
			Result chosen = null;
			short alpha = -32768, beta = 32767;
			short add_alpha = 75, add_beta = 75;
			short depth = 1;
			while(!bLocal.isMated() && to.get() == false) {
				Result r = search(bLocal, depth, alpha, beta, depth, s, false);
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

					depth++;
				}
			}
		});

		ponderThread.start();
	}

	public void stopPonder() {
		if (ponderThread != null) {
			to.set(true);

			try {
				ponderThread.join();
				ponderThread = null;
			}
			catch(InterruptedException ie) {
			}
		}
	}

	public static void main(String [] args) {
		CaffeinatedPawn cp = new CaffeinatedPawn();

		Board b = new Board();

		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(in);

		for(;;) {
			String line = null;

			try {
				line = br.readLine();
			}
			catch(IOException io) {
				break;
			}

			String[] parts = line.split(" ");

			if (parts[0].equals("uci")) {
				System.out.println("id name CaffeinatedPawn");
				System.out.println("id author Folkert van Heusden");
				System.out.println("uciok");
			}
			else if (line.equals("ucinewgame"))
				b = new Board();
			else if (parts[0].equals("position")) {
				boolean moves = false;
				Side side = Side.WHITE;

				for(int i=1; i<parts.length;) {
					if (parts[i].equals("fen")) {
						String fen = new String();

						for(int f = i + 1; f<Math.min(i + 7, parts.length); f++)
							fen += parts[f] + " ";

						b.loadFromFen(fen);

						side = parts[i + 2].equals("w") ? Side.WHITE : Side.BLACK;

						i += 7;
					}
					else if (parts[i].equals("startpos")) {
						b = new Board();
						i++;
					}
					else if (parts[i].equals("moves")) {
						moves = true;
						i++;
					}
					else if (moves) {
						while(i < parts.length && parts[i].length() < 4)
							i++;

						Move m = new Move(parts[i], b.getSideToMove());
						b.doMove(m);

						i++;
					}
					else {
					}
				}
			}
			else if (parts[0].equals("go")) {
				cp.stopPonder();

				int movesToGo = 40 - b.getHistory().size() / 2;
				int wTime = 0, bTime = 0, wInc = 0, bInc = 0;
				boolean timeSet = false;

				for(int i=1; i<parts.length; i++) {
					if (parts[i].equals("depth"))
						i++;
					else if (parts[i].equals("movetime")) {
						wTime = bTime = Integer.parseInt(parts[++i]);
						timeSet = true;
					}
					else if (parts[i].equals("wtime"))
						wTime = Integer.parseInt(parts[++i]);
					else if (parts[i].equals("btime"))
						bTime = Integer.parseInt(parts[++i]);
					else if (parts[i].equals("winc"))
						wInc = Integer.parseInt(parts[++i]);
					else if (parts[i].equals("binc"))
						bInc = Integer.parseInt(parts[++i]);
					else if (parts[i].equals("movestogo"))
						movesToGo = Integer.parseInt(parts[++i]);
				}

				int thinkTime = 0;
				if (timeSet)
					thinkTime = b.getSideToMove() == Side.WHITE ? wTime : bTime;
				else {
					int curNMoves = movesToGo <= 0 ? 40 : movesToGo;

					int timeInc = b.getSideToMove() == Side.WHITE ? wInc : bInc;

					int ms = b.getSideToMove() == Side.WHITE ? wTime : bTime;

					thinkTime = (int)((ms + (curNMoves - 1) * timeInc) / (double)(curNMoves + 7));

					int limit_duration_min = ms / 15;
					if (thinkTime > limit_duration_min)
						thinkTime = limit_duration_min;
				}

				System.out.printf("# think time: %d\n", thinkTime);

				Result r = cp.doSearch(thinkTime, b);

				if (r == null)
					System.out.println("bestmove a1a1");
				else {
					System.out.print("bestmove ");
					System.out.println(r.m);
				}

				cp.startPonder(b);
			}
	                else if (line.equals("isready"))
                        	System.out.println("readyok");
			else if (line.equals("quit") || line.equals("exit"))
				break;
			else {
				System.out.println("# That (" + parts[0] + ") was not understood");
			} 

			System.out.flush();
		}
	}
}
