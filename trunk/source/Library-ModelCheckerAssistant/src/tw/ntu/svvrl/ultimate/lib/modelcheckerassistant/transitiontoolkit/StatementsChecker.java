package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;

/**
 * This class check whether the statements is able to execute (enable).
 */
public abstract class StatementsChecker<S extends ValuationState<S>> {
	protected List<Statement> mStatements;
	/**
	 * the state may change due to assignment and havoc statement.
	 */
	protected S mState;
	
	protected StatementsChecker(final List<Statement> statements) {
		mStatements = statements;
	}
	
	protected abstract boolean checkStatementsEnable();
	protected abstract boolean checkAssumeStatement(final AssumeStatement assumeStmt);
}
