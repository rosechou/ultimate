package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.neverstate;

import java.util.HashMap;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

public class NeverState {
	private final String mStateName;
	private final OutgoingInternalTransition<CodeBlock, String> mTrans;
	
	private final boolean mIsInitial;
	private final boolean mIsFinal;
	
	public NeverState(String stateName, OutgoingInternalTransition<CodeBlock, String> trans
			, boolean isInitial, boolean isFinal) {
		mStateName = stateName;
		mTrans = trans;
		mIsInitial = isInitial;
		mIsFinal = isFinal;
	}
}
