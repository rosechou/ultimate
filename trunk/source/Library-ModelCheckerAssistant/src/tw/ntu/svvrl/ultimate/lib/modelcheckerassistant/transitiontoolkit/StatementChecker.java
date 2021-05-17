package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
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
	
	public StatementChecker(final ExprEvaluator exprEvaluator) {
		mExprEvaluator = exprEvaluator;
	}
	
	/**
	 * Check whether the given statements(from Icfg edge) is enable.
	 * Assignment and Havoc statements should be considered because these statement will
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
				final StatementExecutor stmtExecutor = new StatementExecutor(mExprEvaluator);
				final ProgramState newState = stmtExecutor.execute(stmt);
				final ExprEvaluator newExprEvaluator = new ExprEvaluator(newState.getValuationMap()
						, mExprEvaluator.getFuncInitValuationInfo());
				final StatementChecker newStatementChecker = new StatementChecker(newExprEvaluator);
				return newStatementChecker.checkStatementsEnable(stmts.subList(i+1, stmts.size()));
			} else if(stmt instanceof HavocStatement) {
			}
		}
		return true;
	}
	
	private boolean checkAssumeStatement(final AssumeStatement assumeStmt) {
		return (boolean) mExprEvaluator.evaluate(assumeStmt.getFormula());
	}
	
}
