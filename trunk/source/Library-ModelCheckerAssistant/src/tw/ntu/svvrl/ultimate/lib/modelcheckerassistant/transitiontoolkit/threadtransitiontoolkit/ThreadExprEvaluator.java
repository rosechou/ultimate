package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayAccessExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayStoreExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BitVectorAccessExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BitvecLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BooleanLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionApplication;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IfThenElseExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IntegerLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.QuantifierExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.RealLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StringLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructAccessExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructConstructor;
import de.uni_freiburg.informatik.ultimate.boogie.ast.UnaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WildcardExpression;
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
	
	public boolean checkAccessOnlyLocalVar(final Expression expr) {
		if(expr instanceof ArrayAccessExpression) {
			return checkArrayAccessExpression((ArrayAccessExpression) expr);
		} else if(expr instanceof ArrayStoreExpression) {
			return checkArrayStoreExpression((ArrayStoreExpression) expr);
		} else if(expr instanceof BinaryExpression) {
			return checkBinaryExpression((BinaryExpression) expr);
		} else if(expr instanceof BitvecLiteral) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof BitVectorAccessExpression) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof BooleanLiteral) {
			return true;
		} else if(expr instanceof FunctionApplication) {
			return checkFunctionApplication((FunctionApplication) expr);
		} else if(expr instanceof IdentifierExpression) {
			return checkIdentifierExpression((IdentifierExpression) expr);
		} else if(expr instanceof IfThenElseExpression) {
			return checkIfThenElseExpression((IfThenElseExpression) expr);
		} else if(expr instanceof IntegerLiteral) {
			return true;
		} else if(expr instanceof QuantifierExpression) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof RealLiteral) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof StringLiteral) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof StructAccessExpression) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof StructConstructor) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof UnaryExpression) {
			return checkUnaryExpression((UnaryExpression) expr);
		} else if(expr instanceof WildcardExpression) {
			return true;
		} else {
			throw new UnsupportedOperationException("Unknown Expression type: "
					+ expr.getClass().getSimpleName());
		}
	}

	private boolean checkArrayAccessExpression(final ArrayAccessExpression expr) {
		final Expression[] indices = expr.getIndices();
		for(final Expression index : indices) {
			if(!checkAccessOnlyLocalVar(index)) {
				return false;
			}
		}
		return checkAccessOnlyLocalVar(expr.getArray());
	}

	private boolean checkArrayStoreExpression(final ArrayStoreExpression expr) {
		final Expression[] indices = expr.getIndices();
		for(final Expression index : indices) {
			if(!checkAccessOnlyLocalVar(index)) {
				return false;
			}
		}
		return checkAccessOnlyLocalVar(expr.getArray())
				&& checkAccessOnlyLocalVar(expr.getValue());
	}

	private boolean checkBinaryExpression(final BinaryExpression expr) {
		return checkAccessOnlyLocalVar(expr.getLeft())
				&& checkAccessOnlyLocalVar(expr.getRight());
	}

	private boolean checkFunctionApplication(final FunctionApplication expr) {
		final Expression[] args = expr.getArguments();
		for(final Expression arg : args) {
			if(!checkAccessOnlyLocalVar(arg)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkIdentifierExpression(final IdentifierExpression expr) {
		final String procName = expr .getDeclarationInformation().getProcedure();
		if(procName == null) {
			return false;
		}
		return true;
	}

	private boolean checkIfThenElseExpression(final IfThenElseExpression expr) {
		return checkAccessOnlyLocalVar(expr.getCondition())
				&& checkAccessOnlyLocalVar(expr.getThenPart())
				&& checkAccessOnlyLocalVar(expr.getElsePart());
	}

	private boolean checkUnaryExpression(final UnaryExpression expr) {
		return checkAccessOnlyLocalVar(expr.getExpr());
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
