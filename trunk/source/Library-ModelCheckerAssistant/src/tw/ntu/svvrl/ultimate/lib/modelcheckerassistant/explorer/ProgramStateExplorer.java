package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.debugidentifiers.DebugIdentifier;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ForkHandler;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.JoinHandler;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.NilSelfLoop;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramStateFactory;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;

/**
 * This class explores the boogie program states with the help of
 * the pre-build {@link BoogieIcfgContainer}.
 * The {@link ThreadState}s is generated when a Icfg transition is going to be moved. 
 * 
 * @author Hong-Yang Lin
 */
public class ProgramStateExplorer {
	/*---------------RCFG fields---------------*/
	private final Map<String, BoogieIcfgLocation> mEntryNodes;
	private final Map<String, BoogieIcfgLocation> mExitNodes;
	private final Set<BoogieIcfgLocation> mLoopLocations;
	private final Map<String, Set<BoogieIcfgLocation>> mErrorNodes;
	private final Map<String, Map<DebugIdentifier, BoogieIcfgLocation>> mLocNodes;
	private final Set<BoogieIcfgLocation> mInitialNodes;
	/*------------End of RCFG fields-----------*/
	
	private final ProgramStateFactory mProgramStateFactory;

	public ProgramStateExplorer(final BoogieIcfgContainer rcfg) {
		/*---------------RCFG fields---------------*/
		mEntryNodes = rcfg.getProcedureEntryNodes();
		mExitNodes = rcfg.getProcedureExitNodes();
		mLoopLocations = rcfg.getLoopLocations();
		mErrorNodes = rcfg.getProcedureErrorNodes();
		mLocNodes = rcfg.getProgramPoints();
		mInitialNodes = rcfg.getInitialNodes();
		/*------------End of RCFG fields-----------*/
		
		mProgramStateFactory = new ProgramStateFactory(rcfg.getBoogie2SMT().getBoogie2SmtSymbolTable()
				, rcfg.getCfgSmtToolkit(), mEntryNodes, mExitNodes);
	}
	
	
	/**
	 * Based on the given rcfg, extend the initial {@link BoogieIcfgLocation}s with
	 * value tables so that they become automaton initial states.
	 * @return	A set containing all initial states.
	 */
	public Set<ProgramState> getInitialStates() {
		Set<ProgramState> initialProgramStates = new HashSet<>();
		
		for(BoogieIcfgLocation initialLoc: mInitialNodes) {
			initialProgramStates.add(mProgramStateFactory.createInitialState(initialLoc));
		}
		
		return initialProgramStates;
	}
	
	public List<ProgramStateTransition> getEnabledTrans(final ProgramState p) {
		final List<ProgramStateTransition> enabledTrans = new ArrayList<>();
		for(final ThreadState threadState : p.getThreadStates()) {
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
				final JoinHandler joinHandler = new JoinHandler(p, (ThreadStateTransition) trans, mExitNodes);
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
			for(final ThreadState threadState : p.getThreadStates()) {
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
	public ProgramState doTransition(final ProgramState p, final ProgramStateTransition trans) {
		ProgramState newProgramState = new ProgramState(p);
		
		if(trans instanceof ThreadStateTransition) {
			final ThreadStateTransition threadTrans = (ThreadStateTransition) trans;
			/**
			 * For Fork and Join, we need to pass the whole program state which
			 * consists of all thread states.
			 */
			if(threadTrans.getIcfgEdge() instanceof ForkThreadCurrent) {
				final ForkHandler forkHandler = new ForkHandler(p, threadTrans, mEntryNodes);
				newProgramState = forkHandler.doFork();
			} else if(threadTrans.getIcfgEdge() instanceof JoinThreadCurrent) {
				final JoinHandler joinHandler = new JoinHandler(p, threadTrans, mExitNodes);
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
			return p;
		} else {
			throw new UnsupportedOperationException("Unkown ProgramStateTransition type: "
					+ trans.getClass().getSimpleName());
		}
	}
	
	/**
	 * Only for debugging
	 */
	public ProgramState getLocStateById(String id) {
		for(Map<DebugIdentifier, BoogieIcfgLocation> m : mLocNodes.values()) {
			for(BoogieIcfgLocation l : m.values()) {
				if(l.toString().equals(id)) {
					return mProgramStateFactory.createInitialState(l);
				}
			}
		}
		return null;
	}
	
	public BoogieIcfgLocation getEntryNode(final String procName) {
		return mEntryNodes.get(procName);
	}
	
	public BoogieIcfgLocation getExitNode(final String procName) {
		return mExitNodes.get(procName);
	}
}
