package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
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
	
	public TransitionToolkit(final IcfgEdge edge, final ProgramState programState) {
		mEdge = edge;
		if (mEdge instanceof CodeBlock) {
			mCodeBlockExecutor = new CodeBlockExecutor((CodeBlock) mEdge, programState);
		} else {
			mCodeBlockExecutor = null;
		}
	}
	
	public boolean checkTransEnable() {
		if (mEdge instanceof CodeBlock) {
			return mCodeBlockExecutor.checkEnable();
		} else if (mEdge instanceof RootEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
					+ " should not appear in the function getEnableTrans()");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
	}
	
	/**
	 * Execute the {@link CodeBlock} on the edge.
	 * @return
	 * 		A new state reached after doing this transition(edge).
	 */
	public ProgramState doTransition() {
		if (mEdge instanceof CodeBlock) {
			ProgramState newState = mCodeBlockExecutor.execute();
			newState.setCorrespondingIcfgLoc((BoogieIcfgLocation) mEdge.getTarget());
			return newState;
		} else if (mEdge instanceof RootEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
					+ " should not appear in the function getEnableTrans()");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
	}
}
