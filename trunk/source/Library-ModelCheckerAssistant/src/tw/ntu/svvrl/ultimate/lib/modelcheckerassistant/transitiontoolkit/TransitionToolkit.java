package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;

/**
 * This class handle all issues about a transition(edge) and the statements on it.
 *
 */
public class TransitionToolkit<T, S> {
	private static enum AutTypes{
		Program, NeverClaim
	}
	
	private final T mTrans;
	private CodeBlockExecutor<S> mCodeBlockExecutor = null;
	private AutTypes mAutType;
	
	public TransitionToolkit(final T trans, final S state) {
		mTrans = trans;
		if(trans instanceof IcfgEdge && state instanceof ProgramState) {
			mAutType = AutTypes.Program;
			if (mTrans instanceof CodeBlock) {
				mCodeBlockExecutor = new CodeBlockExecutor((CodeBlock) mTrans, state);
			}
		} else if(trans instanceof OutgoingInternalTransition<?, ?> && state instanceof NeverState) {
			if(((OutgoingInternalTransition<?, ?>) trans).getLetter() instanceof CodeBlock
					&& ((OutgoingInternalTransition<?, ?>) trans).getSucc() instanceof String) {
				mAutType = AutTypes.NeverClaim;
				mCodeBlockExecutor = new CodeBlockExecutor((CodeBlock) ((OutgoingInternalTransition<?, ?>) trans).getLetter(), state);
			} else {
				throw new UnsupportedOperationException("Unknown Transition Type: " 
						+ trans.getClass().getSimpleName());
			}
		} else {
			throw new UnsupportedOperationException("Unknown Transition Type: " 
					+ trans.getClass().getSimpleName());
		}
	}
	
	public boolean checkTransEnable() {
		if(mCodeBlockExecutor != null) {
			return mCodeBlockExecutor.checkEnable();
		} else {
			throw new UnsupportedOperationException("No CodeBlockExecutor");
		}
	}
	
	/**
	 * Execute the {@link CodeBlock} on the edge.
	 * @return
	 * 		A new state reached after doing this transition(edge).
	 */
	public S doTransition() {
		if(mCodeBlockExecutor != null) {
			S newState = mCodeBlockExecutor.execute();
			if(mAutType == AutTypes.Program) {
				((ProgramState) newState).setCorrespondingIcfgLoc((BoogieIcfgLocation) ((IcfgEdge) mTrans).getTarget());
			}
			return newState;
		} else {
			throw new UnsupportedOperationException("No CodeBlockExecutor");
		}
	}
}
