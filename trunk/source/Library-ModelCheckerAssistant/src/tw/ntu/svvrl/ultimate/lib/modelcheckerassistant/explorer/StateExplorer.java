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

/**
 * This class explores the boogie program states with the help of
 * the pre-build {@link BoogieIcfgContainer}.
 * The {@link State}s is generated when a Icfg transition is going to be moved. 
 * 
 * @author Hong-Yang Lin
 */
public class StateExplorer {
	private final CfgSmtToolkit mCfgSmtTookit;
	private final Map<String, BoogieIcfgLocation> mEntryNodes;
	private final Map<String, BoogieIcfgLocation> mExitNode;
	private final Set<BoogieIcfgLocation> mLoopLocations;
	private final Map<String, Set<BoogieIcfgLocation>> mErrorNodes;
	private final Map<String, Map<DebugIdentifier, BoogieIcfgLocation>> mLocNodes;
	private final Set<BoogieIcfgLocation> mInitialNodes;
	
	private State mCurrentState;
	private final Map<IProgramVar, Object> mVar2Value = new HashMap<>();
	private BoogieIcfgLocation mStartLoc;

	public StateExplorer(BoogieIcfgContainer rcfg) {
		mCfgSmtTookit = rcfg.getCfgSmtToolkit();
		mEntryNodes = rcfg.getProcedureEntryNodes();
		mExitNode = rcfg.getProcedureExitNodes();
		mLoopLocations = rcfg.getLoopLocations();
		mErrorNodes = rcfg.getProcedureErrorNodes();
		mLocNodes = rcfg.getProgramPoints();
		mInitialNodes = rcfg.getInitialNodes();
		
		Boogie2SmtSymbolTable boogie2SmtSymbolTable = rcfg.getBoogie2SMT().getBoogie2SmtSymbolTable();
		initializeVar2Value(boogie2SmtSymbolTable);
		mStartLoc = null;
	}
	
	
	/**
	 * Initialize all boogie variables' type and value.
	 * @param boogie2SmtSymbolTable
	 * 		boogie2SmtSymbolTable retrieved from rcfg
	 * @param stateSymbolTable
	 * 		stateSymbolTable to look up
	 */
	private void initializeVar2Value(Boogie2SmtSymbolTable boogie2SmtSymbolTable) {
		
		/**
		 * process all global variables
		 */
		for(IProgramNonOldVar globalVar : boogie2SmtSymbolTable.getGlobals()) {
			initializeVar2Value(boogie2SmtSymbolTable, globalVar);
		}
		
		
		/**
		 * process all local variables
		 * (Maybe do this when getting into a procedure)
		 */
		for(String procName : mCfgSmtTookit.getProcedures()) {
			for(ILocalProgramVar localVar : boogie2SmtSymbolTable.getLocals(procName)) {
				initializeVar2Value(boogie2SmtSymbolTable, localVar);
			}
		}
	}
	
	/**
	 *  Initialize a specific boogie variable's type and value.
	 *  For Boolean variables, set their values to false.
	 *  For Int variables, set their values to 0.
	 *  Real variables are not yet implemented.
	 *  
	 * @param boogie2SmtSymbolTable
	 * 		boogie2SmtSymbolTable retrieved from rcfg
	 * @param var
	 * 		the target variable.
	 */
	private void initializeVar2Value(Boogie2SmtSymbolTable boogie2SmtSymbolTable, IProgramVar var) {
		BoogieASTNode boogieASTNode = boogie2SmtSymbolTable.getAstNode(var);
		if(boogieASTNode instanceof VarList) {
			IBoogieType boogieType = ((VarList)boogieASTNode).getType().getBoogieType();
			if (boogieType instanceof BoogiePrimitiveType) {
				switch(((BoogiePrimitiveType) boogieType).getTypeCode()) {
					case BoogiePrimitiveType.BOOL:
						boolean boolValue = false;
						mVar2Value.put(var, boolValue);
						break;
					case BoogiePrimitiveType.INT:
						int intValue = 0;
						mVar2Value.put(var, intValue);
						break;
					case BoogiePrimitiveType.REAL:
						throw new UnsupportedOperationException("Boogie variable with type"
								+ " \"real\" is not yet supported.");
					case BoogiePrimitiveType.ERROR:
					default:
						throw new UnsupportedOperationException("Boogie variable with"
								+ " error or unknown type.");
				}
			}
			else {
				throw new UnsupportedOperationException("Unsupported"
						+ "BoogieType:" + boogieType.toString());
			}
		}
	}
	
	/**
	 * 		Set a {@link BoogieIcfgLocation} as the start location
	 * 		in the process of state exploration.
	 * @param StartLoc
	 * 		A {@link BoogieIcfgLocation} to start exploration.
	 * 		Note : StartLoc should be an initial node in rcfg.
	 */
	public void setStartLoc(BoogieIcfgLocation StartLoc) {
		if(!mInitialNodes.contains(StartLoc))
			throw new UnsupportedOperationException("The given Loc"
					+ "is not an initial loc, cannot set it as start loc.");
		mStartLoc = StartLoc;
	}
	
	public void startExploration() {
		//...
	}
}
