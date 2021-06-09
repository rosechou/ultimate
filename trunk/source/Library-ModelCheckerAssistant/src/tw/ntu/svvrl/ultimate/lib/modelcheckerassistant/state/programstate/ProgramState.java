package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.Collection;
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
	
	public boolean isErrorState() {
		for(final ThreadState threadState : mThreadStates.values()) {
			if(threadState.getCorrespondingIcfgLoc().isErrorLocation()) {
				return true;
			}
		}
		return false;
	}
	
	
	public List<ProgramStateTransition> getEnabledTrans() {
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
		
		
		final List<ProgramStateTransition> enabledTrans = new ArrayList<>();
		for(final ThreadState threadState : mThreadStates.values()) {
			enabledTrans.addAll(threadState.getEnabledTrans());
		}
		
		/**
		 * If there is a join in <code>enableTrans</code> and
		 * it is blocked, remove it from <code>enableTrans</code>.
		 */
		final List<ProgramStateTransition> blockedTrans = new ArrayList<>();
		for(final ProgramStateTransition trans : enabledTrans) {
			assert trans instanceof ThreadStateTransition;
			if(((ThreadStateTransition) trans).getIcfgEdge() instanceof JoinThreadCurrent) {
				final JoinHandler joinHandler = new JoinHandler(this, (ThreadStateTransition) trans);
				if(joinHandler.isJoinBlocked()) {
					blockedTrans.add(trans);
				}
			}
		}
		enabledTrans.removeAll(blockedTrans);
		
		
		if(enabledTrans.isEmpty()) {
			/**
			 * If every thread state has no successor, attach a nil self-loop.
			 * @see the definition of synchronous product.
			 */
			boolean hasNoSucc = true;
			for(final ThreadState threadState : mThreadStates.values()) {
				if(!threadState.getCorrespondingIcfgLoc().getOutgoingEdges().isEmpty()) {
					hasNoSucc = false;
				}
			}
			if(hasNoSucc) {
				enabledTrans.add(new NilSelfLoop());
			}
		}
		return enabledTrans;
	}
	
	/**
	 * One of thread state do the transition.
	 * (According to the threadID on the {@link ThreadStateTransition}).
	 */
	public ProgramState doTransition(final ProgramStateTransition trans) {
		ProgramState newProgramState = new ProgramState(this);
		
		if(trans instanceof ThreadStateTransition) {
			final ThreadStateTransition threadTrans = (ThreadStateTransition) trans;
			/**
			 * For Fork and Join, we need to pass the whole program state which
			 * consists of all thread states.
			 */
			if(threadTrans.getIcfgEdge() instanceof ForkThreadCurrent) {
				final ForkHandler forkHandler = new ForkHandler(this, threadTrans);
				newProgramState = forkHandler.doFork();
			} else if(threadTrans.getIcfgEdge() instanceof JoinThreadCurrent) {
				final JoinHandler joinHandler = new JoinHandler(this, threadTrans);
				newProgramState = joinHandler.doJoin();
			} else {
				/**
				 * For others(not Fork and Join), Only one thread state is considered.
				 * which thread state to be executed is according to the threadID
				 * in {@link ThreadStateTransition}.
				 */
				final ThreadState newState 
				= newProgramState.getThreadStateByID(threadTrans.getThreadID()).doTransition(threadTrans);
				/**
				 * update the thread state who did the transition.
				 */
				newProgramState.updateThreadState(newState.getThreadID(), newState);
			}
			return newProgramState;
		} else if(trans instanceof NilSelfLoop) {
			/**
			 * Do nothing.
			 */
			return this;
		} else {
			throw new UnsupportedOperationException("Unkown ProgramStateTransition type: "
					+ trans.getClass().getSimpleName());
		}
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
	 * 		the state which is going to compared with.
	 * @return
	 * 		true if two states are equivalent, false if not.
	 */
	@Override
	public boolean equals(final ProgramState anotherProgramState) {
		if(this.getThreadNumber() != anotherProgramState.getThreadNumber()) {
			return false;
		}
		final int threadNumber = this.getThreadNumber();
		
		List<Boolean> match = new ArrayList<>();
		for(int i = 0; i < threadNumber; i++) {
			match.add(false);
		}
		
		/**
		 * If two program state are equivalent, thread states must have
		 * one-to-one mapping.
		 */
		ThreadState[] thisThreadStates = this.mThreadStates.values().toArray(new ThreadState[threadNumber]);
		ThreadState[] anotherThreadStates = anotherProgramState.mThreadStates.values().toArray(new ThreadState[threadNumber]);
		for(int i = 0; i < threadNumber; i++) {
			for(int j = 0; j < threadNumber; j++) {
				if(!match.get(j) && thisThreadStates[i].equals(anotherThreadStates[j])) {
					match.set(j, true);
					break;
				}
			}
		}
		
		/**
		 * If there's any thread state that doesn't match, the two program states
		 * are not equivalent.
		 */
		return !match.contains(false);
	}
}