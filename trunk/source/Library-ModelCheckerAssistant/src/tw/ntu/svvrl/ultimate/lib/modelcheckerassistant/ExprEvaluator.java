package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant;

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
	public ExprEvaluator() {
	}
	
	public Object evaluate(Expression expr) {
		if(expr instanceof ArrayAccessExpression) {
			return evaluateArrayAccessExpression((ArrayAccessExpression) expr);
		}else if(expr instanceof ArrayStoreExpression) {
			return evaluateArrayStoreExpression((ArrayStoreExpression) expr);
		}else if(expr instanceof BinaryExpression) {
			return evaluateBinaryExpression((BinaryExpression) expr);
		}else if(expr instanceof BitvecLiteral) {
			return evaluateBitvecLiteral((BitvecLiteral) expr);
		}else if(expr instanceof BitVectorAccessExpression) {
			return evaluateBitVectorAccessExpression((BitVectorAccessExpression) expr);
		}else if(expr instanceof BooleanLiteral) {
			return evaluateBooleanLiteral((BooleanLiteral) expr);
		}else if(expr instanceof FunctionApplication) {
			return evaluateFunctionApplication((FunctionApplication) expr);
		}else if(expr instanceof IdentifierExpression) {
			return evaluateIdentifierExpression((IdentifierExpression) expr);
		}else if(expr instanceof IfThenElseExpression) {
			return evaluateIfThenElseExpression((IfThenElseExpression) expr);
		}else if(expr instanceof IntegerLiteral) {
			return evaluateIntegerLiteral((IntegerLiteral) expr);
		}else if(expr instanceof QuantifierExpression) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		}else if(expr instanceof RealLiteral) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		}else if(expr instanceof StringLiteral) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		}else if(expr instanceof StructAccessExpression) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		}else if(expr instanceof StructConstructor) {
			throw new UnsupportedOperationException("Unsupported Expression type: "
					+ expr.getClass().getSimpleName());
		}else if(expr instanceof UnaryExpression) {
			return evaluateUnaryExpression((UnaryExpression) expr);
		}else if(expr instanceof WildcardExpression) {
			return evaluateWildcardExpression((WildcardExpression) expr);
		}else {
			throw new UnsupportedOperationException("Unknown Expression type: "
					+ expr.getClass().getSimpleName());
		}
	}

	private Object evaluateWildcardExpression(WildcardExpression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateUnaryExpression(UnaryExpression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateIntegerLiteral(IntegerLiteral expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateIfThenElseExpression(IfThenElseExpression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateIdentifierExpression(IdentifierExpression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateFunctionApplication(FunctionApplication expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateBooleanLiteral(BooleanLiteral expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateBitVectorAccessExpression(BitVectorAccessExpression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateBitvecLiteral(BitvecLiteral expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object evaluateBinaryExpression(BinaryExpression expr) {
		// TODO Auto-generated method stub
		return null;
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
