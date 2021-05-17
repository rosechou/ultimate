package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import de.uni_freiburg.informatik.ultimate.blockencoding.converter.ShortcutCodeBlock;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;

/**
 * This class handle all issues about a transition(edge) and the statements on it.
 *
 */
public class TransitionToolkit {
	private final IcfgEdge mEdge;
	private final StatementChecker mStatementChecker;
	private final CodeBlockExecutor mCodeBlockExecutor;
	
	public TransitionToolkit(final IcfgEdge edge, final Map<String, Map<String, Object>> valuation, 
			final FuncInitValuationInfo funcInitValuationInfo) {
		mEdge = edge;
		ExprEvaluator exprEvaluator = new ExprEvaluator(valuation, funcInitValuationInfo);
		mStatementChecker = new StatementChecker(exprEvaluator);
		mCodeBlockExecutor = new CodeBlockExecutor(exprEvaluator);
	}
	
	public boolean checkTransEnable() {
		if (mEdge instanceof CodeBlock) {
			if(mEdge instanceof StatementSequence) {
				return mStatementChecker.checkStatementsEnable(((StatementSequence) mEdge).getStatements());
			} else if(mEdge instanceof ParallelComposition) {
				/**
				 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
				 * This case is not yet implemented because I'm lazy.
				 * (one of the preferences in
				 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
				 */
				throw new NotImplementedException(ParallelComposition.class.getSimpleName()
						+ "is not yet implemented.");
			} else if(mEdge instanceof SequentialComposition) {
				/**
				 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
				 * This case is not yet implemented because I'm lazy.
				 * (one of the preferences in
				 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
				 */
				throw new NotImplementedException(ParallelComposition.class.getSimpleName()
						+ "is not yet implemented.");
			} else {
				// other edge types are OK.
				return true;
			}
		} else if (mEdge instanceof RootEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
					+ " should not appear in the function getEnableTrans()");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
	}
	
	public ProgramState doTransition() {
		ProgramState newState;
		if (mEdge instanceof CodeBlock) {
			if(mEdge instanceof StatementSequence) {
				newState = mCodeBlockExecutor.executeStatementSequence((StatementSequence) mEdge);
			} else if(mEdge instanceof Call) {
				newState = mCodeBlockExecutor.executeCall((Call) mEdge);
			} else if(mEdge instanceof Summary) {
				newState = mCodeBlockExecutor.executeSummary((Summary) mEdge);
			} else if(mEdge instanceof Return) {
				newState = mCodeBlockExecutor.executeReturn((Return) mEdge);
			} else if(mEdge instanceof ForkThreadCurrent) {
				newState = mCodeBlockExecutor.executeForkThreadCurrent((ForkThreadCurrent) mEdge);
			} else if(mEdge instanceof ForkThreadOther) {
				newState = mCodeBlockExecutor.executeForkThreadOther((ForkThreadOther) mEdge);
			} else if(mEdge instanceof JoinThreadCurrent) {
				newState = mCodeBlockExecutor.executeJoinThreadCurrent((JoinThreadCurrent) mEdge);
			} else if(mEdge instanceof JoinThreadOther) {
				newState = mCodeBlockExecutor.executeJoinThreadOther((JoinThreadOther) mEdge);
			} else if(mEdge instanceof ParallelComposition) {
				/**
				 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
				 * This case is not yet implemented because I'm lazy.
				 * (one of the preferences in
				 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
				 */
				throw new NotImplementedException(ParallelComposition.class.getSimpleName()
						+ "is not yet implemented.");
			} else if(mEdge instanceof SequentialComposition) {
				/**
				 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
				 * This case is not yet implemented because I'm lazy.
				 * (one of the preferences in
				 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
				 */
				throw new NotImplementedException(ParallelComposition.class.getSimpleName()
						+ "is not yet implemented.");
			} else if(mEdge instanceof GotoEdge) {
				throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
						+ " should not appear in the resulting CFG");
			} else if(mEdge instanceof ShortcutCodeBlock) {
				throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
						+ " is not supported.");
			} else {
				throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
						+ " is not supported.");
			}
		} else if (mEdge instanceof RootEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
					+ " should not appear in the function getEnableTrans()");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
		return null;
	}
}
