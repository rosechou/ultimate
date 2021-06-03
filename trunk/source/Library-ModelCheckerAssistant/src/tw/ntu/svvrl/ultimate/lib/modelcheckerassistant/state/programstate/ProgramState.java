package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;

/**
 * ProgramState Consists of one or many {@link ThreadState}s.
 * The <code> mValuation </code> only keeps global valuation.
 * All {@link ThreadState}s' (in <code> mThreadStates </code>) global valuations 
 * are references to this valuation. 
 * So if one thread changes the global variable, the valuation here also
 * changes.
 */
public class ProgramState extends ValuationState<ProgramState> {
	private final Map<String, BoogieIcfgLocation> mEntryNodes;
	private final Map<String, BoogieIcfgLocation> mExitNodes;
	
	/**
	 * Thread ID to ThreadState
	 * One thread must contain only one thread state. 
	 */
	final private Map<Long, ThreadState> mThreadStates = new HashMap<>();
	
	public ProgramState(ThreadState threadState, Valuation globalValuation,
			final Map<String, BoogieIcfgLocation> entryNodes,
			final Map<String, BoogieIcfgLocation> exitNodes) {
		mValuation = globalValuation;
		addThreadState(threadState);
		mEntryNodes = entryNodes;
		mExitNodes = exitNodes;
	}
	
	public boolean isErrorState() {
		for(final ThreadState threadState : mThreadStates.values()) {
			if(threadState.getCorrespondingIcfgLoc().isErrorLocation()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Deep copy a program state and let all the threadStates' global valuation 
	 * refer to <code> mValuation </code>.
	 */
	public ProgramState(final ProgramState state) {
		mValuation = state.getValuationFullCopy();
		for(final ThreadState s : state.mThreadStates.values()) {
			final ThreadState t = new ThreadState(s);
			t.getValuation().linkGlobals(mValuation);
			this.mThreadStates.put(s.getThreadID(), t);
		}
		mEntryNodes = state.getEntryNodesMap();
		mExitNodes = state.getExitNodesMap();
	}
	
	public List<ThreadStateTransition> getEnableTrans() {
//		/**
//		 * Check if there are threads being in the exit node.
//		 * If so, unlock the block of the thread where current thread
//		 * was forked from.
//		 */
//		for(final ThreadState threadState : mThreadStates.values()) {
//			final String threadProcName = threadState.getCurrentProc().getProcName();
//			if(threadState.getForkedFrom() != -1
//					&& getExitNode(threadProcName).equals(threadState.getCorrespondingIcfgLoc())) {
//				getThreadStateByID(threadState.getForkedFrom()).unlock();
//			}
//		}
		
		
		final List<ThreadStateTransition> enableTrans = new ArrayList<>();
		for(final ThreadState threadState : mThreadStates.values()) {
			enableTrans.addAll(threadState.getEnableTrans());
		}
		
		/**
		 * If there is a join in <code>enableTrans</code> and
		 * it is blocked, remove it from <code>enableTrans</code>.
		 */
		final List<ThreadStateTransition> blockedTrans = new ArrayList<>();
		for(final ThreadStateTransition trans : enableTrans) {
			if(trans.getIcfgEdge() instanceof JoinThreadCurrent) {
				final JoinHandler joinHandler = new JoinHandler(this, trans);
				if(joinHandler.isJoinBlocked()) {
					blockedTrans.add(trans);
				}
			}
		}
		enableTrans.removeAll(blockedTrans);
		
		return enableTrans;
	}
	
	/**
	 * One of thread state do the transition.
	 * (According to the threadID on the {@link ThreadStateTransition}).
	 */
	public ProgramState doTransition(final ThreadStateTransition trans) {
		ProgramState newProgramState = new ProgramState(this);
		
		/**
		 * For Fork and Join, we need to pass the whole program state which
		 * consists of all thread states.
		 */
		if(trans.getIcfgEdge() instanceof ForkThreadCurrent) {
			final ForkHandler forkHandler = new ForkHandler(this, trans);
			newProgramState = forkHandler.doFork();
		} else if(trans.getIcfgEdge() instanceof JoinThreadCurrent) {
			final JoinHandler joinHandler = new JoinHandler(this, trans);
			newProgramState = joinHandler.doJoin();
		} else {
			/**
			 * For others(not Fork and Join), Only one thread state is considered.
			 * which thread state to be executed is according to the threadID
			 * in {@link ThreadStateTransition}.
			 */
			final ThreadState newState 
			= newProgramState.getThreadStateByID(trans.getThreadID()).doTransition(trans);
			/**
			 * update the thread state who did the transition.
			 */
			newProgramState.updateThreadState(newState.getThreadID(), newState);
		}
		
		return newProgramState;
	}
	
	private Map<String, BoogieIcfgLocation> getEntryNodesMap(){
		return mEntryNodes;
	}
	
	private Map<String, BoogieIcfgLocation> getExitNodesMap(){
		return mExitNodes;
	}
	
	public BoogieIcfgLocation getEntryNode(final String procName) {
		return mEntryNodes.get(procName);
	}
	
	public BoogieIcfgLocation getExitNode(final String procName) {
		return mExitNodes.get(procName);
	}

	public int getThreadNumber() {
		return mThreadStates.size();
	}
	
	public ThreadState getThreadStateByID(final long threadID) {
		return mThreadStates.get(threadID);
	}
	
	public void updateThreadState(final long threadID, final ThreadState newState) {
		mThreadStates.put(threadID, newState);
	}
	
	public void removeThreadState(final long threadID) {
		mThreadStates.remove(threadID);
	}
	
	public boolean allNonOldGlobalInitialized() {
		return mValuation.allNonOldGlobalInitialized();
	}
	
	private void addThreadState(ThreadState s) {
		if(mThreadStates.containsKey(s.getThreadID())) {
			throw new UnsupportedOperationException("Thread "
					+ String.valueOf(s.getThreadID()) + " already exists.");
		}
		mThreadStates.put(s.getThreadID(), s);
	}
	
	/**
	 * Check whether two program automaton states are equivalent.
	 * This method is needed in the nested DFS procedure. 
	 * @param anotherProgramState
	 * 		the state which is going to be compared to.
	 * @return
	 * 		true if two states are equivalent, false if not.
	 */
	@Override
	public boolean equals(final ProgramState anotherProgramState) {
		if(this.getThreadNumber() != anotherProgramState.getThreadNumber()) {
			return false;
		}
		
		int equalCount = 0;
		for(final ThreadState anotherThreadState : anotherProgramState.mThreadStates.values()) {
			for(final ThreadState thisThreadState : mThreadStates.values()) {
				if(anotherThreadState.equals(thisThreadState)) {
					equalCount++;
					break;
				}
			}
		}
		return equalCount == this.getThreadNumber() ? true : false;
	}
}
