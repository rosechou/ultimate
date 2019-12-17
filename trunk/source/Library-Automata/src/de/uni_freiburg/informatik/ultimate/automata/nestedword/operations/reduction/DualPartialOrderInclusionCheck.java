/*
 * Copyright (C) 2019 Dominik Klumpp (klumpp@informatik.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
 *
 * This file is part of the ULTIMATE Automata Library.
 *
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Automata Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.reduction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import de.uni_freiburg.informatik.ultimate.automata.Word;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomataUtils;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;

/**
 * Employs a modified sleep set algorithm to check if a given proof is
 * sufficient to prove a given program correct. A proof is considered
 * sufficient, if its closure under two independence relations (in a fixed
 * order) contains the program.
 *
 * As this problem is undecidable (even for a single independence relation), the
 * check implemented here is sound but not complete: Even when a counterexample
 * is found, the proof might actually have been sufficient.
 *
 * @author Dominik Klumpp (klumpp@informatik.uni-freiburg.de)
 *
 * @param <STATE1>
 * @param <STATE2>
 * @param <LETTER>
 */
public class DualPartialOrderInclusionCheck<STATE1, STATE2, LETTER> {
	private final IIndependenceRelation<STATE2, LETTER> mRelation1;
	private final IIndependenceRelation<STATE2, LETTER> mRelation2;

	private final INestedWordAutomaton<LETTER, STATE1> mProgram;
	private final INestedWordAutomaton<LETTER, STATE2> mProof;
	private final NestedRun<LETTER, STATE1> mCounterexample;

	private final LinkedList<SearchState> mStack = new LinkedList<>();
	private final Set<SearchState> mStackSet = new HashSet<>();

	private final ArrayDeque<Integer> mRestartQueue = new ArrayDeque<>();
	private final Map<SearchState, Boolean> mVisited = new HashMap<>();

	private final boolean mAssumeProofSinkAccept;

	public DualPartialOrderInclusionCheck(final IIndependenceRelation<STATE2, LETTER> relation1,
			final IIndependenceRelation<STATE2, LETTER> relation2, final INestedWordAutomaton<LETTER, STATE1> program,
			final INestedWordAutomaton<LETTER, STATE2> proof, final boolean assumeProofSinkAccept) {
		mRelation1 = relation1;
		mRelation2 = relation2;

		mProgram = program;
		mProof = proof;
		mAssumeProofSinkAccept = assumeProofSinkAccept;

		if (!NestedWordAutomataUtils.isFiniteAutomaton(program) || !NestedWordAutomataUtils.isFiniteAutomaton(proof)) {
			throw new UnsupportedOperationException("POR does not support calls and returns.");
		}

		mCounterexample = performCheck();
	}

	/**
	 * @return The result of the inclusion check: {@code true} if the program is
	 *         guaranteed to be covered by the proof, {@code false} otherwise.
	 */
	public boolean getResult() {
		return mCounterexample == null;
	}

	/**
	 * Retrieves the counterexample found during the search, if any. This is an
	 * automaton run leading from the program's initial location to an error
	 * location. The corresponding word is guaranteed not to be accepted by the
	 * proof automaton.
	 */
	public NestedRun<LETTER, STATE1> getCounterexample() {
		return mCounterexample;
	}

	private final NestedRun<LETTER, STATE1> performCheck() {
		final STATE1 initialLoc = getInitial(mProgram);
		final STATE2 initialPred = getInitial(mProof);
		final SearchState initial = new SearchState(initialLoc, initialPred, Collections.emptySet(),
				Collections.emptySet());

		mStack.addLast(initial);
		mStackSet.add(initial);
		final Pair<ArrayDeque<LETTER>, Boolean> result = search(initial);
		mStackSet.remove(initial);
		mStack.removeLast();

		assert mStack.isEmpty() : "Stack should be empty after complete backtracking";

		final ArrayDeque<LETTER> symbols = result.getFirst();
		if (symbols != null) {
			return createRun(symbols);
		}
		return null;
	}

	private final Pair<ArrayDeque<LETTER>, Boolean> search(final SearchState currentNode) {
		final STATE1 location = currentNode.mLocation;
		final STATE2 predicate = currentNode.mPredicate;
		final Set<LETTER> sleepSet1 = currentNode.mSleep1;
		final Set<LETTER> sleepSet2 = currentNode.mSleep2;

		if (mProgram.isFinal(location) && !mProof.isFinal(predicate)) {
			// A counterexample has been found.
			return new Pair<>(new ArrayDeque<>(), false);
		} else if (mProof.isFinal(predicate) && mAssumeProofSinkAccept) {
			// Assumes any final state of mProof is a sink state.
			// Hence we can abort the search here.
			return new Pair<>(null, false);
		}

		final Set<LETTER> enabledActions = mProgram.lettersInternal(location);
		final Set<Pair<LETTER, Boolean>> done = new HashSet<>(enabledActions.size());
		boolean switched = false;

		for (final LETTER a : enabledActions) {
			final boolean callSwitched;

			final Set<LETTER> sleepSet = switched ? sleepSet1 : sleepSet2;
			if (sleepSet.contains(a)) {
				callSwitched = switched;
			} else {
				final STATE1 nextLocation = getSuccessor(mProgram, location, a);
				final STATE2 nextPredicate = getSuccessor(mProof, predicate, a);

				final Set<LETTER> nextSleep1 = recomputeSleep(false, sleepSet1, sleepSet2, done, predicate, a);
				final Set<LETTER> nextSleep2 = recomputeSleep(true, sleepSet1, sleepSet2, done, predicate, a);

				final SearchState nextNode = new SearchState(nextLocation, nextPredicate, nextSleep1, nextSleep2);
				final boolean onStack = mStackSet.contains(nextNode);
				if (onStack || mVisited.containsKey(nextNode)) {
					callSwitched = mVisited.getOrDefault(nextNode, false);
					if (onStack && !callSwitched) {
						// We don't know if the ongoing call will switch. For the moment, we assume it
						// will not (default value false above). We later verify this and restart if our
						// assumption was incorrect.
						final int index = mStack.indexOf(nextNode);
						if (mRestartQueue.isEmpty() || index <= mRestartQueue.element()) {
							// save restart point, unless there already is one higher in the hierarchy
							mRestartQueue.offerLast(index);
						}
					}
				} else {
					mStack.addLast(nextNode);
					mStackSet.add(nextNode);
					final Pair<ArrayDeque<LETTER>, Boolean> result = search(nextNode);
					mStackSet.remove(nextNode);
					mStack.removeLast();

					final ArrayDeque<LETTER> counterexample = result.getFirst();
					if (counterexample == null) {
						callSwitched = result.getSecond();
					} else if (sleepSet2.contains(a)) {
						callSwitched = true;
					} else {
						callSwitched = result.getSecond();
						counterexample.addFirst(a);
						return new Pair<>(counterexample, switched || callSwitched);
					}
				}
			}
			done.add(new Pair<>(a, callSwitched));
			switched = switched || callSwitched;
		}
		mVisited.put(currentNode, switched);

		if (!mRestartQueue.isEmpty() && mRestartQueue.element() == mStack.size() - 1 && switched) {
			mRestartQueue.removeFirst();
			// NOTE Dominik 2019-12-04: We currently re-explore the entire subgraph, instead
			// of returning directly to the node which inserted the marker in the restart
			// queue. There is potential for future optimization here.
			final Pair<ArrayDeque<LETTER>, Boolean> result = search(currentNode);
			if (result.getFirst() != null) {
				mVisited.remove(currentNode);
			}
			return result;
		}

		return new Pair<>(null, switched);
	}

	private final Set<LETTER> recomputeSleep(final boolean switched, final Set<LETTER> oldSleepSet1,
			final Set<LETTER> oldSleepSet2, final Set<Pair<LETTER, Boolean>> done, final STATE2 context,
			final LETTER action) {
		final Set<LETTER> newSleepSet = new HashSet<>(
				oldSleepSet1.size() + (switched ? oldSleepSet2.size() : 0) + done.size());
		final IIndependenceRelation<STATE2, LETTER> relation = switched ? mRelation1 : mRelation2;

		for (final LETTER sleepAction : oldSleepSet1) {
			if (relation.contains(context, sleepAction, action)) {
				newSleepSet.add(sleepAction);
			}
		}

		if (switched) {
			for (final LETTER sleepAction : oldSleepSet2) {
				if (relation.contains(context, sleepAction, action)) {
					newSleepSet.add(sleepAction);
				}
			}
		}

		for (final Pair<LETTER, Boolean> donePair : done) {
			final LETTER doneAction = donePair.getFirst();
			final boolean doneSwitched = donePair.getSecond();
			if ((!doneSwitched || switched) && relation.contains(context, doneAction, action)) {
				newSleepSet.add(doneAction);
			}
		}

		return newSleepSet;
	}

	private NestedRun<LETTER, STATE1> createRun(final ArrayDeque<LETTER> symbols) {
		final Word<LETTER> word = new Word<>((LETTER[]) symbols.toArray());
		final NestedWord<LETTER> nestedWord = NestedWord.nestedWord(word);

		final ArrayList<STATE1> stateSequence = new ArrayList<>(word.length() + 1);
		STATE1 current = getInitial(mProgram);
		stateSequence.add(current);
		for (final LETTER a : word) {
			current = getSuccessor(mProgram, current, a);
			stateSequence.add(current);
		}

		return new NestedRun<>(nestedWord, stateSequence);
	}

	// TODO: determinize automata, use built-in product automata
	private static <STATE> STATE getInitial(final INestedWordAutomaton<?, STATE> automaton) {
		final Set<STATE> initial = automaton.getInitialStates();
		assert initial.size() == 1 : "Automaton must be deterministic";
		return initial.iterator().next();
	}

	private static <STATE, LETTER> STATE getSuccessor(final INestedWordAutomaton<LETTER, STATE> automaton,
			final STATE state, final LETTER letter) {
		// TODO: there must be a much better way than this, this is horrible
		final Set<STATE> successors = StreamSupport
				.stream(automaton.internalSuccessors(state, letter).spliterator(), false)
				.map(OutgoingInternalTransition::getSucc).collect(Collectors.toSet());
		assert successors.size() == 1 : "Automaton must be deterministic";
		return successors.iterator().next();
	}

	private final class SearchState {
		private final STATE1 mLocation;
		private final STATE2 mPredicate;
		private final Set<LETTER> mSleep1;
		private final Set<LETTER> mSleep2;

		public SearchState(final STATE1 loc, final STATE2 pred, final Set<LETTER> sleep1, final Set<LETTER> sleep2) {
			mLocation = loc;
			mPredicate = pred;
			mSleep1 = sleep1;
			mSleep2 = sleep2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mLocation == null) ? 0 : mLocation.hashCode());
			result = prime * result + ((mPredicate == null) ? 0 : mPredicate.hashCode());
			result = prime * result + ((mSleep1 == null) ? 0 : mSleep1.hashCode());
			result = prime * result + ((mSleep2 == null) ? 0 : mSleep2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;

			@SuppressWarnings("unchecked")
			SearchState other = (SearchState) obj;
			return mLocation == other.mLocation && mPredicate == other.mPredicate && mSleep1.equals(other.mSleep1)
					&& mSleep2.equals(other.mSleep2);
		}
	}
}
