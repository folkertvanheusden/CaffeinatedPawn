package com.vanheusden.CaffeinatedPawn;

import com.github.bhlangonijr.chesslib.move.Move;
import java.lang.Math.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Tt
{
	final int n_entries = 2097152, slots = 8;
	TtElement elements[][] = new TtElement[n_entries][slots];
	short age = 0;
	TtStats ts = new TtStats();

	Tt() {
		for(int entry=0; entry<n_entries; entry++) {
			for(int slot=0; slot<slots; slot++) {
				elements[entry][slot] = new TtElement();
			}
		}
	}

	void incAge() {
		age++;
	}

	void restartStats() {
		ts = new TtStats();
	}

	void dumpStats(Logger logger) {
		logger.log(Level.INFO, String.format("# tt lookups: %d, hits: %.2f%%\n", ts.lookups, ts.lookupHits * 100.0 / ts.lookups));

		logger.log(Level.INFO, String.format("# tt stores: %d, skipDepth: %.2f%%, skipFlag: %.2f%%, skipBest: %.2f%%, minDepth: %.2f%%, age: %.2f%%\n",
				ts.stores,
				ts.storesSkipDepth * 100.0 / ts.stores,
				ts.storesSkipFlag * 100.0 / ts.stores,
				ts.storesSkipBest * 100.0 / ts.stores,
				ts.storeMinDepth * 100.0 / ts.stores,
				ts.storeAge * 100.0 / ts.stores));
	}

	TtElement lookup(long hash) {
	        int index = (int)(hash & (n_entries - 1));
		
		ts.lookups++;

		for(int i=0; i<slots; i++) {
			if (elements[index][i].hash == hash) {
				elements[index][i].age = age;

				ts.lookupHits++;

				return elements[index][i];
			}
                }

		return null;
        }

	void store(long hash, ttFlag f, short depth, short score, Move m, Move m2) {
	        int index = (int)(hash & (n_entries - 1));

		ts.stores++;

		int useSubIndex = -1, minDepth = 999, mdi = -1;

		for(int i=0; i<slots; i++) {
			if (elements[index][i].hash == hash) {
				if (elements[index][i].depth > depth) {
					elements[index][i].age = age;
					ts.storesSkipDepth++;
					return;
				}

				if (f != ttFlag.EXACT && elements[index][i].depth == depth) {
					ts.storesSkipFlag++;
					elements[index][i].age = age;
					return;
				}

				useSubIndex = i;

				ts.storesSkipBest++;

				break;
			}

			if (elements[index][i].age != age)
				useSubIndex = i;
			else if (elements[index][i].depth < minDepth) {
				minDepth = elements[index][i].depth;
				mdi = i;
			}
		}

		if (useSubIndex == -1) {
			useSubIndex = mdi;
			ts.storeMinDepth++;
		}
		else {
			ts.storeAge++;
		}

		TtElement e = new TtElement();
		e.hash  = hash;
		e.score = score;
		e.depth = depth;
		e.f     = f;
		e.age   = age;
		e.m     = m;
		e.m2    = m2;

		elements[index][useSubIndex] = e;
	}

	int hashFullPermil() {
		int p = 0;

		for(int k=0; k<1000/slots; k++) {
			for(int i=0; i<slots; i++) {
				if (elements[k][i].m != null)
					p++;
			}
		}

		return p;
	}
}
