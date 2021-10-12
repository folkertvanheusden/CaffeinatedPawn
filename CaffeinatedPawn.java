import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import java.lang.Math.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

	Result search(Board b, short depth, short alpha, short beta, short maxDepth) {
		if (to.get())
			return null;

		Result r = new Result();

		if (b.isMated()) {
			r.score = -9999;
			return r;
		}

		if (depth == 0) {
			r.score = evaluate(b);
			return r;
		}

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

		List<Move> moves = b.pseudoLegalMoves();

		int n_moves_tried = 0;

		for(Move move : moves) {
			if (b.isMoveLegal(move, true) == false)
				continue;

			b.doMove(move);
			n_moves_tried++;

			Result child = search(b, (short)(depth - 1), (short)-beta, (short)-alpha, maxDepth);
			if (child == null)
				return null;

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

	public static void main(String [] args) {
		CaffeinatedPawn cp = new CaffeinatedPawn();

		int to_ms = 1000;

		Thread toThread = new Thread(() -> { cp.timeoutThread(to_ms); });
		toThread.start();

		Board b = new Board();

		for(;!b.isMated();) {
			Result r = cp.search(b, (short)3, (short)-32767, (short)32767, (short)3);
			if (r == null || r.m == null)
				break;

			b.doMove(r.m);

			System.out.print(r.m);
			System.out.print(' ');
			System.out.println(r.score);
			System.out.println(b);
			System.out.println("");
		}

		toThread.interrupt();
		try {
			toThread.join();
		}
		catch(InterruptedException ie) {
		}
	}
}
