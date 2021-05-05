package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

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
	private final StateSymbolTable mStateSymbolTable;
	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private final BoogieIcfgLocation mRelaedIcfgLoc;
	
	public State(StateSymbolTable stateSymbolTable, BoogieIcfgLocation boogieIcfgLocation) {
		mStateSymbolTable = stateSymbolTable;
		mRelaedIcfgLoc = boogieIcfgLocation;
	}
}
