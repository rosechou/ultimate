package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;

/**
 * ProgramState Consists of one or many {@link ThreadState}s.
 * The <code> mValuation </code> only keeps global valuation.
 * All {@link ThreadState}s' (in <code> mThreadStates </code>) global valuations 
 * are references to this valuation. 
 * So if one thread changes the global variable, the valuation here also
 * changes.
 */
public class ProgramState extends ValuationState<ProgramState> {
	/**
	 * Thread ID to ThreadState
	 * One thread must contain only one thread state. 
	 */
	final Map<Integer, ThreadState> mThreadStates = new HashMap<>();
	
	public ProgramState(ThreadState threadState, Valuation globalValuation) {
		mValuation = globalValuation;
		addThreadState(threadState);
	}
	
	/**
	 * Deep copy a program state and let all the threadStates' global valuation 
	 * refer to <code> mValuation </code>.
	 */
	public ProgramState(ProgramState state) {
		mValuation = state.getValuationFullCopy();
		for(ThreadState s : state.getThreadStatesMap().values()) {
			ThreadState t = new ThreadState(s);
			t.getValuation().linkGlobals(mValuation);
			mThreadStates.put(s.getThreadID(), t);
		}
	}
	
	public List<ThreadStateTransition> getEnableTrans() {
		List<ThreadStateTransition> enableTrans = new ArrayList<>();
		for(final ThreadState threadState : mThreadStates.values()) {
			enableTrans.addAll(threadState.getEnableTrans());
		}
		return enableTrans;
	}
	
	/**
	 * One of thread state do the transition.
	 * (According to the threadID on the {@link ThreadStateTransition}).
	 */
	public ProgramState doTransition(final ThreadStateTransition trans) {
		ProgramState newProgramState = new ProgramState(this);
		ThreadState newState = newProgramState.getThreadStatesMap().get(trans.getThreadID()).doTransition(trans);
		/**
		 * update the thread state who did the transition.
		 */
		newProgramState.getThreadStatesMap().put(newState.getThreadID(), newState);
		return newProgramState;
	}


	public int getThreadNumber() {
		return mThreadStates.size();
	}
	
	private Map<Integer, ThreadState> getThreadStatesMap() {
		return mThreadStates;
	}
	
	public boolean allNonOldGlobalInitialized() {
		return mValuation.allNonOldGlobalInitialized();
	}
	
	private void addThreadState(ThreadState s) {
		if(mThreadStates.containsKey(s.getThreadID())) {
			throw new UnsupportedOperationException("Thread "
					+ String.valueOf(s.getThreadID()) + " already exists.");
		}
		mThreadStates.put(s.getThreadID(), s);
	}
	
	/**
	 * Check whether two program automaton states are equivalent.
	 * This method is needed in the nested DFS procedure. 
	 * @param anotherProgramState
	 * 		the state which is going to be compared to.
	 * @return
	 * 		true if two states are equivalent, false if not.
	 */
	@Override
	public boolean equals(final ProgramState anotherProgramState) {
		if(this.getThreadNumber() != anotherProgramState.getThreadNumber()) {
			return false;
		}
		/**
		 * Thread ID should be consistent?
		 */
		for(final ThreadState threadState : anotherProgramState.getThreadStatesMap().values()) {
			if(!mThreadStates.get(threadState.getThreadID()).equals(threadState)) {
				return false;
			}
		}
		return true;
	}
}
