package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.neverstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;

public class NeverState {
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
	
	public List<OutgoingInternalTransition<CodeBlock, String>> getEnableTrans() {
		List<IcfgEdge> enableTrans = new ArrayList<>();
		//...
		return null;
	}
}
