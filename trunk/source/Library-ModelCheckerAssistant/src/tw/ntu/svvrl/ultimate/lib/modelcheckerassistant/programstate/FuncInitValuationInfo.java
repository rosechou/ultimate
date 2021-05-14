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
	 * Type: 
	 * String			× String		× Object	
	 * function name	× identifier	× value
	 */
	private final Map<String, Map<String, Object>> mFuncInitValuation = new HashMap<>();
	
	
	private final Map<String, Expression> mFunc2Body = new HashMap<>();
	
	public FuncInitValuationInfo(final List<FunctionDeclaration> functionDeclarations) {
		createFuncInitValuation(functionDeclarations);
		createFunc2Body(functionDeclarations);
	}
	
	private void createFuncInitValuation(final List<FunctionDeclaration> functionDeclarations) {
		final VarAndParamAdder mVarAdder = new VarAndParamAdder();
		for(FunctionDeclaration funcDecl : functionDeclarations) {
			mVarAdder.addInParams2Valuation(mFuncInitValuation, funcDecl);
		}
	}
	
	private void createFunc2Body(final List<FunctionDeclaration> functionDeclarations) {
		for(FunctionDeclaration funcDecl : functionDeclarations) {
			final String funcName = funcDecl.getIdentifier();
			final Expression funcBody = funcDecl.getBody();
			mFunc2Body.put(funcName, funcBody);
		}
	}
	
	public final Map<String, Map<String, Object>> getFuncInitValuation() {
		return mFuncInitValuation;
	}
	
	public final Expression getFuncBody(String funcName) {
		return mFunc2Body.get(funcName);
	}

}
