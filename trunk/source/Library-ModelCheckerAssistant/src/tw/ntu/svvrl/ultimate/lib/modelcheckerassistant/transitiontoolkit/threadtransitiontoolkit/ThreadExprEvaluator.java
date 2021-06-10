package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.Map;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.ExprEvaluator;

public class ThreadExprEvaluator extends ExprEvaluator<ThreadState> {

	public ThreadExprEvaluator(final ThreadState state, final ProgramStateExplorer pe) {
		super(state);
		mFuncInitValuationInfo = pe.getFuncInitValuationInfo();
		createFuncInitValuation(mFuncInitValuationInfo);
	}
	
	private void createFuncInitValuation(FuncInitValuationInfo funcInitValuationInfo) {
		mFuncValuation = funcInitValuationInfo.getFuncInitValuation().clone();
		
		/**
		 * Put all global variables to each function for {@link #evaluateFunctionApplication}
		 * to looking up.
		 * We can do this because boogie function has no side effects.
		 * i.e. No variable assignment.
		 */
		for(String funcName : mFuncValuation.getProcOrFuncNames()) {
			final Map<String, Object> globalVarMap = mValuation.getProcOrFuncId2V(null);
			for(final String globalVarName : globalVarMap.keySet()) {
				mFuncValuation.setValue(funcName, globalVarName, globalVarMap.get(globalVarName));
			}
		}
	}
}
