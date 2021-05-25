package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.ExprEvaluator;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

public class ProgramStatementsExecutor extends StatementsExecutor<ProgramState> {

	/**
	 * For many statements.
	 * @param statements
	 * 		List of statements
	 * @param threadState
	 */
	public ProgramStatementsExecutor(final List<Statement> statements, final ProgramState state) {
		mStatements = statements;
		mStatement = null; 
		mCurrentState = state;
	}
	
	/**
	 * For only one statement.
	 * @param statement
	 * @param threadState
	 */
	public ProgramStatementsExecutor(final Statement statement, final ProgramState state) {
		mStatements = null;
		mStatement = statement;
		mCurrentState = state;
	}
	
	/**
	 * For no statement. ({@link Return} code block)
	 * @param threadState
	 */
	public ProgramStatementsExecutor(final ProgramState state) {
		mStatements = null;
		mStatement = null;
		mCurrentState = state;
	}
	
	
	public void executeOne(final Statement stmt) {
		if(stmt instanceof AssumeStatement) {
			executeAssumeStatement((AssumeStatement) stmt);
		} else {
			throw new UnsupportedOperationException("Suppose the Statement Type: "
					+ stmt.getClass().getSimpleName() + " should not be in Never Claim"
							+ " Automata.");
		}
	}

}
