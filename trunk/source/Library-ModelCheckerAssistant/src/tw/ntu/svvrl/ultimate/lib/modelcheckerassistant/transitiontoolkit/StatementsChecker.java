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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

/**
 * This class check whether the statements is able to execute (enable).
 */
public abstract class StatementsChecker<S extends ValuationState<S>> {
	protected List<Statement> mStatements;
	/**
	 * the state may change due to assignment and havoc statement.
	 */
	protected S mState;
	
	protected abstract boolean checkStatementsEnable();
	protected abstract boolean checkAssumeStatement(final AssumeStatement assumeStmt);
}
