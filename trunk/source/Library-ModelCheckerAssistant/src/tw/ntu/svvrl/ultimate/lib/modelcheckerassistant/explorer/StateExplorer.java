package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ASTType;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.boogie.ast.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableDeclaration;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieDeclarations;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.State;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.StateSymbolTable;

/**
 * This class explores the boogie program states with the help of
 * the pre-build {@link BoogieIcfgContainer}.
 * The {@link State}s is generated when a Icfg transition is going to be moved. 
 * 
 * @author Hong-Yang Lin
 */
public class StateExplorer {
	private StateSymbolTable mCurrentStateSymbolTable;
	private State mCurrentState;
	//private final Map<IProgramVar, BoogieASTNode> mBoogieVar2AstNode;

	public StateExplorer(BoogieIcfgContainer rcfg) {
		StateSymbolTable originalStateSymbolTable = 
				new StateSymbolTable(rcfg.getBoogie2SMT().getBoogie2SmtSymbolTable());
		//mCurrentStateSymbolTable = initializeVarType()
		
	}
	
	
	/**
	 * Set all boogie variables' type and value.
	 * @param stateSymbolTable
	 * 			The target state symbol table
	 * @return
	 * 			An updated state symbol table
	 */
	private StateSymbolTable initializeVarType(BoogieIcfgContainer rcfg, StateSymbolTable stateSymbolTable) {
		BoogieDeclarations boogieDeclarations = 
				rcfg.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieDeclarations();
		/**
		 * process all global variable declarations
		 */
		for(VariableDeclaration globalVarDeclaration : boogieDeclarations.getGlobalVarDeclarations()) {
				for(VarList globalVar : globalVarDeclaration.getVariables()) {
					IBoogieType boogieType = globalVar.getType().getBoogieType();
					if (boogieType instanceof BoogiePrimitiveType) {
						// TODO : assign type and value to this var
					}
					else {
						// ...
					}
				}
		}
		
		return stateSymbolTable;
	}
}
