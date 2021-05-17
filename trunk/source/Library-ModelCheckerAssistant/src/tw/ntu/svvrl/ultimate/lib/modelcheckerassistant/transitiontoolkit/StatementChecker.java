package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;

/**
 * This class check whether the statements is able to execute (enable).
 */
public class StatementChecker {
	private final ExprEvaluator mExprEvaluator;
	
	public StatementChecker(ExprEvaluator exprEvaluator) {
		mExprEvaluator = exprEvaluator;
	}
	
	/**
	 * Check whether the given statements(from Icfg edge) is enable.
	 * Assignment statement should be considered because this statement will
	 * make the origin state move to a new program state. Whether an assignment statement
	 * is enable is equal to asking whether the new state is enable after executing the 
	 * rest statements. (use recursion) 
	 * @param stmts
	 * 		list of statements
	 * @return
	 * 		true if no assume statement is violated
	 */
	public boolean checkStatementsEnable(final List<Statement> stmts) {
		for(int i = 0; i < stmts.size(); i++) {
			final Statement stmt = stmts.get(i);
			if(stmt instanceof AssumeStatement) {
				// if the formula assumed is not hold, then not enable. 
				if(!checkAssumeStatement((AssumeStatement) stmt)) {
					return false;
				}
			} else if(stmt instanceof AssertStatement) {
				/**
				 * We don't check whether the assertion is satisfied or not here.
				 * Instead, we leave this check in the doTransition function.
				 * So assert statement will be skipped here.
				 */
			} else if(stmt instanceof AssignmentStatement) {
				final ProgramState newState = processAssignmentStatement((AssignmentStatement) stmt);
				final ExprEvaluator newExprEvaluator = new ExprEvaluator(newState.getValuationMap()
						, mExprEvaluator.getFuncInitValuationInfo());
				final StatementChecker newStatementChecker = new StatementChecker(newExprEvaluator);
				return newStatementChecker.checkStatementsEnable(stmts.subList(i+1, stmts.size()));
			}
		}
		return true;
	}
	
	private boolean checkAssumeStatement(AssumeStatement assumeStmt) {
		return (boolean) mExprEvaluator.evaluate(assumeStmt.getFormula());
	}
	

	/**
	 * AssignmentStatement will change the valuation and move to a new state.
	 * @param assignmentStmt
	 * @return
	 * 		new program state.
	 */
	private ProgramState processAssignmentStatement(final AssignmentStatement assignmentStmt) {
		LeftHandSide[] lhs = assignmentStmt.getLhs();
		Expression[] rhs = assignmentStmt.getRhs();
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
