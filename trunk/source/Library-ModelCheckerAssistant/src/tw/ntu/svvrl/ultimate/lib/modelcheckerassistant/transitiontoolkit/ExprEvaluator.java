package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;

public class ExprEvaluator<S extends ValuationState<S>> {
	protected final Valuation mValuation;
	
	/**
	 * Initial function value table and function bodies.
	 */
	protected FuncInitValuationInfo mFuncInitValuationInfo;
	

	/**
	 * Actual value table in the execution of a function.
	 * After functionApplicationExpr finished, this table should be
	 * reset to <code>mFuncInitValuation<code>.
	 */
	protected Valuation mFuncValuation;
	
	/**
	 * To indicate recursive function application.
	 * If not empty stack, lookup <code>mFuncValuation<code>.
	 * If empty stack, not in a function, lookup <code>mValuation<code>.
	 */
	protected Stack<String> mFuncNameStack = new Stack<>();
	
	protected ExprEvaluator(final S state) {
		mValuation = state.getValuation();
	}
	
	public Object evaluate(final Expression expr) {
		if(expr instanceof ArrayAccessExpression) {
			return evaluateArrayAccessExpression((ArrayAccessExpression) expr);
		} else if(expr instanceof ArrayStoreExpression) {
			return evaluateArrayStoreExpression((ArrayStoreExpression) expr);
		} else if(expr instanceof BinaryExpression) {
			return evaluateBinaryExpression((BinaryExpression) expr);
		} else if(expr instanceof BitvecLiteral) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof BitVectorAccessExpression) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		} else if(expr instanceof BooleanLiteral) {
			return evaluateBooleanLiteral((BooleanLiteral) expr);
		} else if(expr instanceof FunctionApplication) {
			return evaluateFunctionApplication((FunctionApplication) expr);
		} else if(expr instanceof IdentifierExpression) {
			return evaluateIdentifierExpression((IdentifierExpression) expr);
		} else if(expr instanceof IfThenElseExpression) {
			return evaluateIfThenElseExpression((IfThenElseExpression) expr);
		} else if(expr instanceof IntegerLiteral) {
			return evaluateIntegerLiteral((IntegerLiteral) expr);
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
			return evaluateUnaryExpression((UnaryExpression) expr);
		} else if(expr instanceof WildcardExpression) {
			return evaluateWildcardExpression((WildcardExpression) expr);
		} else {
			throw new UnsupportedOperationException("Unknown Expression type: "
					+ expr.getClass().getSimpleName());
		}
	}

	private Object evaluateWildcardExpression(final WildcardExpression expr) {
		Random random = new Random();
	    return random.nextBoolean();
	}

	private Object evaluateUnaryExpression(final UnaryExpression expr) {
		final UnaryExpression.Operator operator = expr.getOperator();
		final Object rv =  evaluate(expr.getExpr());
		switch(operator) {
			case LOGICNEG:
				return !(Boolean) rv;
			case ARITHNEGATIVE:
				return -(Long) rv;
			case OLD:
				if(rv instanceof IdentifierExpression) {
					String oldId = "old(" + ((IdentifierExpression) rv).getIdentifier() + ")";
					return evaluate(new IdentifierExpression(((IdentifierExpression) rv).getLoc(),
							((IdentifierExpression) rv).getType(), oldId, 
							((IdentifierExpression) rv).getDeclarationInformation()));
				} else {
					return rv;
				}
			default:
				throw new UnsupportedOperationException("Unknown Unary Operator: "
						+ operator.getClass().getSimpleName());
		}
	}

	private Object evaluateIntegerLiteral(final IntegerLiteral expr) {
		return Long.valueOf(expr.getValue());
	}

	private Object evaluateIfThenElseExpression(final IfThenElseExpression expr) {
		if((boolean) evaluate(expr.getCondition())) {
			return evaluate(expr.getThenPart());
		} else {
			return evaluate(expr.getElsePart());
		}
	}

	private Object evaluateIdentifierExpression(final IdentifierExpression expr) {
		if(mFuncNameStack.isEmpty()) {
			final String procName = expr.getDeclarationInformation().getProcedure();
			final String varName = expr.getIdentifier();
			return mValuation.lookUpValue(procName, varName);
		} else {
			final String funcName = mFuncNameStack.peek();
			final String varName = expr.getIdentifier();
			return mFuncValuation.lookUpValue(funcName, varName);
		}
	}

	/**
	 * This function will not work correctly with funciton inliner in Boogie preprocessor.
	 * With funciton inliner,
	 * the function body in the {@link FunctionDeclaration} will be <code>null<code>.
	 * And all information about the function body is stored in the form of
	 * formulae in axioms.
	 * 
	 * To avoid this situation, set "Use function inliner" to false
	 * in the preference of boogie preprocessor.
	 */
	private Object evaluateFunctionApplication(final FunctionApplication expr) {
		assert mFuncInitValuationInfo != null;
		String funcName = expr.getIdentifier();
		mFuncNameStack.push(funcName);
		/**
		 * Set parameters' values.
		 */
		Expression[] args = expr.getArguments();
		List<String> argsName = mFuncInitValuationInfo.getInParams(funcName);
		assert(args.length == argsName.size());
		for(int i = 0; i < args.length; i++) {
			mFuncValuation.setValue(funcName, argsName.get(i), evaluate(args[i]));
		}
		/**
		 * Type casting is not yet implemented.
		 * Or it is no need to do this because of the existence
		 * of boogie type checker ?
		 */
		Object v = evaluate(mFuncInitValuationInfo.getFuncBody(mFuncNameStack.peek()));
		
		mFuncNameStack.pop();
		return v;
	}

	private Object evaluateBooleanLiteral(final BooleanLiteral expr) {
		return expr.getValue();
	}

	private Object evaluateBinaryExpression(final BinaryExpression expr) {
		BinaryExpression.Operator operator = expr.getOperator();
		final Object lv =  evaluate(expr.getLeft());
		final Object rv =  evaluate(expr.getRight());
		switch(operator) {
			case LOGICIFF:
				return (!(Boolean) lv || (Boolean) rv) && (!(Boolean) rv || (Boolean) lv);
			case LOGICIMPLIES:
				return !(Boolean) lv || (Boolean) rv;
			case LOGICAND:
				return (Boolean) lv && (Boolean) rv;
			case LOGICOR:
				return (Boolean) lv || (Boolean) rv;
			case COMPLT:
				return (Long) lv < (Long) rv;
			case COMPGT:
				return (Long) lv > (Long) rv;
			case COMPLEQ:
				return (Long) lv <= (Long) rv;
			case COMPGEQ:
				return (Long) lv >= (Long) rv;
			case COMPEQ:
				if(lv instanceof Boolean && rv instanceof Boolean) {
					return (Boolean) lv == (Boolean) rv;
				} else if(lv instanceof Long && rv instanceof Long) {
					return (Long) lv == (Long) rv;
				} else {
					throw new UnsupportedOperationException("Binary operation type error.");
				}
			case COMPNEQ:
				if(lv instanceof Boolean && rv instanceof Boolean) {
					return (Boolean) lv != (Boolean) rv;
				} else if(lv instanceof Long && rv instanceof Long) {
					return (Long) lv != (Long) rv;
				} else {
					throw new UnsupportedOperationException("Binary operation type error.");
				}
			case COMPPO:
				throw new UnsupportedOperationException("Binary operation \"COMPPO\""
						+ "is not yet supported.");
			case BITVECCONCAT:
				throw new UnsupportedOperationException("Binary operation \"BITVECCONCAT\""
						+ "is not yet supported.");
			case ARITHPLUS:
				return (Long) lv + (Long) rv;
			case ARITHMINUS:
				return (Long) lv - (Long) rv;
			case ARITHMUL:
				return (Long) lv * (Long) rv;
			case ARITHDIV:
				return (Long) lv / (Long) rv;
			case ARITHMOD:
				return (Long) lv % (Long) rv;
			default:
				throw new UnsupportedOperationException("Unknown Binary Operator: "
						+ operator.getClass().getSimpleName());
		}
	}

	private Object evaluateArrayStoreExpression(final ArrayStoreExpression expr) {
		Map<Object, Object> map = (Map<Object, Object>) evaluate(expr.getArray());
		final List<Expression> indexExprs = Arrays.asList(expr.getIndices());
		final Iterator<Expression> it = indexExprs.iterator();
		
		final List<Integer> indexList = new ArrayList<>();
		while(it.hasNext()) {
			final Expression indexExpr = it.next();
			final int index = Math.toIntExact((long) evaluate(indexExpr));
			indexList.add(index);
		}
		map.put(indexList, evaluate(expr.getValue()));
		
		return new HashMap<>(map);
		
		/**
		 * Old implementation using java Array, now use java Map.
		 */
//		ArrayList<Object> newArray = new ArrayList<>();
//		newArray = (ArrayList<Object>) evaluate(expr.getArray());
//		
//		final List<Expression> indexExprs = Arrays.asList(expr.getIndices());
//		final Iterator<Expression> it = indexExprs.iterator();
//		while(it.hasNext()) {
//			/**
//			 * If array size is too small, grow it.
//			 * We do not need to consider whether this array will be out of bound here.
//			 * Because the rcfg has constructed the error state for us.
//			 * (Toggle on "Check array bounds for arrays that are off heap"
//			 * in the preference of cacsl2boogietranslator.)
//			 */
//			final Expression indexExpr = it.next();
//			final int index = Math.toIntExact((long) evaluate(indexExpr));
//			
//			assert(newArray.size() > 0);
//			newArray = growArraySize(newArray, index + 1);
//			
//			if(!it.hasNext()) {
//				newArray.set(index, evaluate(expr.getValue()));
//			}
//		}
//		
//		return newArray;
	}

	private Object evaluateArrayAccessExpression(final ArrayAccessExpression expr) {	
		Map<Object, Object> arrayToAccess = (Map<Object, Object>) evaluate(expr.getArray());
		final List<Expression> indexExprs = Arrays.asList(expr.getIndices());
		final Iterator<Expression> it = indexExprs.iterator();
		
		final List<Integer> indexList = new ArrayList<>();
		while(it.hasNext()) {
			Expression indexExpr = it.next();
			final int index = Math.toIntExact((long) evaluate(indexExpr));
			assert(arrayToAccess.size() > 0);
			indexList.add(index);
		}
		
		if(arrayToAccess.containsKey(indexList)) {
			if(arrayToAccess.get(indexList) instanceof Map<?, ?>) {
				arrayToAccess = (Map<Object, Object>) arrayToAccess.get(indexList);
			}
		}
		
		return new HashMap<>(arrayToAccess);
		
		/**
		 * Old implementation using java Array, now use java Map.
		 */
//		ArrayList<Object> arrayToAccess = new ArrayList<>();
//		arrayToAccess = (ArrayList<Object>) evaluate(expr.getArray());
//		
//		final List<Expression> indexExprs = Arrays.asList(expr.getIndices());
//		final Iterator<Expression> it = indexExprs.iterator();
//		while(it.hasNext()) {
//			Expression indexExpr = it.next();
//			final int index = Math.toIntExact((long) evaluate(indexExpr));
//			
//			assert(arrayToAccess.size() > 0);
//			arrayToAccess = growArraySize(arrayToAccess, index + 1);
//			
//			if(arrayToAccess.get(index) instanceof ArrayList<?>) {
//				arrayToAccess = (ArrayList<Object>) arrayToAccess.get(index);
//			}
//		}
//		
//		return arrayToAccess;
	}
	
	
	/**
	 * Data strcture of Boogie array has already changed to java Map.
	 * This function is for java Array implementation.
	 * @deprecated
	 * Ex:
	 * 		in param 						-> out param
	 * 		([], 2)							-> [null, null]
	 * 		([3, 4], 5) 					-> [3, 4, null, null, null]
	 * 		([[1]], 2) 						-> [[1], [null]]
	 * 		([[[1, 2], [3,4]], [[5]]], 4) 	-> [[[1, 2], [3,4]], [[5]]], [[null]], [[null]]]
	 * @param array
	 * 		the target array
	 * @param size
	 * @return
	 * 		a new array with the specific size.
	 */
	private ArrayList<Object> growArraySize(final ArrayList<Object> array, final int size) {
		if(array.size() <= 0) {
  			ArrayList<Object> newArray = new ArrayList<>();
  			while(newArray.size() < size) {
				newArray.add(null);
			}
			return newArray;
  		}
		final Object nullElem = generateNullElem(((ArrayList<Object>) array).get(0));
		final ArrayList<Object> tempArray = new ArrayList<>(size);
		tempArray.addAll(array);
		while(tempArray.size() < size) {
			tempArray.add(nullElem);
		}
		return tempArray;
	}
	
	
	/**
	 * Data strcture of Boogie array has already changed to java Map.
	 * This function is for java Array implementation.
	 * @deprecated
	 * A recursive function used in {@link #growArraySize(ArrayList, int)}.
	 * Ex:
	 * 		in param 		-> out param
	 * 		3 				-> null
	 * 		[]				-> [null]
	 * 		[1] 			-> [null]
	 * 		[1, 2] 			-> [null]
	 * 		[[5,7], [3]] 	-> [[null]]
	 * @param array
	 * @return
	 * 		n dimension array(same as input).
	 * 		the innermost element is null. 
	 */
	private Object generateNullElem(final Object array) {
		if(!(array instanceof ArrayList<?>)) {
			return null;
		} else if(((ArrayList<Object>) array).size() <= 0) {
			ArrayList<Object> newArray = new ArrayList<>();
			newArray.add(null);
			return newArray;
		}
		ArrayList<Object> newArray = new ArrayList<>();
		newArray.add(generateNullElem(((ArrayList<Object>) array).get(0)));
		return newArray;
	}

}
