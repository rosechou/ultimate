package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.IState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit.NeverTransitionToolkit;

public class NeverState implements IState<NeverState>{
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
	
	public List<OutgoingInternalTransition<CodeBlock, NeverState>> getEnabledTrans(final ProgramState correspondingProgramState) {
		List<OutgoingInternalTransition<CodeBlock, NeverState>> enabledTrans = new ArrayList<>();
		/**
		 * All NonOld global variables must be initialized, or some errors
		 * will occur during expression evaluation.
		 * If some global variables are not initialized yet, return empty list.
		 */
		if(!correspondingProgramState.allNonOldGlobalInitialized()) {
			return enabledTrans;
		}
		
		for(final OutgoingInternalTransition<CodeBlock, NeverState> edge : mTranss) {
			final NeverTransitionToolkit transitionToolkit
			= new NeverTransitionToolkit(edge, this, correspondingProgramState);
			if (transitionToolkit.checkTransEnabled()) {
				enabledTrans.add(edge);
			}
		}
		return enabledTrans;
	}

	public NeverState doTransition(final OutgoingInternalTransition<CodeBlock, NeverState> edge
			, final ProgramState correspondingProgramState) {
		final NeverTransitionToolkit transitionToolkit 
			= new NeverTransitionToolkit(edge, this, correspondingProgramState);
		return (NeverState) transitionToolkit.doTransition();
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
