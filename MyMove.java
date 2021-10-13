import com.github.bhlangonijr.chesslib.move.Move;

class MyMove implements Comparable<MyMove> {
	Move m;
	int score;

	MyMove(Move mIn, int s) {
		m = mIn;
		score = s;
	}

	@Override
	public int compareTo(MyMove other) {
		return other.score - score;
	}

	Move getMove() {
		return m;
	}
}
