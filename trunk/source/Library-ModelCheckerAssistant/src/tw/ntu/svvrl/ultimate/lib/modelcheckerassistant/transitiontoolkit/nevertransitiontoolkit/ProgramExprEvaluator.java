package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.ExprEvaluator;

public class ProgramExprEvaluator extends ExprEvaluator<ProgramState> {
	
	public ProgramExprEvaluator(final ProgramState state) {
		super(state);
		mFuncInitValuationInfo = null;
		mFuncValuation = null;
	}
}
