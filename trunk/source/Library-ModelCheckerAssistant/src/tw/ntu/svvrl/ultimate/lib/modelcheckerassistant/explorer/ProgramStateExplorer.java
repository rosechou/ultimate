package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.debugidentifiers.DebugIdentifier;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramStateFactory;

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
	private final Map<String, BoogieIcfgLocation> mExitNode;
	private final Set<BoogieIcfgLocation> mLoopLocations;
	private final Map<String, Set<BoogieIcfgLocation>> mErrorNodes;
	private final Map<String, Map<DebugIdentifier, BoogieIcfgLocation>> mLocNodes;
	private final Set<BoogieIcfgLocation> mInitialNodes;
	/*------------End of RCFG fields-----------*/
	
	private final ProgramStateFactory mProgramStateFactory;

	public ProgramStateExplorer(final BoogieIcfgContainer rcfg) {
		/*---------------RCFG fields---------------*/
		mEntryNodes = rcfg.getProcedureEntryNodes();
		mExitNode = rcfg.getProcedureExitNodes();
		mLoopLocations = rcfg.getLoopLocations();
		mErrorNodes = rcfg.getProcedureErrorNodes();
		mLocNodes = rcfg.getProgramPoints();
		mInitialNodes = rcfg.getInitialNodes();
		/*------------End of RCFG fields-----------*/
		
		mProgramStateFactory = new ProgramStateFactory(rcfg.getBoogie2SMT().getBoogie2SmtSymbolTable()
				, rcfg.getCfgSmtToolkit());
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
}
