package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit.ThreadTransitionToolkit;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProcInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

/**
 * This class represents a boogie state in a specific thread.
 * It differs from {@link BoogieIcfgLocation} in the existence of actual valuation
 * during the program execution.
 *
 */

public class ThreadState extends ValuationState<ThreadState>{
	/**
	 * Indicate which copy method used in the constructor.
	 * 
	 * localCopy : Local valuation and stack are deep copied
	 * while global valuation is shallow copied.
	 * 
	 * fullCopy : Local valuation, global valuation and stack are deep copied
	 *
	 */
	public static enum ConstructStrategy{
		localCopy, fullCopy
	}
	
	private ProgramStateExplorer mProgramStateExplorer;
	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private BoogieIcfgLocation mCorrespondingIcfgLoc;
	
	
	/**
	 * The stack that keeps the procedure calls.
	 * top element is the current procedure.
	 * call: push
	 * return: pop
	 */
	private Stack<ProcInfo> mProcStack = new Stack<>();
	
	
	/**
	 * From 0 to total number of threads - 1.
	 */
	private long mThreadID;
	
	/**
	 * Initial constructor.
	 * @param v
	 * @param boogieIcfgLocation
	 * 		Corresponding rcfg location.
	 * @param funcInitValuationInfo
	 * 		function info
	 */
	public ThreadState(final Valuation v,
						final BoogieIcfgLocation boogieIcfgLocation,
						final long threadID, final ProgramStateExplorer pe) {
		mValuation = v;
		mCorrespondingIcfgLoc = boogieIcfgLocation;
		mProcStack.push(
				new ProcInfo(mCorrespondingIcfgLoc.getProcedure()));
		mThreadID = threadID;
		mProgramStateExplorer = pe;
	}
	
	/**
	 * A program state constructor that updates the valuation.
	 * Thus the field <code>mCorrespondingIcfgLoc</code> may move
	 * but we have no idea where it is. This field remains unknown until
	 * {@link #setCorrespondingIcfgLoc} is called.
	 * Used in {@link StatementsExecutor#updateThreadState}'s
	 * Pass the old state to get FuncInitValuationInfo and Proc2InParams.
	 * @param valuation
	 */
	public ThreadState(final Valuation v, final ThreadState oldState) {
		mValuation = v;
		mCorrespondingIcfgLoc = null;
		mProcStack = oldState.getProcStackCopy();
		mThreadID = oldState.getThreadID();
		mProgramStateExplorer = oldState.getProgramStateExplorer();
	}
	
	/**
	 * Copy constructor
	 */
	public ThreadState(final ThreadState threadState, final ConstructStrategy t) {
		if(t == ConstructStrategy.localCopy) {
			mValuation = threadState.getValuationLocalCopy();
		} else if(t == ConstructStrategy.fullCopy) {
			mValuation = threadState.getValuationFullCopy();
		} else {
			throw new UnsupportedOperationException("Unknown ConstructStrategy: " + 
					t.getClass().getSimpleName());
		}
		mCorrespondingIcfgLoc = threadState.getCorrespondingIcfgLoc();
		mProcStack = threadState.getProcStackCopy();
		mThreadID = threadState.getThreadID();
		mProgramStateExplorer = threadState.getProgramStateExplorer();
	}

	public BoogieIcfgLocation getCorrespondingIcfgLoc() {
		return mCorrespondingIcfgLoc;
	}
	
	public ProgramStateExplorer getProgramStateExplorer() {
		return mProgramStateExplorer;
	}
	
	public Stack<ProcInfo> getProcStackCopy() {
		return (Stack<ProcInfo>) mProcStack.clone();
	}
	
	public void pushProc(ProcInfo proc) {
		mProcStack.push(proc);
	}
	
	public void popProc() {
		mProcStack.pop();
	}
	
	public ProcInfo getCurrentProc() {
		return mProcStack.peek();
	}
	
	public long getThreadID() {
		return mThreadID;
	}
	
	public void assignNewThreadID(final long newThreadID) {
		mThreadID = newThreadID;
	}
	
	public Valuation getValuation() {
		return mValuation;
	}
	
	/**
	 * @note Only used when procedure {@link Return}.
	 */
	public void resetLocalValuation(Valuation v) {
		mValuation.resetLocals(v);
	}
	
	public ProcInfo getCallerProc() {
		if(mProcStack.size() > 1) {
			final ProcInfo temp = mProcStack.peek();
			mProcStack.pop();
			final ProcInfo result = mProcStack.peek();
			mProcStack.push(temp);
			return result;
		} else {
			throw new UnsupportedOperationException("No caller proc.");
		}
		
	}
	
	/**
	 * This method is used for make up the unknown <code>mCorrespondingIcfgLoc</code>.
	 * see {@link #ThreadState(Map, FuncInitValuationInfo)}.
	 * @param icfgLocation
	 */
	public void setCorrespondingIcfgLoc(final BoogieIcfgLocation icfgLocation) {
		mCorrespondingIcfgLoc = icfgLocation;
	}
	
	/**
	 * Get the a list of transitions which is enable from this state.
	 * A transition is enable if the assume statement is not violated.
	 * 
	 * @param exitNodes
	 * 		a mapping from procedure name to cfg location.
	 * 		This is used to check whether this thread state is in the exit node.
	 * @return
	 * 		a list of enable transitions.
	 */
	public List<ThreadStateTransition> getEnabledTrans() {
		final List<IcfgEdge> edges = mCorrespondingIcfgLoc.getOutgoingEdges();
		final List<ThreadStateTransition> enabledTrans = new ArrayList<>();
		for(final IcfgEdge edge : edges) {
			/**
			 * Mark outgoing edge using current thread ID.
			 */
			final ThreadStateTransition trans = new ThreadStateTransition(edge, mThreadID);
			final ThreadTransitionToolkit transitionToolkit 
					= new ThreadTransitionToolkit(trans, this, mProgramStateExplorer);
			if (transitionToolkit.checkTransEnabled()) {
				enabledTrans.add(trans);
			}
		}
		
		return enabledTrans;
	}
	
	
	
	/**
	 * 		Execute the statements on the IcfgEge and move from a this state
	 * 		to the next state.
	 * @param edge
	 * 		An IcfgEge
	 * @return
	 * 		The next thread state.
	 */
	public ThreadState doTransition(final ThreadStateTransition edge) {
		final ThreadTransitionToolkit transitionToolkit
				= new ThreadTransitionToolkit(edge, this, mProgramStateExplorer);
		return transitionToolkit.doTransition();
	}
	
	/**
	 * Check whether two thread states are equivalent.
	 * This method is needed in the nested DFS procedure. 
	 * @param anotherThreadState
	 * 		the state which is going to be compared to.
	 * @return
	 * 		true if two states are equivalent, false if not.
	 */
	@Override
	public boolean equals(final ThreadState anotherThreadState) {
		if(!mCorrespondingIcfgLoc.equals(anotherThreadState.getCorrespondingIcfgLoc())) {
			return false;
		}
		/**
		 * The procNames in the two procStacks should be consistent
		 */
		final List<ProcInfo> l1 = new ArrayList<>(mProcStack);
		final List<ProcInfo> l2 = new ArrayList<>(anotherThreadState.getProcStackCopy());
		if(l1.size() != l2.size()) {
			return false;
		}
		for(int i = 0; i < l1.size(); i++) {
			if(!l1.get(i).getProcName().equals(l2.get(i).getProcName())) {
				return false;
			}
		}
		
		return mValuation.equals(anotherThreadState.getValuationFullCopy()) ? true : false;
	}


	@Override
	public String toString() {
		return "Thread" + mThreadID + "@" + mCorrespondingIcfgLoc.toString();
	}
}
