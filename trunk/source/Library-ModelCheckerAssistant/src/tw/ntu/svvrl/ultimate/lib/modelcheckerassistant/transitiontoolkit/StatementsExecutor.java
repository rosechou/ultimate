package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WhileStatement;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.Valuation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
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
	private final List<Statement> mStatements;
	private final Statement mStatement;
	private ProgramState mCurrentProgramState;
	
	/**
	 * For many statements.
	 * @param statements
	 * 		List of statements
	 * @param programState
	 */
	public StatementsExecutor(final List<Statement> statements, final ProgramState programState) {
		mStatements = statements;
		mStatement = null;
		mCurrentProgramState = new ProgramState(programState);
	}
	
	/**
	 * For only one statement.
	 * @param statement
	 * @param programState
	 */
	public StatementsExecutor(final Statement statement, final ProgramState programState) {
		mStatements = null;
		mStatement = statement;
		mCurrentProgramState = new ProgramState(programState);
	}
	
	/**
	 * Execute one or many statements
	 * @return
	 * 		the new state reached after executing
	 */
	public ProgramState execute() {
		if(mStatements != null) {
			for(final Statement stmt : mStatements) {
				executeOne(stmt);
			}
		} else {
			executeOne(mStatement);
		}
		return mCurrentProgramState;
	}
	
	public void executeOne(final Statement stmt) {
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
		final ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		
		final LeftHandSide[] lhs = stmt.getLhs();
		final Expression[] rhs = stmt.getRhs();
		assert(lhs.length == rhs.length);
		
		/**
		 * Handle multi-assignment
		 * For example
		 * int a, b, c := 1, 2, 3;
		 */
		for(int i = 0; i < lhs.length; i++) {
			if(lhs[i] instanceof VariableLHS) {
				final String procName = ((VariableLHS)lhs[i]).getDeclarationInformation().getProcedure();
				final String varName = ((VariableLHS)lhs[i]).getIdentifier();
				final Object value = exprEvaluator.evaluate(rhs[i]);
				
				updateProgramState(procName, varName, value);
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
		/**
		 * Suppose this statement would not occur here because
		 * this statement is handled during the
		 * preprocessing of Boogie and the building of RCFG.
		 * The body of atomic statement will form a {@link StatementSequence}
		 * and thus this issue is solved.
		 * So we just follow the transition of the resulting RCFG.
		 */
		throw new UnsupportedOperationException(stmt.getClass().getSimpleName()
				+ "should not appear.");
	}

	private void executeBreakStatement(BreakStatement stmt) {
		/**
		 * Suppose this statement would not occur here because
		 * this statement is translated to {@link GotoEdge} during the
		 * preprocessing of Boogie and the building of RCFG.
		 */
		throw new UnsupportedOperationException(stmt.getClass().getSimpleName()
				+ "should not appear.");
	}

	private void executeCallStatement(CallStatement stmt) {
		/**
		 * {@link CallStatement#isForall} is not yet implemented.
		 */
		String procName = stmt.getMethodName();
		Expression[] args = stmt.getArguments();
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		
		List<String> argsName = mCurrentProgramState.getProc2InParams().get(procName);
		assert(args.length == argsName.size());
		for(int i = 0; i < args.length; i++) {
			updateProgramState(procName, argsName.get(i), exprEvaluator.evaluate(args[i]));
		}
		
	}

	private void executeForkStatement(ForkStatement stmt) {
	}

	private void executeGotoStatement(GotoStatement stmt) {
		/**
		 * Suppose this statement would not occur here because
		 * this statement is translated to {@link GotoEdge} during the
		 * preprocessing of Boogie and the building of RCFG.
		 */
		throw new UnsupportedOperationException(stmt.getClass().getSimpleName()
				+ "should not appear.");
	}

	private void executeHavocStatement(HavocStatement stmt) {
		VariableLHS[] lhs = stmt.getIdentifiers();
		for(int i = 0; i < lhs.length; i++) {
			final String procName = lhs[i].getDeclarationInformation().getProcedure();
			final String varName = lhs[i].getIdentifier();
			IBoogieType bt = lhs[i].getType();
			if (bt instanceof BoogiePrimitiveType) {
				final Random r = new Random();
				Object value;
				switch(((BoogiePrimitiveType) bt).getTypeCode()) {
					case BoogiePrimitiveType.BOOL:
						value = r.nextBoolean();
						updateProgramState(procName, varName, value);
					case BoogiePrimitiveType.INT:
						value = r.nextInt();
						updateProgramState(procName, varName, value);
					case BoogiePrimitiveType.REAL:
						throw new NotImplementedException("Boogie variable with type"
								+ ((BoogiePrimitiveType) bt).toString() + " is not yet implemented.");
					case BoogiePrimitiveType.ERROR:
					default:
						throw new UnsupportedOperationException("Boogie variable with"
								+ " error or unknown type.");
				}
				
			} else {
				throw new UnsupportedOperationException("Unsupported"
						+ "BoogieType:" + bt.toString() + "in havoc statement");
			}
		}
	}

	private void executeIfStatement(IfStatement stmt) {
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		if((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			StatementsExecutor newStatementsExecutor
					 = new StatementsExecutor(Arrays.asList(stmt.getThenPart()), mCurrentProgramState);
			ProgramState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		} else {
			StatementsExecutor newStatementsExecutor
			 		= new StatementsExecutor(Arrays.asList(stmt.getElsePart()), mCurrentProgramState);
			ProgramState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		}
	}

	private void executeJoinStatement(JoinStatement stmt) {
	}

	private void executeLabel(Label stmt) {
		/**
		 * Do nothing.
		 */
	}

	private void executeReturnStatement(ReturnStatement stmt) {
	}

	private void executeWhileStatement(WhileStatement stmt) {
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		while((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			StatementsExecutor newStatementsExecutor
	 			= new StatementsExecutor(Arrays.asList(stmt.getBody()), mCurrentProgramState);
			ProgramState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		}
	}
	
	private void updateProgramState(final String procName, final String varName, final Object value) {
		Valuation newValuation = mCurrentProgramState.getValuation().shallowCopy();
		assert(newValuation.containsProcOrFunc(procName));
		newValuation.setValue(procName, varName, value);

		mCurrentProgramState = new ProgramState(newValuation, mCurrentProgramState);
	}
	
	
	private void setCurrentState(ProgramState newState) {
		mCurrentProgramState = new ProgramState(newState);
	}
}
