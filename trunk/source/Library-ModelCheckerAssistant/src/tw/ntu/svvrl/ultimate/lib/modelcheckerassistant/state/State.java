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
import de.uni_freiburg.informatik.ultimate.blockencoding.converter.ShortcutCodeBlock;

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
	private final Map<IProgramVar, Object> mVar2Value = new HashMap<>();
	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private final BoogieIcfgLocation mRelaedIcfgLoc;
	
	public State(Map<IProgramVar, Object> var2Value, BoogieIcfgLocation boogieIcfgLocation) {
		mVar2Value.putAll(var2Value);
		mRelaedIcfgLoc = boogieIcfgLocation;
	}
	
	public BoogieIcfgLocation getRelatedIcfgLoc() {
		return mRelaedIcfgLoc;
	}
	
	public Map<IProgramVar, Object> getVar2Value() {
		return mVar2Value;
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
				} else if(edge instanceof ShortcutCodeBlock) {
				} else if(edge instanceof StatementSequence) {
				} else if(edge instanceof Summary) {
				}
			} else if (edge instanceof RootEdge) {
				throw new UnsupportedOperationException("Suppose type \"RootEdge\" should"
						+ " not appear in the function getEnableTrans()");
			} else {
				throw new UnsupportedOperationException("Error: This type of IcfgEdge is"
						+ " not supported.");
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
		return mVar2Value.equals(anotherState.getVar2Value()) ? true : false;
	}
}
