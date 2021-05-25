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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.State;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;

/**
 * This class handle all issues about a transition(edge) and the statements on it.
 *
 */
public class TransitionToolkit<T, S extends State<S>> {
	public static enum AutTypes{
		Program, NeverClaim
	}
	
	private final T mTrans;
	private CodeBlockExecutor<S> mCodeBlockExecutor = null;
	private final AutTypes mAutType;
	
	public TransitionToolkit(final T trans, final S state) {
		mTrans = trans;
		if(trans instanceof ThreadStateTransition && state instanceof ThreadState) {
			mAutType = AutTypes.Program;
			CodeBlock codeBlock = (CodeBlock) ((ThreadStateTransition) trans).getIcfgEdge();
			if (mTrans instanceof CodeBlock) {
				mCodeBlockExecutor = new CodeBlockExecutor<S>(codeBlock, state, mAutType);
			}
		} else if(trans instanceof OutgoingInternalTransition<?, ?> && state instanceof NeverState) {
			if(((OutgoingInternalTransition<?, ?>) trans).getLetter() instanceof CodeBlock
					&& ((OutgoingInternalTransition<?, ?>) trans).getSucc() instanceof NeverState) {
				mAutType = AutTypes.NeverClaim;
				mCodeBlockExecutor
				= new CodeBlockExecutor<S>((CodeBlock) ((OutgoingInternalTransition<?, ?>) trans).getLetter(), state, mAutType);
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
	 * For NeverClaim Automata, we need to know the current program valuation.
	 * @param correspondingThreadState
	 * 		Current program State which contains the valuation.
	 * @return
	 * 		True if this trans is enable for correspondingThreadState, false if not.
	 */
	public boolean checkTransEnable(ProgramState correspondingProgramState) {
		if(mAutType == AutTypes.NeverClaim) {
			if(mCodeBlockExecutor != null) {
				mCodeBlockExecutor.setCorrespondingProgramState(correspondingProgramState);
				NeverState targetState = (NeverState) ((OutgoingInternalTransition<?, ?>) mTrans).getSucc();
				mCodeBlockExecutor.setTargrtState(targetState);
				return mCodeBlockExecutor.checkEnable();
			} else {
				throw new UnsupportedOperationException("No CodeBlockExecutor");
			}
		} else {
			throw new UnsupportedOperationException("This doTransition function is for NeverState");
		}
	}
	
	/**
	 * Execute the {@link CodeBlock} on the edge.
	 * @return
	 * 		A new Thread state reached after doing this transition(edge).
	 */
	public S doTransition() {
		if(mAutType == AutTypes.Program) {
			if(mCodeBlockExecutor != null) {
				final S newState = mCodeBlockExecutor.execute();
				final BoogieIcfgLocation correspondingLoc 
					= (BoogieIcfgLocation) ((ThreadStateTransition) mTrans).getIcfgEdge().getTarget();
				((ThreadState) newState).setCorrespondingIcfgLoc(correspondingLoc);
				return newState;
			} else {
				throw new UnsupportedOperationException("No CodeBlockExecutor");
			}
		} else {
			throw new UnsupportedOperationException("This doTransition function is for ThreadState");
		}
	}

	/**
	 * For NeverClaim Automata, we need to know the current program valuation.
	 * @param correspondingThreadState
	 * 		Current thread State which contains the valuation.
	 * @return
	 * 		A new Never state reached after doing this transition(edge).
	 */
	public S doTransition(ProgramState correspondingProgramState) {
		if(mAutType == AutTypes.NeverClaim) {
			if(mCodeBlockExecutor != null) {
				mCodeBlockExecutor.setCorrespondingProgramState(correspondingProgramState);
				NeverState targetState = (NeverState) ((OutgoingInternalTransition<?, ?>) mTrans).getSucc();
				mCodeBlockExecutor.setTargrtState(targetState);
				S newState = mCodeBlockExecutor.execute();
				return newState;
			} else {
				throw new UnsupportedOperationException("No CodeBlockExecutor");
			}
		} else {
			throw new UnsupportedOperationException("This doTransition function is for NeverState");
		}
	}
}
