package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.uni_freiburg.informatik.ultimate.automata.alternating.BooleanExpression;
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
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.UnaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WildcardExpression;

public class ExprEvaluator {
	private final Map<String, Map<String, Object>> mValuation = new HashMap<>();
	
	public ExprEvaluator(final Map<String, Map<String, Object>> valuation) {
		mValuation.putAll(valuation);
	}
	
	/**
	 * Look up the valuation table.
	 * @param procName
	 * 		name of procedure
	 * @param identifier
	 * 		name of identifier
	 * @return
	 * 		the value of given identifier
	 */
	private Object lookUpValue(final String procName, final String identifier) {
		Object v = mValuation.get(procName).get(identifier);
		if(v instanceof Integer) {
			return (Integer)v;
		} else if(v instanceof Boolean) {
			return (Boolean)v;
		} else {
			throw new UnsupportedOperationException("Unkown variable type");
		}
	}
	
	public Object evaluate(Expression expr) {
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

	private Object evaluateWildcardExpression(WildcardExpression expr) {
		Random random = new Random();
	    return random.nextBoolean();
	}

	private Object evaluateUnaryExpression(UnaryExpression expr) {
		UnaryExpression.Operator operator = expr.getOperator();
		Object rv =  evaluate(expr.getExpr());
		switch(operator) {
			case LOGICNEG:
				return !(Boolean) rv;
			case ARITHNEGATIVE:
				return -(Integer) rv;
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

	private Object evaluateIntegerLiteral(IntegerLiteral expr) {
		return Integer.valueOf(expr.getValue());
	}

	private Object evaluateIfThenElseExpression(IfThenElseExpression expr) {
		if((boolean) evaluate(expr.getCondition())) {
			return evaluate(expr.getThenPart());
		} else {
			return evaluate(expr.getElsePart());
		}
	}

	private Object evaluateIdentifierExpression(IdentifierExpression expr) {
		
		return null;
	}

	private Object evaluateFunctionApplication(FunctionApplication expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateBooleanLiteral(BooleanLiteral expr) {
		return expr.getValue();
	}

	private Object evaluateBinaryExpression(BinaryExpression expr) {
		BinaryExpression.Operator operator = expr.getOperator();
		Object lv =  evaluate(expr.getLeft());
		Object rv =  evaluate(expr.getRight());
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
				return (Integer) lv < (Integer) rv;
			case COMPGT:
				return (Integer) lv > (Integer) rv;
			case COMPLEQ:
				return (Integer) lv <= (Integer) rv;
			case COMPGEQ:
				return (Integer) lv >= (Integer) rv;
			case COMPEQ:
				if(lv instanceof Boolean && rv instanceof Boolean) {
					return (Boolean) lv == (Boolean) rv;
				} else if(lv instanceof Integer && rv instanceof Integer) {
					return (Integer) lv == (Integer) rv;
				} else {
					throw new UnsupportedOperationException("Binary operation type error.");
				}
			case COMPNEQ:
				if(lv instanceof Boolean && rv instanceof Boolean) {
					return (Boolean) lv != (Boolean) rv;
				} else if(lv instanceof Integer && rv instanceof Integer) {
					return (Integer) lv != (Integer) rv;
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
				return (Integer) lv + (Integer) rv;
			case ARITHMINUS:
				return (Integer) lv - (Integer) rv;
			case ARITHMUL:
				return (Integer) lv * (Integer) rv;
			case ARITHDIV:
				return (Integer) lv / (Integer) rv;
			case ARITHMOD:
				return (Integer) lv % (Integer) rv;
			default:
				throw new UnsupportedOperationException("Unknown Binary Operator: "
						+ operator.getClass().getSimpleName());
		}
	}

	private Object evaluateArrayStoreExpression(ArrayStoreExpression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateArrayAccessExpression(ArrayAccessExpression expr) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
