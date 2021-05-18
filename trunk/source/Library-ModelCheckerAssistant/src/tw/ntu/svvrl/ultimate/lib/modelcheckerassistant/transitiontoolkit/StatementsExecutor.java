package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WhileStatement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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

public class StatementsExecutor {
	private ProgramState mCurrentProgramState;

	public StatementsExecutor(final ProgramState programState) {
		mCurrentProgramState = programState;
	}
	
	public void execute(final List<Statement> stmts) {
		for(Statement stmt : stmts) {
			execute(stmt);
		}
	}
	
	public void execute(final Statement stmt) {
		if(stmt instanceof AssertStatement) {
			executeAssertStatement((AssertStatement) stmt);
		} else if(stmt instanceof AssignmentStatement) {
			executeAssignmentStatement((AssignmentStatement) stmt);
		} else if(stmt instanceof AssumeStatement) {
			executeAssumeStatement((AssumeStatement) stmt);
		} else if(stmt instanceof AtomicStatement) {
			executeAtomicStatement((AtomicStatement) stmt);
		} else if(stmt instanceof BreakStatement) {
			executeBreakStatement((BreakStatement) stmt);
		} else if(stmt instanceof CallStatement) {
			executeCallStatement((CallStatement) stmt);
		} else if(stmt instanceof ForkStatement) {
			executeForkStatement((ForkStatement) stmt);
		} else if(stmt instanceof GotoStatement) {
			executeGotoStatement((GotoStatement) stmt);
		} else if(stmt instanceof HavocStatement) {
			executeHavocStatement((HavocStatement) stmt);
		} else if(stmt instanceof IfStatement) {
			executeIfStatement((IfStatement) stmt);
		} else if(stmt instanceof JoinStatement) {
			executeJoinStatement((JoinStatement) stmt);
		} else if(stmt instanceof Label) {
			executeLabel((Label) stmt);
		} else if(stmt instanceof ReturnStatement) {
			executeReturnStatement((ReturnStatement) stmt);
		} else if(stmt instanceof WhileStatement) {
			executeWhileStatement((WhileStatement) stmt);
		} else {
			throw new UnsupportedOperationException("Error: " + stmt.getClass().getSimpleName()
					+ " is not supported.");
		}
	}
	

	private void updateValuation(final String procName, final String varName, final Object value) {
		Map<String, Map<String, Object>> newValuation = new HashMap<>();
		newValuation.putAll(mCurrentProgramState.getValuationMap());
		assert(newValuation.containsKey(procName));
		
		Map<String, Object> id2v = newValuation.get(procName);
		if(id2v.containsKey(varName)) {
			id2v.replace(varName, value);
		} else {
			id2v.put(varName, value);
		}
		mCurrentProgramState = new ProgramState(newValuation, mCurrentProgramState.getFuncInitValuationInfo());
	}
	
	
	public ProgramState getCurrentState() {
		return mCurrentProgramState;
	}

	private void executeAssertStatement(AssertStatement stmt) {
		// TODO Auto-generated method stub
	}

	/**
	 * AssignmentStatement will change the valuation and move to a new state.
	 * @param stmt
	 * 		an assignment statement.
	 * @return
	 * 		new program state.
	 */
	private void executeAssignmentStatement(final AssignmentStatement stmt) {
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		
		LeftHandSide[] lhs = stmt.getLhs();
		Expression[] rhs = stmt.getRhs();
		assert(lhs.length == rhs.length);
		
		/**
		 * Handle multi-assignment
		 * For example
		 * int a, b, c := 1, 2, 3;
		 */
		for(int i = 0; i < lhs.length; i++) {
			if(lhs[i] instanceof VariableLHS) {
				final String procName = ((VariableLHS)lhs[i]).getDeclarationInformation().getProcedure();
				final String identifier = ((VariableLHS)lhs[i]).getIdentifier();
				final Object value = exprEvaluator.evaluate(rhs[i]);
				
				updateValuation(procName, identifier, value);
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
	}


	private void executeAssumeStatement(AssumeStatement stmt) {
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		assert((boolean) exprEvaluator.evaluate(stmt.getFormula()) == true);
	}

	private void executeAtomicStatement(AtomicStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeBreakStatement(BreakStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeCallStatement(CallStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeForkStatement(ForkStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeGotoStatement(GotoStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeHavocStatement(HavocStatement stmt) {
		VariableLHS[] lhs = stmt.getIdentifiers();
		
	}

	private void executeIfStatement(IfStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeJoinStatement(JoinStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeLabel(Label stmt) {
		// TODO Auto-generated method stub
	}

	private void executeReturnStatement(ReturnStatement stmt) {
		// TODO Auto-generated method stub
	}

	private void executeWhileStatement(WhileStatement stmt) {
		// TODO Auto-generated method stub
	}
}
