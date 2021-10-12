import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

class CaffeinatedPawn {
	AtomicBoolean to = new AtomicBoolean(false);

	void timeoutThread(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch(InterruptedException ex) {
			System.out.println("# sleep interrupted");
		}

		to.set(true);
	}

	int evaluate(Board b) {
		int score = 0;

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
			score = -score;

		return score;
	}

	Result search(Board b, int depth, int alpha, int beta) {
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

		r.score = -32767;

		List<Move> moves = b.pseudoLegalMoves();

		int n_moves_tried = 0;

		for(Move move : moves) {
			if (b.isMoveLegal(move, true) == false)
				continue;

			b.doMove(move);
			n_moves_tried++;

			Result child = search(b, depth - 1, -beta, -alpha);
			if (child == null)
				return null;

			int score = -child.score;

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
			r.score = 0;
			return r;
		}

		return r;
	}

	public static void main(String [] args) {
		CaffeinatedPawn cp = new CaffeinatedPawn();

		int to_ms = 1000;

		Thread toThread = new Thread(() -> { cp.timeoutThread(to_ms); });
		toThread.start();

		Board b = new Board();

		for(;!b.isMated();) {
			Result r = cp.search(b, 3, -32767, 32767);
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
