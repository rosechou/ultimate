package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.List;

public class ProgramState {
	final List<ThreadState> mThreadStates = new ArrayList<>();
	final Valuation mGlobalValuation;
	
	public ProgramState(ThreadState threadState, Valuation globalValuation) {
		mThreadStates.add(threadState);
		mGlobalValuation = globalValuation;
	}
	
	public int getThreadNumber() {
		return mThreadStates.size();
	}
}
