package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionDeclaration;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.ExprEvaluator;

public class FuncInitValuationInfo {
	/**
	 * the initial valuation table of in params in a function.
	 * This is used in functionApplication evaluation in {@link ExprEvaluator#evaluate(Expression)}
	 * Type: function name × identifier × value
	 */
	private final Map<String, Map<String, Object>> mFuncInitValuation = new HashMap<>();
	private final Object outParamType;
	
	public FuncInitValuationInfo(final List<FunctionDeclaration> functionDeclarations) {
		mFuncInitValuation.putAll(createFuncInitValuation(functionDeclarations));
		this.outParamType = new Object();
	}
	
	private Map<String, Map<String, Object>> createFuncInitValuation(final List<FunctionDeclaration> functionDeclarations) {
		final VarAndParamAdder mVarAdder = new VarAndParamAdder();
		for(FunctionDeclaration funcDecl : functionDeclarations) {
			String funcName = funcDecl.getIdentifier();
			//...
		}
		return null;
	}
}
