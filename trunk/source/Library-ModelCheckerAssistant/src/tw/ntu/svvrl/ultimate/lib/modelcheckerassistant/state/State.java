package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.HashMap;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;

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
}
