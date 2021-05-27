package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;

public class ThreadStateTransition {
	private final IcfgEdge mIcfgEdge;
	private final int mThreadID;
	
	public ThreadStateTransition(IcfgEdge edge, int threadID) {
		mIcfgEdge = edge;
		mThreadID = threadID;
	}
	
	public IcfgEdge getIcfgEdge() {
		return mIcfgEdge;
	}
	
	public int getThreadID() {
		return mThreadID;
	}
}
