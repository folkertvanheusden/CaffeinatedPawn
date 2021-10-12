import com.github.bhlangonijr.chesslib.move.Move;

enum ttFlag { NOTVALID, EXACT, LOWERBOUND, UPPERBOUND };

class TtElement
{
	long hash;
	short score;
	ttFlag f;
	short age;
	short depth;
	Move m;
}
