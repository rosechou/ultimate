package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;

public class ProgramState {
	final List<ThreadState> mThreadStates = new ArrayList<>();
	final Valuation mGlobalValuation;
	
	public ProgramState(ThreadState threadState, Valuation globalValuation) {
		mThreadStates.add(threadState);
		mGlobalValuation = globalValuation;
	}
	
	public List<IcfgEdge> getEnableTrans() {
		List<IcfgEdge> enableTrans = new ArrayList<>();
		for(final ThreadState threadState : mThreadStates) {
			enableTrans.addAll(threadState.getEnableTrans());
		}
		return enableTrans;
	}
	

	public ProgramState doTransition(final IcfgEdge edge) {
		//...
		return null;
	}
	
	public int getThreadNumber() {
		return mThreadStates.size();
	}
	
	public List<ThreadState> getThreadStates() {
		return mThreadStates;
	}
	
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
