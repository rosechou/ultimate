package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;

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
	private final Map<String, Map<String, Object>> mValuation = new HashMap<>();

	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private final BoogieIcfgLocation mCorrespondingIcfgLoc;
	private final FuncInitValuationInfo mFuncInitValuationInfo;
	
	/**
	 * A program state constructor that only cares the valuation.
	 * Used in {@link #processAssignmentStatement(AssignmentStatement)}.
	 * @param valuation
	 */
	public ProgramState(final Map<String, Map<String, Object>> valuation, final FuncInitValuationInfo funcInitValuationInfo) {
		mValuation.putAll(valuation);
		mCorrespondingIcfgLoc = null;
		mFuncInitValuationInfo = funcInitValuationInfo;
	}
	
	public ProgramState(final Map<String, Map<String, Object>> valuation,
						final BoogieIcfgLocation boogieIcfgLocation,
						final FuncInitValuationInfo funcInitValuationInfo) {
		mValuation.putAll(valuation);
		mCorrespondingIcfgLoc = boogieIcfgLocation;
		mFuncInitValuationInfo = funcInitValuationInfo;
	}
	
	public BoogieIcfgLocation getCorrespondingIcfgLoc() {
		return mCorrespondingIcfgLoc;
	}
	
	public Map<String, Map<String, Object>> getValuationMap() {
		return mValuation;
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
			TransitionToolkit transitionToolkit = new TransitionToolkit(edge, mValuation, mFuncInitValuationInfo);
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
		TransitionToolkit transitionToolkit = new TransitionToolkit(edge, mValuation, mFuncInitValuationInfo);
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
		return mValuation.equals(anotherProgramState.getValuationMap()) ? true : false;
	}
}
