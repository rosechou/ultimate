package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionDeclaration;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.Valuation;

public class FuncInitValuationInfo {
	/**
	 * the initial valuation of in params for each boogie function.
	 * This is used in functionApplication evaluation in {@link ExprEvaluator#evaluate(Expression)}
	 * Type: 
	 * String			× String		× Object	
	 * function name	× identifier	× value
	 */
	private final Valuation mFuncInitValuation = new Valuation();
	
	/**
	 * Function name to list of in params.
	 */
	private final Map<String, List<String>> mFunc2InParams =  new HashMap<>();
	private final Map<String, Expression> mFunc2Body = new HashMap<>();
	
	public FuncInitValuationInfo(final List<FunctionDeclaration> functionDeclarations) {
		createFuncInitValuation(functionDeclarations);
		createFunc2Body(functionDeclarations);
		createFunc2InParams();
	}
	
	private void createFunc2InParams() {
		for(String funcName : mFuncInitValuation.getProcOrFuncNames()) {
			Map<String, Object> inParam2Value = mFuncInitValuation.getProcOrFuncId2V(funcName);
			List<String> inParams = new ArrayList<>();
			for(String inParamName : inParam2Value.keySet()) {
				inParams.add(inParamName);
			}
			mFunc2InParams.put(funcName, inParams);
		}
	}

	private void createFuncInitValuation(final List<FunctionDeclaration> functionDeclarations) {
		final VarAndParamAdder mVarAdder = new VarAndParamAdder();
		for(FunctionDeclaration funcDecl : functionDeclarations) {
			mVarAdder.addFunInParams2Valuation(mFuncInitValuation, funcDecl);
		}
	}
	
	private void createFunc2Body(final List<FunctionDeclaration> functionDeclarations) {
		for(FunctionDeclaration funcDecl : functionDeclarations) {
			final String funcName = funcDecl.getIdentifier();
			final Expression funcBody = funcDecl.getBody();
			mFunc2Body.put(funcName, funcBody);
		}
	}
	
	public final Valuation getFuncInitValuation() {
		return mFuncInitValuation;
	}
	
	public final Expression getFuncBody(String funcName) {
		return mFunc2Body.get(funcName);
	}

	public final List<String> getInParams(String funcName) {
		return mFunc2InParams.get(funcName);
	}
}
