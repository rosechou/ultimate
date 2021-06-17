package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
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
	
	public boolean accessOnlyLocalVar() {
		if(mIcfgEdge instanceof CodeBlock) {
			return ((CodeBlock) mIcfgEdge).accessOnlyLocalVar();
		} else {
			throw new UnsupportedOperationException("accessOnlyLocalVar() cannot support type "
					+ mIcfgEdge.getClass().getSimpleName());
		}
	}
	
	public long getThreadID() {
		return mThreadID;
	}
	
	@Override
	public String getCStatement() {
		return mIcfgEdge.getPayload().toString();
	}
	
	@Override
	public String toString() {
		return "Thread" + mThreadID + " do \"" + mIcfgEdge.toString() + "\"";
	}
}
