package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit.ThreadTransitionToolkit;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProcInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

/**
 * This class represents a boogie state in a specific thread.
 * It differs from {@link BoogieIcfgLocation} in the existence of actual valuation
 * during the program execution.
 *
 */

public class ThreadState extends ValuationState<ThreadState>{

	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private BoogieIcfgLocation mCorrespondingIcfgLoc;
	private final FuncInitValuationInfo mFuncInitValuationInfo;
	
	private final Map<String, List<String>> mProc2InParams;
	private final Map<String, List<String>> mProc2OutParams;
	
	/**
	 * The stack that keeps the procedure calls.
	 * top element is the current procedure name.
	 * call: push
	 * return: pop
	 */
	private Stack<ProcInfo> mProcStack = new Stack<>();
	
	
	/**
	 * From 0 to total number of threads - 1.
	 */
	private long mThreadID;
	
	/**
	 * If this state encounter {@link JoinStatement} and
	 * is waiting for other thread's termination, then
	 * this flag is true.
	 */
	private boolean mIsBlocked = false;
	
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
						final FuncInitValuationInfo funcInitValuationInfo,
						final Map<String, List<String>> proc2InParams,
						final Map<String, List<String>> proc2OutParams,
						final int threadID) {
		mValuation = v;
		mCorrespondingIcfgLoc = boogieIcfgLocation;
		mFuncInitValuationInfo = funcInitValuationInfo;
		mProc2InParams = proc2InParams;
		mProc2OutParams = proc2OutParams;
		mProcStack.push(
				new ProcInfo(mCorrespondingIcfgLoc.getProcedure()));
		mThreadID = threadID;
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
		mFuncInitValuationInfo = oldState.getFuncInitValuationInfo();
		mProc2InParams = oldState.getProc2InParams();
		mProc2OutParams = oldState.getProc2OutParams();
		mProcStack = oldState.getProcStackCopy();
		mThreadID = oldState.getThreadID();
	}
	
	/**
	 * Copy constructor
	 * Local valuation and stack are deep copied
	 * while global valuation is shallow copied.
	 */
	public ThreadState(final ThreadState threadState) {
		mValuation = threadState.getValuationLocalCopy();
		mCorrespondingIcfgLoc = threadState.getCorrespondingIcfgLoc();
		mFuncInitValuationInfo = threadState.getFuncInitValuationInfo();
		mProc2InParams = threadState.getProc2InParams();
		mProc2OutParams = threadState.getProc2OutParams();
		mProcStack = threadState.getProcStackCopy();
		mThreadID = threadState.getThreadID();
	}

	public BoogieIcfgLocation getCorrespondingIcfgLoc() {
		return mCorrespondingIcfgLoc;
	}
	
	public FuncInitValuationInfo getFuncInitValuationInfo() {
		return mFuncInitValuationInfo;
	}
	
	public Map<String, List<String>> getProc2InParams() {
		return mProc2InParams;
	}
	
	public Map<String, List<String>> getProc2OutParams() {
		return mProc2OutParams;
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
	
	public void block() {
		mIsBlocked = true;
	}
	
	public boolean isBlocked() {
		return mIsBlocked;
	}
	
	public Valuation getValuation() {
		return mValuation;
	}
	
	/**
	 * @note Only used when procedure {@link Return}.
	 */
	public void setValuation(Valuation v) {
		mValuation = v;
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
	 * @return
	 * 		a list of enable transitions.
	 */
	public List<ThreadStateTransition> getEnableTrans() {
		final List<IcfgEdge> edges = mCorrespondingIcfgLoc.getOutgoingEdges();
		final List<ThreadStateTransition> enableTrans = new ArrayList<>();
		for(final IcfgEdge edge : edges) {
			/**
			 * Mark outgoing edge using current thread ID.
			 */
			final ThreadStateTransition trans = new ThreadStateTransition(edge, mThreadID);
			final ThreadTransitionToolkit transitionToolkit 
					= new ThreadTransitionToolkit(trans, this);
			if (transitionToolkit.checkTransEnable()) {
				enableTrans.add(trans);
			}
		}
		return enableTrans;
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
				= new ThreadTransitionToolkit(edge, this);
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
	public boolean equals(final ThreadState anotherThreadState) {
		if(!mCorrespondingIcfgLoc.equals(anotherThreadState.getCorrespondingIcfgLoc())) {
			return false;
		}
		return mValuation.equals(anotherThreadState.getValuationFullCopy()) ? true : false;
	}


	@Override
	public String toString() {
		return "ThreadState@" + mCorrespondingIcfgLoc.toString();
	}
}
