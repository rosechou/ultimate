package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ASTType;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.boogie.ast.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableDeclaration;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieDeclarations;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.debugidentifiers.DebugIdentifier;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.State;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.StateFactory;

/**
 * This class explores the boogie program states with the help of
 * the pre-build {@link BoogieIcfgContainer}.
 * The {@link State}s is generated when a Icfg transition is going to be moved. 
 * 
 * @author Hong-Yang Lin
 */
public class StateExplorer {
	/*---------------RCFG fields---------------*/
	private final Map<String, BoogieIcfgLocation> mEntryNodes;
	private final Map<String, BoogieIcfgLocation> mExitNode;
	private final Set<BoogieIcfgLocation> mLoopLocations;
	private final Map<String, Set<BoogieIcfgLocation>> mErrorNodes;
	private final Map<String, Map<DebugIdentifier, BoogieIcfgLocation>> mLocNodes;
	private final Set<BoogieIcfgLocation> mInitialNodes;
	/*------------End of RCFG fields-----------*/
	
	private BoogieIcfgLocation mStartLoc;
	private final StateFactory mStateFactory;

	public StateExplorer(BoogieIcfgContainer rcfg) {
		/*---------------RCFG fields---------------*/
		mEntryNodes = rcfg.getProcedureEntryNodes();
		mExitNode = rcfg.getProcedureExitNodes();
		mLoopLocations = rcfg.getLoopLocations();
		mErrorNodes = rcfg.getProcedureErrorNodes();
		mLocNodes = rcfg.getProgramPoints();
		mInitialNodes = rcfg.getInitialNodes();
		/*------------End of RCFG fields-----------*/
		
		mStartLoc = null;
		mStateFactory = new StateFactory(rcfg.getBoogie2SMT().getBoogie2SmtSymbolTable()
				, rcfg.getCfgSmtToolkit());
	}
	
	
	
	
//	public Set<State> getInitialStates() {
//	//...
//	return
//}
	
//	/**
//	 * 		Set a {@link BoogieIcfgLocation} as the start location
//	 * 		in the process of state exploration.
//	 * @param StartLoc
//	 * 		A {@link BoogieIcfgLocation} to start exploration.
//	 * 		Note : StartLoc should be an initial node in rcfg.
//	 */
//	public void setStartLoc(BoogieIcfgLocation StartLoc) {
//		if(!mInitialNodes.contains(StartLoc))
//			throw new UnsupportedOperationException("The given Loc"
//					+ "is not an initial loc, cannot set it as start loc.");
//		mStartLoc = StartLoc;
//	}
	

}
