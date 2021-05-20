package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

/**
 * This class represents a boogie program state.
 * It differs from {@link BoogieIcfgLocation} in the existence of actual valuation
 * during the program execution.
 * @author Hong-Yang Lin
 *
 */

public class ProgramState {
	/**
	 * To record the valuation of boogie variables.
	 * Type: procedure name × identifier × value
	 */
	private final Valuation mValuation;

	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private BoogieIcfgLocation mCorrespondingIcfgLoc;
	private final FuncInitValuationInfo mFuncInitValuationInfo;
	
	private final Map<String, List<String>> mProc2InParams;
	
	/**
	 * A program state constructor that only cares the valuation.
	 * Used in {@link StatementsExecutor#updateProgramState}'s
	 * Pass the old state to get FuncInitValuationInfo and Proc2InParams.
	 * @param valuation
	 */
	public ProgramState(final Valuation v, final ProgramState oldState) {
		mValuation = v;
		mCorrespondingIcfgLoc = null;
		mFuncInitValuationInfo = oldState.getFuncInitValuationInfo();
		mProc2InParams = oldState.getProc2InParams();
	}
	
	/**
	 * Initial constructor.
	 * @param v
	 * @param boogieIcfgLocation
	 * 		Corresponding rcfg location.
	 * @param funcInitValuationInfo
	 * 		function info
	 */
	public ProgramState(final Valuation v,
						final BoogieIcfgLocation boogieIcfgLocation,
						final FuncInitValuationInfo funcInitValuationInfo,
						final Map<String, List<String>> proc2InParams) {
		mValuation = v;
		mCorrespondingIcfgLoc = boogieIcfgLocation;
		mFuncInitValuationInfo = funcInitValuationInfo;
		mProc2InParams = proc2InParams;
	}
	
	/**
	 * copy constructor
	 * valuation is shallow copied.
	 */
	public ProgramState(final ProgramState programState) {
		mValuation = programState.getValuation().shallowCopy();
		mCorrespondingIcfgLoc = programState.getCorrespondingIcfgLoc();
		mFuncInitValuationInfo = programState.getFuncInitValuationInfo();
		mProc2InParams = programState.getProc2InParams();
	}
	
	private Map<String, Map<String, Object>> shallowCopyValuation(final Map<String, Map<String, Object>> valuationMap) {
		Map<String, Map<String, Object>> val = new HashMap<>();
		for(String procName : valuationMap.keySet()) {
			Map<String, Object> id2v = new HashMap<>(valuationMap.get(procName));
			val.put(procName, id2v);
		}
		return val;
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
	
	public Valuation getValuation() {
		return mValuation;
	}
	
	/**
	 * This method is used for make up the unknown <code>mCorrespondingIcfgLoc</code>.
	 * see {@link #ProgramState(Map, FuncInitValuationInfo)}.
	 * @param icfgLocation
	 */
	public void setCorrespondingIcfgLoc(BoogieIcfgLocation icfgLocation) {
		if(mCorrespondingIcfgLoc == null) {
			mCorrespondingIcfgLoc = icfgLocation;
		}
		else {
			throw new UnsupportedOperationException("Cannot change a state\'s"
					+ "mCorrespondingIcfgLoc.");
		}
	}
	
	/**
	 * Get the a list of transitions which is enable from this state.
	 * A transition is enable if the assume statement is not violated.
	 * @return
	 * 		a list of enable transitions.
	 */
	public List<IcfgEdge> getEnableTrans() {
		List<IcfgEdge> edges = mCorrespondingIcfgLoc.getOutgoingEdges();
		List<IcfgEdge> enableTrans = new ArrayList<>();
		for(final IcfgEdge edge : edges) {
			final TransitionToolkit transitionToolkit = new TransitionToolkit(edge, this);
			if (transitionToolkit.checkTransEnable()) {
				enableTrans.add(edge);
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
	 * 		The next program state.
	 */
	public ProgramState doTransition(final IcfgEdge edge) {
		final TransitionToolkit transitionToolkit = new TransitionToolkit(edge, this);
		return transitionToolkit.doTransition();
	}
	
	/**
	 * Check whether two automaton states are equivalent.
	 * This method is needed in the nested DFS procedure. 
	 * @param anotherProgramState
	 * 		the state which is going to be compared to.
	 * @return
	 * 		true if two states are equivalent, false if not.
	 */
	public boolean equals(final ProgramState anotherProgramState) {
		if(!mCorrespondingIcfgLoc.equals(anotherProgramState.getCorrespondingIcfgLoc())) {
			return false;
		}
		return mValuation.equals(anotherProgramState.getValuation()) ? true : false;
	}
}
