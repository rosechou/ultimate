package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramConst;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramNonOldVar;

/**
 * Symbol table for a state in Büchi automata
 *
 */
public interface IStateSymbolTable {
	/**
	 * @return Set of all global (non-old) variables that occur in the ICFG.
	 */
	Set<IProgramNonOldVar> getGlobals();

	/**
	 * @return all local variables, input parameters and output parameters for a given procedure.
	 */
	Set<ILocalProgramVar> getLocals(String procedurename);

	/**
	 * @return global constants;
	 */
	Set<IProgramConst> getConstants();

}
