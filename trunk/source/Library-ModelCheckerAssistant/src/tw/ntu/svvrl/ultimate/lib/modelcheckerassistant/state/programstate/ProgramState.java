package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;

public class ProgramState extends ValuationState<ProgramState, ThreadStateTransition> {
	/**
	 * Thread ID to ThreadState
	 * One thread must contain only one thread state. 
	 */
	final Map<Integer, ThreadState> mThreadStates = new HashMap<>();
	
	public ProgramState(ThreadState threadState, Valuation globalValuation) {
		addThreadState(threadState);
		mValuation = globalValuation;
	}
	
	public List<ThreadStateTransition> getEnableTrans() {
		List<ThreadStateTransition> enableTrans = new ArrayList<>();
		for(final ThreadState threadState : mThreadStates.values()) {
			enableTrans.addAll(threadState.getEnableTrans());
		}
		return enableTrans;
	}
	

	public ProgramState doTransition(final ThreadStateTransition edge) {
		//...
		return null;
	}
	
	public int getThreadNumber() {
		return mThreadStates.size();
	}
	
	public Map<Integer, ThreadState> getThreadStatesMap() {
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
	
	@Override
	public boolean equals(final ProgramState anotherProgramState) {
		if(this.getThreadNumber() != anotherProgramState.getThreadNumber()) {
			return false;
		}
		/**
		 * The order of thread states should be consistent.
		 */
		for(int i = 0; i < this.getThreadNumber(); i++) {
			if(!mThreadStates.get(i).equals(anotherProgramState.getThreadStatesMap().get(i))) {
				return false;
			}
		}
		return true;
	}
}
