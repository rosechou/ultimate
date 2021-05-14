package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionDeclaration;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.ExprEvaluator;

public class FuncInitValuationInfo {
	/**
	 * the initial valuation table of in params for each boogie function.
	 * This is used in functionApplication evaluation in {@link ExprEvaluator#evaluate(Expression)}
	 * Type: function name × identifier × value
	 */
	private final Map<String, Map<String, Object>> mFuncInitValuation = new HashMap<>();
	
	private final Map<String, Expression> mFunc2Body = new HashMap<>();
	
	public FuncInitValuationInfo(final List<FunctionDeclaration> functionDeclarations) {
		createFuncInitValuation(functionDeclarations);
	}
	
	private void createFuncInitValuation(final List<FunctionDeclaration> functionDeclarations) {
		final VarAndParamAdder mVarAdder = new VarAndParamAdder();
		for(FunctionDeclaration funcDecl : functionDeclarations) {
			mVarAdder.addInParams2Valuation(mFuncInitValuation, funcDecl);
			//process function body
		}
	}
	
	public final Map<String, Map<String, Object>> getFuncInitValuation() {
		return mFuncInitValuation;
	}

}
