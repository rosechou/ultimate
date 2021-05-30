package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;

public class ThreadStateTransition {
	private final IcfgEdge mIcfgEdge;
	private final long mThreadID;
	
	public ThreadStateTransition(IcfgEdge edge, long mThreadID2) {
		mIcfgEdge = edge;
		mThreadID = mThreadID2;
	}
	
	public IcfgEdge getIcfgEdge() {
		return mIcfgEdge;
	}
	
	public long getThreadID() {
		return mThreadID;
	}
}
