package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.AbstractIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;

/**
 * This class represents a boogie program state.
 * It differs from {@link BoogieIcfgLocation} in the existence of actual valuation
 * during the program execution.
 * @author Hong-Yang Lin
 *
 */

public class State {
	/**
	 * To record the valuation of boogie variables.
	 */
	private final Map<IProgramVar, Object> mValuation = new HashMap<>();
	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private final BoogieIcfgLocation mRelaedIcfgLoc;
	
	public State(Map<IProgramVar, Object> var2Value, BoogieIcfgLocation boogieIcfgLocation) {
		mValuation.putAll(var2Value);
		mRelaedIcfgLoc = boogieIcfgLocation;
	}
	
	public BoogieIcfgLocation getRelatedIcfgLoc() {
		return mRelaedIcfgLoc;
	}
	
	public Map<IProgramVar, Object> getVar2Value() {
		return mValuation;
	}
	
	public List<IcfgEdge> getEnableTrans() {
		List<IcfgEdge> edges = mRelaedIcfgLoc.getOutgoingEdges();
		List<IcfgEdge> enableTrans = new ArrayList<>();
		for(IcfgEdge edge : edges) {
			if (edge instanceof CodeBlock) {
				if(edge instanceof Call) {
				} else if(edge instanceof ForkThreadCurrent) {
				} else if(edge instanceof ForkThreadOther) {
				} else if(edge instanceof GotoEdge) {
				} else if(edge instanceof JoinThreadCurrent) {
				} else if(edge instanceof JoinThreadOther) {
				} else if(edge instanceof ParallelComposition) {
				} else if(edge instanceof Return) {
				} else if(edge instanceof SequentialComposition) {
				} else if(edge instanceof StatementSequence) {
				} else if(edge instanceof Summary) {
				} else {
				}
			} else if (edge instanceof RootEdge) {
				throw new UnsupportedOperationException("Suppose the type " + edge.getClass().getSimpleName()
						+ " should not appear in the function getEnableTrans()");
			} else {
				throw new UnsupportedOperationException("Error: " + edge.getClass().getSimpleName()
						+ " is not supported.");
			}
		}
		
		return enableTrans;
	}
	
	/**
	 * Check whether two automaton states are equivalent.
	 * This method is needed in the nested DFS procedure. 
	 * @param anotherState
	 * 		the state which is going to be compared to.
	 * @return
	 * 		true if two states are equivalent, false if not.
	 */
	public boolean equals(State anotherState) {
		if(!mRelaedIcfgLoc.equals(anotherState.getRelatedIcfgLoc())) {
			return false;
		}
		return mValuation.equals(anotherState.getVar2Value()) ? true : false;
	}
}
