package com.vanheusden.CaffeinatedPawn;

import com.github.bhlangonijr.chesslib.move.Move;
import java.lang.Math.*;

class Tt
{
	final int n_entries = 2097152, slots = 8;
	TtElement elements[][] = new TtElement[n_entries][slots];
	short age = 0;

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

	TtElement lookup(long hash) {
	        int index = (int)Math.abs(hash % n_entries);

		for(int i=0; i<slots; i++) {
			if (elements[index][i].hash == hash) {
				elements[index][i].age = age;

				return elements[index][i];
			}
                }

		return null;
        }

	void store(long hash, ttFlag f, short depth, short score, Move m) {
	        int index = (int)Math.abs(hash % n_entries);

		int useSubIndex = -1, minDepth = 999, mdi = -1;

		for(int i=0; i<slots; i++) {
			if (elements[index][i].hash == hash) {
				if (elements[index][i].depth > depth) {
					elements[index][i].age = age;
					return;
				}

				if (f != ttFlag.EXACT && elements[index][i].depth == depth) {
					elements[index][i].age = age;
					return;
				}

				useSubIndex = i;

				break;
			}

			if (elements[index][i].age != age)
				useSubIndex = i;
			else if (elements[index][i].depth < minDepth) {
				minDepth = elements[index][i].depth;
				mdi = i;
			}
		}

		if (useSubIndex == -1)
			useSubIndex = mdi;

		TtElement e = new TtElement();
		e.hash  = hash;
		e.score = score;
		e.depth = depth;
		e.f     = f;
		e.age   = age;
		e.m     = m;

		elements[index][useSubIndex] = e;
	}
}
