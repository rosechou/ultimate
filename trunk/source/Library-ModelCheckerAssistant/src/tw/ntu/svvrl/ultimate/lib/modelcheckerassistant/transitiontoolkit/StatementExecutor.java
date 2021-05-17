package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WhileStatement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;

import java.util.HashMap;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AtomicStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BreakStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.GotoStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IfStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.JoinStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Label;
import de.uni_freiburg.informatik.ultimate.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ReturnStatement;

public class StatementExecutor {
	private final ExprEvaluator mExprEvaluator;

	public StatementExecutor(final ExprEvaluator exprEvaluator) {
		mExprEvaluator = exprEvaluator;
	}
	
	public ProgramState execute(final Statement stmt) {
		if(stmt instanceof AssertStatement) {
			return executeAssertStatement((AssertStatement) stmt);
		} else if(stmt instanceof AssignmentStatement) {
			return executeAssignmentStatement((AssignmentStatement) stmt);
		} else if(stmt instanceof AssumeStatement) {
			return executeAssumeStatement((AssumeStatement) stmt);
		} else if(stmt instanceof AtomicStatement) {
			return executeAtomicStatement((AtomicStatement) stmt);
		} else if(stmt instanceof BreakStatement) {
			return executeBreakStatement((BreakStatement) stmt);
		} else if(stmt instanceof CallStatement) {
			return executeCallStatement((CallStatement) stmt);
		} else if(stmt instanceof ForkStatement) {
			return executeForkStatement((ForkStatement) stmt);
		} else if(stmt instanceof GotoStatement) {
			return executeGotoStatement((GotoStatement) stmt);
		} else if(stmt instanceof HavocStatement) {
			return executeHavocStatement((HavocStatement) stmt);
		} else if(stmt instanceof IfStatement) {
			return executeIfStatement((IfStatement) stmt);
		} else if(stmt instanceof JoinStatement) {
			return executeJoinStatement((JoinStatement) stmt);
		} else if(stmt instanceof Label) {
			return executeLabel((Label) stmt);
		} else if(stmt instanceof ReturnStatement) {
			return executeReturnStatement((ReturnStatement) stmt);
		} else if(stmt instanceof WhileStatement) {
			return executeWhileStatement((WhileStatement) stmt);
		} else {
			throw new UnsupportedOperationException("Error: " + stmt.getClass().getSimpleName()
					+ " is not supported.");
		}
	}

	private ProgramState executeAssertStatement(AssertStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * AssignmentStatement will change the valuation and move to a new state.
	 * @param stmt
	 * 		an assignment statement.
	 * @return
	 * 		new program state.
	 */
	private ProgramState executeAssignmentStatement(final AssignmentStatement stmt) {
		LeftHandSide[] lhs = stmt.getLhs();
		Expression[] rhs = stmt.getRhs();
		assert(lhs.length == rhs.length);
		
		
		Map<String, Map<String, Object>> newValuation = new HashMap<>();
		newValuation.putAll(mExprEvaluator.getValuationMap());
		/**
		 * Handle multi-assignment
		 * For example
		 * int a, b, c := 1, 2, 3;
		 */
		for(int i = 0; i < lhs.length; i++) {
			if(lhs[i] instanceof VariableLHS) {
				final String procName = ((VariableLHS)lhs[i]).getDeclarationInformation().getProcedure();
				final String identifier = ((VariableLHS)lhs[i]).getIdentifier();
				final Object value = mExprEvaluator.evaluate(rhs[i]);
				newValuation.putAll(generateNewValuation(newValuation, procName, identifier, value));
			} else if(lhs[i] instanceof ArrayLHS) {
				/**
				 * I don't know how to produce these case.
				 * It seems no chance to occur. (?)
				 */
				throw new UnsupportedOperationException(StructLHS.class.getSimpleName() 
						+ "is not yet supported.");
			} else if(lhs[i] instanceof StructLHS) {
				throw new UnsupportedOperationException(StructLHS.class.getSimpleName() 
						+ "is not yet supported.");
			}
		}
		
		return new ProgramState(newValuation, mExprEvaluator.getFuncInitValuationInfo());
	}

	private ProgramState executeAssumeStatement(AssumeStatement stmt) {
		assert((boolean) mExprEvaluator.evaluate(stmt.getFormula()) == true);
		return new ProgramState(mExprEvaluator.getValuationMap(), mExprEvaluator.getFuncInitValuationInfo());
	}

	private ProgramState executeAtomicStatement(AtomicStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeBreakStatement(BreakStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeCallStatement(CallStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeForkStatement(ForkStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeGotoStatement(GotoStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeHavocStatement(HavocStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeIfStatement(IfStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeJoinStatement(JoinStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeLabel(Label stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeReturnStatement(ReturnStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeWhileStatement(WhileStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Generate new valuation table due to the modification or declaration of variable.
	 * We could not just set valuation because this modification leads to a new program state.
	 * @param originValuation
	 * 		origin valuation
	 * @param procName
	 * 		name of procedure
	 * @param identifier
	 * 		name of identifier
	 * @param v
	 * 		the new value of given identifier
	 * @return
	 * 		the new valuation
	 */
	private Map<String, Map<String, Object>> generateNewValuation(final Map<String, Map<String, Object>> originValuation,
		final String procName, final String identifier, final Object v) {
		Map<String, Map<String, Object>> newValuation = new HashMap<>();
		newValuation.putAll(originValuation);
		final Object result = newValuation.get(procName).replace(identifier, v);
		if(result == null) {
			throw new UnsupportedOperationException("No variable found in valuation table. "
					+ "Variable update failed.");
		}
		return newValuation;
	}
}
