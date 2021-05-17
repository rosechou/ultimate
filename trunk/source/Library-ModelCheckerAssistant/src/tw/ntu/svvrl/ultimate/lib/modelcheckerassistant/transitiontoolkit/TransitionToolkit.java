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
	private final CodeBlockExecutor mCodeBlockExecutor;
	
	public TransitionToolkit(final IcfgEdge edge, final Map<String, Map<String, Object>> valuation, 
			final FuncInitValuationInfo funcInitValuationInfo) {
		mEdge = edge;
		ExprEvaluator exprEvaluator = new ExprEvaluator(valuation, funcInitValuationInfo);
		mCodeBlockExecutor = new CodeBlockExecutor(exprEvaluator);
	}
	
	public boolean checkTransEnable() {
		if (mEdge instanceof CodeBlock) {
			return mCodeBlockExecutor.checkEnable(mEdge);
		} else if (mEdge instanceof RootEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
					+ " should not appear in the function getEnableTrans()");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
	}
	
	public ProgramState doTransition() {
		if (mEdge instanceof CodeBlock) {
			return mCodeBlockExecutor.execute(mEdge);
		} else if (mEdge instanceof RootEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
					+ " should not appear in the function getEnableTrans()");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
	}
}
