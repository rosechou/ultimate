package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WhileStatement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AtomicStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BreakStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.GotoStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IfStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.JoinStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Label;
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

	private ProgramState executeWhileStatement(WhileStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeReturnStatement(ReturnStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeLabel(Label stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeJoinStatement(JoinStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeIfStatement(IfStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeHavocStatement(HavocStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeGotoStatement(GotoStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeForkStatement(ForkStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeCallStatement(CallStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeBreakStatement(BreakStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeAtomicStatement(AtomicStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeAssumeStatement(AssumeStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeAssignmentStatement(AssignmentStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeAssertStatement(AssertStatement stmt) {
		// TODO Auto-generated method stub
		return null;
	}
}
