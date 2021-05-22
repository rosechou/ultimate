package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.State;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;

public class NeverState implements State<NeverState, OutgoingInternalTransition<CodeBlock, String>>{
	private final String mStateName;
	private final List<OutgoingInternalTransition<CodeBlock, String>> mTranss;
	
	private final boolean mIsInitial;
	private final boolean mIsFinal;
	
	public NeverState(String stateName, List<OutgoingInternalTransition<CodeBlock, String>> transs
			, boolean isInitial, boolean isFinal) {
		mStateName = stateName;
		mTranss = transs;
		mIsInitial = isInitial;
		mIsFinal = isFinal;
	}
	
	public boolean isFinal() {
		return mIsFinal;
	}
	
	@Override
	public List<OutgoingInternalTransition<CodeBlock, String>> getEnableTrans() {
		List<OutgoingInternalTransition<CodeBlock, String>> enableTrans = new ArrayList<>();
		for(final OutgoingInternalTransition<CodeBlock, String> edge : mTranss) {
			final TransitionToolkit<OutgoingInternalTransition<CodeBlock, String>, NeverState> transitionToolkit
			= new TransitionToolkit<OutgoingInternalTransition<CodeBlock, String>, NeverState>(edge, this);
			if (transitionToolkit.checkTransEnable()) {
				enableTrans.add(edge);
			}
		}
		return null;
	}
	
	public NeverState doTransition(final OutgoingInternalTransition<CodeBlock, String> edge
			, final ProgramState correspondingProgramState) {
		final TransitionToolkit<OutgoingInternalTransition<CodeBlock, String>, NeverState> transitionToolkit
		= new TransitionToolkit<OutgoingInternalTransition<CodeBlock, String>, NeverState>(edge, this);
		return (NeverState) transitionToolkit.doTransition(correspondingProgramState);
	}
}
