package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayAccessExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayStoreExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.EnsuresSpecification;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.JoinStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.chandler.BaseMemoryModel;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProcInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.CodeBlockExecutor;

public class ThreadCodeBlockExecutor extends CodeBlockExecutor<ThreadState> {
	static long resBase = 0;
	private final ProgramStateExplorer mProgramStateExplorer;
	/**
	 * For Program Automata.
	 * @param codeBlock
	 * @param state
	 */
	public ThreadCodeBlockExecutor(final CodeBlock codeBlock, final ThreadState state, final ProgramStateExplorer pe) {
		mCodeBlock = codeBlock;
		mCurrentState = state;
		mProgramStateExplorer = pe;
	}
	


	public boolean checkEnabled() {
		if(mCodeBlock instanceof StatementSequence) {
			List<Statement> stmts = ((StatementSequence) mCodeBlock).getStatements();
			final ThreadStatementsChecker statementsChecker = new ThreadStatementsChecker(stmts, mCurrentState, mProgramStateExplorer);
			return statementsChecker.checkStatementsEnable();
		} else if(mCodeBlock instanceof Return) {
			/**
			 * The caller procedure (top-1)
			 * and return destination's procedure should match.
			 */
			String TargetProcName = mCodeBlock.getSucceedingProcedure();
			if(mCurrentState.getCallerProc().getProcName().equals(TargetProcName)) {
				return true;
			} else {
				return false;
			}
		} else if(mCodeBlock instanceof Summary) {
			/**
			 * If there are {@link Call} and {@link Summary}
			 * at the same CFG location, then block the {@link Summary}.
			 * (Force the execution to execute {@link Call} and {@link Return})
			 * later.)
			 */
			IcfgLocation source = mCodeBlock.getSource();
			if(source != null) {
				List<IcfgEdge> otherEdges = mCodeBlock.getSource().getOutgoingEdges();
				if(containsCall(otherEdges)) {
					return false;
				}
			}
			return true;
		} else if(mCodeBlock instanceof ParallelComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof SequentialComposition) {
			final List<CodeBlock> codeBlocks = ((SequentialComposition) mCodeBlock).getCodeBlocks();
			
			/**
			 * Deep copy the current state and do the dummy execution.
			 * 
			 * For instance, the SequentialComposition has two code blocks, they are
			 * (1)	#t~post0 := ~x~0;		and
    		 * (2)	~x~0 := 1 + #t~post0;
			 * 
			 * Code block (1) must be executed or #t~post0 in (2) may be null
			 * and a null pointer exception could be thrown.
			 * 
			 * We don't really want the valuation is modified since this is an enabledness check.
			 * Thus, both global and local variables should be deep copied.
			 */
			ThreadState dummyCurrentState = new ThreadState(mCurrentState, ThreadState.ConstructStrategy.fullCopy);
			for(CodeBlock codeBlock : codeBlocks) {
				ThreadCodeBlockExecutor cbe = new ThreadCodeBlockExecutor(codeBlock, dummyCurrentState, mProgramStateExplorer);
				if(cbe.checkEnabled()) {
					cbe.execute();
					dummyCurrentState = cbe.mCurrentState;
				} else {
					return false;
				}
			}
			return true;
		} else {
			// other edge types are OK.
			return true;
		}
	}
	
	/**
	 * Whether other edges(if exist) contain {@link Call} statement.
	 * It is used for {@link Summary} enable check.
	 * @param otherEdges
	 * @return true if yes, false if no.
	 */
	private boolean containsCall(final List<IcfgEdge> otherEdges) {
		for(final IcfgEdge edge : otherEdges) {
			if(edge instanceof Call) {
				return true;
			}
		}
		return false;
	}

	public ThreadState execute() {
		if(mCodeBlock instanceof StatementSequence) {
			executeStatementSequence((StatementSequence) mCodeBlock);
		} else if(mCodeBlock instanceof Call) {
			executeCall((Call) mCodeBlock);
		} else if(mCodeBlock instanceof Summary) {
			executeSummary((Summary) mCodeBlock);
		} else if(mCodeBlock instanceof Return) {
			executeReturn((Return) mCodeBlock);
		} else if(mCodeBlock instanceof ForkThreadCurrent) {
			executeForkThreadCurrent((ForkThreadCurrent) mCodeBlock);
		} else if(mCodeBlock instanceof ForkThreadOther) {
			executeForkThreadOther((ForkThreadOther) mCodeBlock);
		} else if(mCodeBlock instanceof JoinThreadCurrent) {
			executeJoinThreadCurrent((JoinThreadCurrent) mCodeBlock);
		} else if(mCodeBlock instanceof JoinThreadOther) {
			executeJoinThreadOther((JoinThreadOther) mCodeBlock);
		} else if(mCodeBlock instanceof ParallelComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof SequentialComposition) {
			executeSequentialComposition((SequentialComposition) mCodeBlock);
		} else if(mCodeBlock instanceof GotoEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mCodeBlock.getClass().getSimpleName()
					+ " should not appear in the resulting CFG");
		} else {
			throw new UnsupportedOperationException("Error: " + mCodeBlock.getClass().getSimpleName()
					+ " is not supported.");
		}
		return mCurrentState;
	}

	private void moveToNewState(final ThreadState newState) {
		mCurrentState = newState;
	}

	private void executeStatementSequence(final StatementSequence stmtSeq) {
		final List<Statement> stmts = stmtSeq.getStatements();
		final ThreadStatementsExecutor statementExecutor 
			= new ThreadStatementsExecutor(stmts, mCurrentState, ThreadStatementsExecutor.ExecStrategy.realExec, mProgramStateExplorer);
		moveToNewState(statementExecutor.execute());
	}

	private void executeCall(final Call call) {
		final CallStatement callStmt = call.getCallStatement();
		final ThreadStatementsExecutor statementExecutor 
			= new ThreadStatementsExecutor(callStmt, mCurrentState, ThreadStatementsExecutor.ExecStrategy.realExec, mProgramStateExplorer);
		moveToNewState(statementExecutor.execute());
	}

	private void executeSummary(final Summary summary) {
		/**
		 * If called Procedure Has Implementation,
		 * Force it to execute {@link CallStatement}
		 * and never reach here.
		 */
		if(summary.calledProcedureHasImplementation()) {
			throw new UnsupportedOperationException("Error: Summary with"
					+ " implementation is not yet supported.");
		}
		
		doMemoryAssignments(summary);
	}

	private void doMemoryAssignments(final Summary summary) {
		/**
		 * These four procedures are considered for the purpose of
		 * the handling of array memory model. In Ultimate, the array representation
		 * is in the symbolic way, i.e. using requires, ensures and modifies specifications
		 * as memory address constraints. I implements the assignments from the ensures spec
		 * in procedure read~int, write~int and write~init~int.
		 * Note that this approach treats the symptoms but not the root cause.
		 * It is better to modify the Boogie generator directly. However, it might be time-consuming.
		 * Also note that the operations related to Boogie map #valid and #length are ignored.
		 * 
		 * In the following, see the Boogie program which involves array access and store.
		 */
		final CallStatement callStmt = summary.getCallStatement();
		final String readInt = "read~int";
		final String writeInt = "write~int";
		final String writeInitInt = "write~init~int";
		final String allocOnStack = "#Ultimate.allocOnStack";
		
		/**
		 * Do actual call for these four procedures.
		 */
		if(callStmt.getMethodName().equals(readInt) || callStmt.getMethodName().equals(writeInt)
			|| callStmt.getMethodName().equals(writeInitInt) || callStmt.getMethodName().equals(allocOnStack)) {
			final ThreadStatementsExecutor statementExecutor 
				= new ThreadStatementsExecutor(callStmt, mCurrentState, ThreadStatementsExecutor.ExecStrategy.realExec, mProgramStateExplorer);
			moveToNewState(statementExecutor.execute());
		}
		
		switch(callStmt.getMethodName()) {
		case readInt:
			/**
			 * procedure read~int(...)
			 * ensures #value == #memory_int[#ptr.base,#ptr.offset];
			 * modifies ;
			 * 
			 * Assign value of #memory_int[#ptr.base,#ptr.offset] to #value
			 */
			doAssignmentFromEnsures(readInt, callStmt);
			break;
		case writeInt:
			/**
			 * procedure write~int(...)
			 * ensures #memory_int == old(#memory_int)[#ptr.base,#ptr.offset := #value];
			 * modifies #memory_int;
			 * 
			 * #memory_int can be modifies, so
			 * assign value of old(#memory_int)[#ptr.base,#ptr.offset := #value] to #memory_int
			 */
			doAssignmentFromEnsures(writeInt, callStmt);
			break;
		case writeInitInt:
			/**
			 * procedure write~init~int(...)
			 * ensures #memory_int[#ptr.base,#ptr.offset] == #value;
			 * modifies ;
			 * 
			 * Assign value of #value to #memory_int[#ptr.base,#ptr.offset]
			 */
			doAssignmentFromEnsures(writeInitInt, callStmt);
			break;
		case allocOnStack:
			
			List<String> outParams = mProgramStateExplorer.getProc2OutParams().get(allocOnStack);
			if(!outParams.get(0).equals("#res.base") || !outParams.get(1).equals("#res.offset")) {
				throw new UnsupportedOperationException("Unexpected behavior in procedure "
						+ allocOnStack);
			}
			final ThreadStatementsExecutor statementExecutor 
				= new ThreadStatementsExecutor(callStmt, mCurrentState, ThreadStatementsExecutor.ExecStrategy.realExec, mProgramStateExplorer);
			/**
			 * procedure #Ultimate.allocOnStack(...)
			 * ensures 0 != #res.base;
			 * ensures #StackHeapBarrier < #res.base;
			 * 
			 * Once #Ultimate.allocOnStack is called, #res.base increases by 1. 
			 * The value of #StackHeapBarrier is always ignored.
			 */
			statementExecutor.updateThreadState(allocOnStack, "#res.base", resBase);
			resBase++;
			/**
			 * procedure #Ultimate.allocOnStack(...)
			 * ensures 0 == #res.offset
			 * 
			 * #res.offset is always 0.
			 */
			statementExecutor.updateThreadState(allocOnStack, "#res.offset", (long) 0);
			moveToNewState(statementExecutor.getCurrentState());
		default:
			/**
			 * do nothing.
			 */
		}
		
		/**
		 * Do actual return for these four procedures.
		 */
		if(callStmt.getMethodName().equals(readInt) || callStmt.getMethodName().equals(writeInt)
			|| callStmt.getMethodName().equals(writeInitInt) || callStmt.getMethodName().equals(allocOnStack)) {
			final ProcInfo returnDestinationProc = mCurrentState.getCallerProc();
			moveToNewState(doReturnRoutines(mCurrentState, returnDestinationProc
					, callStmt.getLhs()));
		}
	}



	private void doAssignmentFromEnsures(final String procName, final CallStatement callStmt) {
		final EnsuresSpecification ensures = mProgramStateExplorer.getProc2Ensures().get(procName).get(0);
		final Expression ensuresFormula = ensures.getFormula();
		final Set<String> modifiedVars = mProgramStateExplorer.getProc2ModifiedVars().get(procName);
		final ThreadStatementsExecutor statementExecutor 
			= new ThreadStatementsExecutor(callStmt, mCurrentState, ThreadStatementsExecutor.ExecStrategy.realExec, mProgramStateExplorer);
		
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState, mProgramStateExplorer);
		if(ensuresFormula instanceof BinaryExpression) {
			final Expression left = ((BinaryExpression) ensuresFormula).getLeft();
			final Expression right = ((BinaryExpression) ensuresFormula).getRight();
			if(exprEvaluator.evaluate(left) == null) {
				/**
				 * For read~int(...) and write~init~int(...)
				 */
				/**
				 * 						left				  == right
				 * ensures #memory_int[#ptr.base,#ptr.offset] == #value;
				 */
				/**
				 * 			left  == 				right
				 * ensures #value == #memory_int[#ptr.base,#ptr.offset];
				 */
				assert left instanceof IdentifierExpression || left instanceof ArrayAccessExpression;
				if(left instanceof IdentifierExpression) {
					statementExecutor.updateThreadState(
							((IdentifierExpression) left).getDeclarationInformation().getProcedure(),
							((IdentifierExpression) left).getIdentifier(),
							exprEvaluator.evaluate(right));
					moveToNewState(statementExecutor.getCurrentState());
				} else if(left instanceof ArrayAccessExpression) {
					final ArrayStoreExpression arrayStoreExpr = new ArrayStoreExpression(left.getLoc(), ((ArrayAccessExpression) left).getArray()
							, ((ArrayAccessExpression) left).getIndices(), right);
					exprEvaluator.evaluate(arrayStoreExpr);
					moveToNewState(statementExecutor.getCurrentState());
				}
			}else if(exprEvaluator.evaluate(right) == null) {
				/**
				 * left and right exchange
				 */
				assert right instanceof IdentifierExpression || right instanceof ArrayAccessExpression;
				if(right instanceof IdentifierExpression) {
					statementExecutor.updateThreadState(
							((IdentifierExpression) right).getDeclarationInformation().getProcedure(),
							((IdentifierExpression) right).getIdentifier(),
							exprEvaluator.evaluate(left));
					moveToNewState(statementExecutor.getCurrentState());
				} else if(right instanceof ArrayAccessExpression) {
					final ArrayStoreExpression arrayStoreExpr = new ArrayStoreExpression(right.getLoc(), ((ArrayAccessExpression) right).getArray()
							, ((ArrayAccessExpression) right).getIndices(), left);
					exprEvaluator.evaluate(arrayStoreExpr);
					moveToNewState(statementExecutor.getCurrentState());
				}
			}else {
				/**
				 * for write~int(...)
				 */
				if(left instanceof IdentifierExpression) {
					final String identifier = ((IdentifierExpression) left).getIdentifier();
					if(modifiedVars.contains(identifier)) {
						statementExecutor.updateThreadState(
							((IdentifierExpression) left).getDeclarationInformation().getProcedure(),
							identifier, exprEvaluator.evaluate(right));
						moveToNewState(statementExecutor.getCurrentState());
					} else {
						throw new UnsupportedOperationException("Unexpected behavior in procedure "
							+ procName);
					}
				}else if(right instanceof IdentifierExpression) {
					final String identifier = ((IdentifierExpression) right).getIdentifier();
					if(modifiedVars.contains(identifier)) {
						statementExecutor.updateThreadState(
							((IdentifierExpression) right).getDeclarationInformation().getProcedure(),
							identifier, exprEvaluator.evaluate(left));
						moveToNewState(statementExecutor.getCurrentState());
					} else {
						throw new UnsupportedOperationException("Unexpected behavior in procedure "
								+ procName);
					}
				}else {
					throw new UnsupportedOperationException("Unexpected behavior in procedure "
						+ procName);
				}
			}
		}else {
			throw new UnsupportedOperationException("Unsupported ensures formula "
				+ "type: " + ensuresFormula.getClass().getSimpleName());
		}
		moveToNewState(statementExecutor.getCurrentState());
	}



	private void executeReturn(final Return returnn) {
		final CallStatement correspondingCallStmt = returnn.getCallStatement();
		final ProcInfo returnDestinationProc = mCurrentState.getCallerProc();
		
		moveToNewState(doReturnRoutines(mCurrentState, returnDestinationProc
				, correspondingCallStmt.getLhs()));
	}

	private void executeForkThreadCurrent(final ForkThreadCurrent forkThreadCurrent) {
		throw new UnsupportedOperationException(ForkThreadCurrent.class.getSimpleName()
				+ "is handled at other place.");
	}

	private void executeForkThreadOther(final ForkThreadOther forkThreadOther) {
		/**
		 * Cannot produce this case.
		 */
		throw new NotImplementedException(JoinThreadOther.class.getSimpleName()
				+ "is not yet implemented.");
	}

	private void executeJoinThreadCurrent(final JoinThreadCurrent joinThreadCurrent) {
		throw new UnsupportedOperationException(JoinThreadCurrent.class.getSimpleName()
				+ "is handled at other place.");
	}

	private void executeJoinThreadOther(final JoinThreadOther joinThreadOther) {
		/**
		 * Cannot produce this case.
		 */
		throw new NotImplementedException(JoinThreadOther.class.getSimpleName()
				+ "is not yet implemented.");
	}
	
	private void executeSequentialComposition(SequentialComposition sequentialComposition) {
		final List<CodeBlock> codeBlocks = sequentialComposition.getCodeBlocks();
		for(CodeBlock codeBlock : codeBlocks) {
			ThreadCodeBlockExecutor cbe = new ThreadCodeBlockExecutor(codeBlock, mCurrentState, mProgramStateExplorer);
			cbe.execute();
			moveToNewState(cbe.mCurrentState);
		}
	}

	public ThreadState doReturnRoutines(final ThreadState fromState, final ProcInfo toInfo
			, final VariableLHS[] stmtLhss) {

		final ProcInfo fromProc = fromState.getCurrentProc();
		final String fromProcName = fromProc.getProcName();
		
		/**
		 * Retrieve return values
		 */
		final List<String> outParamNames = mProgramStateExplorer.getProc2OutParams().get(fromProcName);
		final VariableLHS[] lhss = stmtLhss;
		final Object[] values = new Object[lhss.length];
		assert(lhss.length == outParamNames.size());
		for(int i = 0; i < lhss.length; i++) {
			final Object v = fromState.getValuationLocalCopy().lookUpValue(fromProcName, outParamNames.get(i));
			values[i] = v;
		}
		
		/**
		 * Reset the return procedure valuation.
		 */
		fromState.resetLocalValuation(toInfo.getValuationRecord());
		
		final ThreadStatementsExecutor statementExecutor
		= new ThreadStatementsExecutor(fromState, ThreadStatementsExecutor.ExecStrategy.realExec);
		
		/**
		 * assign return value(s) to lhs(s).
		 */
		for(int i = 0; i < lhss.length; i++) {
			final String lhsName = lhss[i].getIdentifier();
			statementExecutor.updateThreadState(
					lhss[i].getDeclarationInformation().getProcedure(), lhsName, values[i]);
		}
		
		/**
		 * This will lead to a bug in the context of recursive procedure call.
		 * Move it to {@link CallStatement}.
		 */
//		/**
//		 * set all <code>currentProcName</code>'s local variables to null.
//		 */
//		final Map<String, Object> id2v = mCurrentState.getValuation().getProcOrFuncId2V(currentProcName);
//		for(final String varName : id2v.keySet()) {
//			statementExecutor.updateThreadState(currentProcName, varName, null);
//		}
		
		statementExecutor.getCurrentState().popProc();
		return statementExecutor.getCurrentState();
	}



	public boolean checkAccessOnlyLocalVar() {
		if(mCodeBlock instanceof StatementSequence) {
			final List<Statement> stmts = ((StatementSequence) mCodeBlock).getStatements();
			final ThreadStatementsChecker statementsChecker = new ThreadStatementsChecker(stmts, mCurrentState, mProgramStateExplorer);
			return statementsChecker.checkStatementsAccessOnlyLocalVar();
		} else if(mCodeBlock instanceof Call) {
			final List<Statement> stmts = new ArrayList<>();
			final CallStatement callStmt = ((Call) mCodeBlock).getCallStatement();
			stmts.add(callStmt);
			final ThreadStatementsChecker statementsChecker = new ThreadStatementsChecker(stmts, mCurrentState, mProgramStateExplorer);
			return statementsChecker.checkCallAccessOnlyLocalVar();
		} else if(mCodeBlock instanceof Summary) {
			return true;
		} else if(mCodeBlock instanceof Return) {
			final List<Statement> stmts = new ArrayList<>();
			final CallStatement callStmt = ((Return) mCodeBlock).getCorrespondingCall().getCallStatement();
			stmts.add(callStmt);
			final ThreadStatementsChecker statementsChecker = new ThreadStatementsChecker(stmts, mCurrentState, mProgramStateExplorer);
			return statementsChecker.checkReturnAccessOnlyLocalVar();
		} else if(mCodeBlock instanceof ForkThreadCurrent) {
			final List<Statement> stmts = new ArrayList<>();
			final ForkStatement forkStmt = ((ForkThreadCurrent) mCodeBlock).getForkStatement();
			stmts.add(forkStmt);
			final ThreadStatementsChecker statementsChecker = new ThreadStatementsChecker(stmts, mCurrentState, mProgramStateExplorer);
			return statementsChecker.checkForkAccessOnlyLocalVar();
		} else if(mCodeBlock instanceof ForkThreadOther) {
			throw new NotImplementedException(ForkThreadOther.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof JoinThreadCurrent) {
			final List<Statement> stmts = new ArrayList<>();
			final JoinStatement joinStmt = ((JoinThreadCurrent) mCodeBlock).getJoinStatement();
			stmts.add(joinStmt);
			final ThreadStatementsChecker statementsChecker = new ThreadStatementsChecker(stmts, mCurrentState, mProgramStateExplorer);
			return statementsChecker.checkJoinAccessOnlyLocalVar();
		} else if(mCodeBlock instanceof JoinThreadOther) {
			throw new NotImplementedException(JoinThreadOther.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof ParallelComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof SequentialComposition) {
			final List<CodeBlock> codeBlocks = ((SequentialComposition) mCodeBlock).getCodeBlocks();
			
			boolean accessOnlyLocalVar = true;
			for(CodeBlock codeBlock : codeBlocks) {
				ThreadCodeBlockExecutor cbe = new ThreadCodeBlockExecutor(codeBlock, mCurrentState, mProgramStateExplorer);
				if(!cbe.checkAccessOnlyLocalVar()) {
					accessOnlyLocalVar = false;
				}
			}
			return accessOnlyLocalVar;
		} else {
			// other edge types are OK.
			return true;
		}
	}
	
}
