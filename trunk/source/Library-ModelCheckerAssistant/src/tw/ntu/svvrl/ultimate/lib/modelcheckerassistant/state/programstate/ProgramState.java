package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
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
	final private Map<Integer, ThreadState> mThreadStates = new HashMap<>();
	
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
		for(final ThreadState s : state.getThreadStatesMap().values()) {
			final ThreadState t = new ThreadState(s);
			t.getValuation().linkGlobals(mValuation);
			mThreadStates.put(s.getThreadID(), t);
		}
		mEntryNodes = state.getEntryNodesMap();
		mExitNodes = state.getExitNodesMap();
	}
	
	public List<ThreadStateTransition> getEnableTrans() {
		final List<ThreadStateTransition> enableTrans = new ArrayList<>();
		for(final ThreadState threadState : mThreadStates.values()) {
			enableTrans.addAll(threadState.getEnableTrans());
		}
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
	
	public ThreadState getThreadStateByID(final int threadID) {
		return mThreadStates.get(threadID);
	}
	
	private Map<Integer, ThreadState> getThreadStatesMap() {
		return mThreadStates;
	}
	
	public void updateThreadState(final int threadID, final ThreadState newState) {
		mThreadStates.put(threadID, newState);
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
		/**
		 * Thread ID should be consistent?
		 */
		for(final ThreadState threadState : anotherProgramState.getThreadStatesMap().values()) {
			if(!mThreadStates.get(threadState.getThreadID()).equals(threadState)) {
				return false;
			}
		}
		return true;
	}
}
