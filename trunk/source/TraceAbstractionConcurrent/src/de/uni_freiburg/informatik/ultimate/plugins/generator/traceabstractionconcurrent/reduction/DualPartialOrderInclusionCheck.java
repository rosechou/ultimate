package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstractionconcurrent.reduction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import de.uni_freiburg.informatik.ultimate.automata.Word;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomataUtils;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.independencerelation.IIndependenceRelation;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;

/**
 * Employs a modified sleep set algorithm to check if a given proof is
 * sufficient to prove a given program correct. A proof is considered
 * sufficient, if its closure under two independence relations (in a fixed
 * order) contains the program.
 *
 * As this problem is undecidable, the check implemented here is sound but not
 * complete: Even when a counterexample is found, the proof might actually have
 * been sufficient.
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
	private final Set<SearchState> mStack = new HashSet<>();

	private final boolean mAssumeProofSinkAccept;

	public DualPartialOrderInclusionCheck(final IIndependenceRelation<STATE2, LETTER> relation1,
			final IIndependenceRelation<STATE2, LETTER> relation2, final INestedWordAutomaton<LETTER, STATE1> program,
			final INestedWordAutomaton<LETTER, STATE2> proof, final boolean assumeProofSinkAccept) {
		mRelation1 = relation1;
		mRelation2 = relation2;

		mProgram = program;
		mProof = proof;
		mAssumeProofSinkAccept = assumeProofSinkAccept;

		assert NestedWordAutomataUtils.isFiniteAutomaton(program) : "POR does not support calls and returns.";
		assert NestedWordAutomataUtils.isFiniteAutomaton(proof) : "POR does not support calls and returns.";

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
		final Pair<ArrayDeque<LETTER>, Boolean> result = search(getInitial(mProgram), getInitial(mProof),
				Collections.emptySet(), Collections.emptySet());
		final ArrayDeque<LETTER> symbols = result.getFirst();
		if (symbols != null) {
			return createRun(symbols);
		}
		return null;
	}

	private final Pair<ArrayDeque<LETTER>, Boolean> search(final STATE1 location, final STATE2 predicate,
			final Set<LETTER> sleepSet1, final Set<LETTER> sleepSet2) {

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
			final Set<LETTER> sleepSet = getActiveSleepSet(switched, sleepSet1, sleepSet2);
			final boolean callSwitched;

			if (sleepSet.contains(a)) {
				callSwitched = switched;
			} else {
				final STATE1 nextLocation = getSuccessor(mProgram, location, a);
				final STATE2 nextPredicate = getSuccessor(mProof, predicate, a);

				final Set<LETTER> nextSleep1 = recomputeSleep(false, sleepSet1, done, predicate, a);
				final Set<LETTER> nextSleep2 = recomputeSleep(true, sleepSet2, done, predicate, a);

				final SearchState nextNode = new SearchState(nextLocation, nextPredicate, nextSleep1, nextSleep2);
				if (mStack.contains(nextNode)) {
					// We don't know if the delayed call will switch.
					// For the moment, we simply assume it will.
					// This is a safe assumption, but possibly too strict.
					//
					// Alternative solutions:
					// - always, or heuristically, assume it won't; then enforce it (also possibly too strict)
					// - assume it won't; but if it does, perform a partial restart of the reduction from this point on (more complex, and possibly in vain)
					// - if we record explored (AND about-to-be explored) actions, and avoid re-exploring them, can we just not delay calls but do them immediately?
					//   termination argument: the set of unexplored actions strictly decreases with each call for the same location, if it is empty we terminate this call
					//     soundness argument: ?
					callSwitched = true;
				} else {
					mStack.add(nextNode);
					final Pair<ArrayDeque<LETTER>, Boolean> result = search(nextLocation, nextPredicate, nextSleep1,
							nextSleep2);
					final ArrayDeque<LETTER> counterexample = result.getFirst();
					mStack.remove(nextNode);

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

		return new Pair<>(null, switched);
	}

	private Set<LETTER> getActiveSleepSet(final boolean switched, final Set<LETTER> sleepSet1,
			final Set<LETTER> sleepSet2) {
		if (switched) {
			return sleepSet2;
		} else {
			return sleepSet1;
		}
	}

	private final Set<LETTER> recomputeSleep(final boolean switched, final Set<LETTER> oldSleepSet,
			final Set<Pair<LETTER, Boolean>> done, final STATE2 context, final LETTER action) {
		final Set<LETTER> newSleepSet = new HashSet<>(oldSleepSet.size() + done.size());
		final IIndependenceRelation<STATE2, LETTER> relation = switched ? mRelation1 : mRelation2;

		for (final LETTER sleepAction : oldSleepSet) {
			if (relation.contains(context, sleepAction, action)) {
				newSleepSet.add(sleepAction);
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

	private <STATE> STATE getSuccessor(final INestedWordAutomaton<LETTER, STATE> automaton, final STATE state,
			final LETTER letter) {
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
			return mLocation == other.mLocation && mPredicate == other.mPredicate
					&& Objects.equals(mSleep1, other.mSleep1) && mSleep2.equals(other.mSleep2);
		}
	}
}
