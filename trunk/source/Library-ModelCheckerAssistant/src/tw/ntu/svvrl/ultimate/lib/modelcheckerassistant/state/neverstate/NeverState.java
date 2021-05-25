package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.State;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;

public class NeverState implements State<NeverState, OutgoingInternalTransition<CodeBlock, NeverState>>{
	private final String mStateName;
	private final List<OutgoingInternalTransition<CodeBlock, NeverState>> mTranss = new ArrayList<>();
	
	private final boolean mIsInitial;
	private final boolean mIsFinal;
	
	public NeverState(String stateName, boolean isInitial, boolean isFinal) {
		mStateName = stateName;
		mIsInitial = isInitial;
		mIsFinal = isFinal;
	}
	
	public boolean isFinal() {
		return mIsFinal;
	}
	
	public void addTrans(OutgoingInternalTransition<CodeBlock, NeverState> trans) {
		mTranss.add(trans);
	}
	
	public String getName() {
		return mStateName;
	}
	
	public List<OutgoingInternalTransition<CodeBlock, NeverState>> getEnableTrans(final ThreadState correspondingProgramState) {
		List<OutgoingInternalTransition<CodeBlock, NeverState>> enableTrans = new ArrayList<>();
		/**
		 * All NonOld global variables must be initialized, or some errors
		 * will occur during expression evaluation.
		 * If some global variables are not initialized yet, return empty list.
		 */
		if(!correspondingProgramState.allNonOldGlobalInitialized()) {
			return enableTrans;
		}
		
		for(final OutgoingInternalTransition<CodeBlock, NeverState> edge : mTranss) {
			final TransitionToolkit<OutgoingInternalTransition<CodeBlock, NeverState>, NeverState> transitionToolkit
			= new TransitionToolkit<OutgoingInternalTransition<CodeBlock, NeverState>, NeverState>(edge, this);
			if (transitionToolkit.checkTransEnable(correspondingProgramState)) {
				enableTrans.add(edge);
			}
		}
		return enableTrans;
	}

	public NeverState doTransition(final OutgoingInternalTransition<CodeBlock, NeverState> edge
			, final ThreadState correspondingProgramState) {
		final TransitionToolkit<OutgoingInternalTransition<CodeBlock, NeverState>, NeverState> transitionToolkit
		= new TransitionToolkit<OutgoingInternalTransition<CodeBlock, NeverState>, NeverState>(edge, this);
		return (NeverState) transitionToolkit.doTransition(correspondingProgramState);
	}
	
	@Override
	public boolean equals(NeverState anotherState) {
		return mStateName.equals(anotherState.getName()) ? true : false;
	}
	
	@Override
	public String toString() {
		return "NeverState@" + mStateName;
	}
}
