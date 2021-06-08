package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramStateTransition;

public class ThreadStateTransition extends ProgramStateTransition {
	private final IcfgEdge mIcfgEdge;
	private final long mThreadID;
	
	public ThreadStateTransition(IcfgEdge edge, long threadID) {
		mIcfgEdge = edge;
		mThreadID = threadID;
	}
	
	public IcfgEdge getIcfgEdge() {
		return mIcfgEdge;
	}
	
	public long getThreadID() {
		return mThreadID;
	}
}
