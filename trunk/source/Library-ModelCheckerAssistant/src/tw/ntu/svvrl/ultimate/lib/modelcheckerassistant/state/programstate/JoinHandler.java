package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit.ThreadStatementsExecutor;

public class JoinHandler {
	final ProgramState mProgramState;
	final ThreadStateTransition mTrans;
	
	public JoinHandler(final ProgramState programState, final ThreadStateTransition trans) {
		mProgramState = new ProgramState(programState);
		mTrans = trans;
	}
	

	public ProgramState doJoin() {
		
		
		return null;
	}
}

