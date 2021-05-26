package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.List;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;

public class ProgramState extends ValuationState<ProgramState, ThreadStateTransition> {
	final List<ThreadState> mThreadStates = new ArrayList<>();
	
	public ProgramState(ThreadState threadState, Valuation globalValuation) {
		mThreadStates.add(threadState);
		mValuation = globalValuation;
	}
	
	public List<ThreadStateTransition> getEnableTrans() {
		List<ThreadStateTransition> enableTrans = new ArrayList<>();
		for(final ThreadState threadState : mThreadStates) {
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
	
	public List<ThreadState> getThreadStates() {
		return mThreadStates;
	}
	
	public boolean allNonOldGlobalInitialized() {
		return mValuation.allNonOldGlobalInitialized();
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
			if(!mThreadStates.get(i).equals(anotherProgramState.getThreadStates().get(i))) {
				return false;
			}
		}
		return true;
	}
}
