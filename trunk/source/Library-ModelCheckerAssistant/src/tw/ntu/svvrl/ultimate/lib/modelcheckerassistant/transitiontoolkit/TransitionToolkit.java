package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.FuncInitValuationInfo;

public class TransitionToolkit {
	private final IcfgEdge mEdge;
	private final StatementChecker mStatementChecker;
	private final StatementExecutor mStatementExecutor;
	
	public TransitionToolkit(final IcfgEdge edge, final Map<String, Map<String, Object>> valuation, 
			final FuncInitValuationInfo funcInitValuationInfo) {
		mEdge = edge;
		ExprEvaluator exprEvaluator = new ExprEvaluator(valuation, funcInitValuationInfo);
		mStatementChecker = new StatementChecker(exprEvaluator);
		mStatementExecutor = new StatementExecutor(exprEvaluator);
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
				// same as above.
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
}
