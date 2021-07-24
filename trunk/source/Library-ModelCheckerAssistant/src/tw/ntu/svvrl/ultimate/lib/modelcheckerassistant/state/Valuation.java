package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.multipebble.IFullMultipebbleAuxiliaryGameState.AuxiliaryGameStateType;

public class Valuation implements Cloneable {
	/**
	 * Procedure Name -> Variable Name -> Value
	 */
	private Map<String, Map<String, Object>> mValueMap = new HashMap<>();
	
	public Valuation() {
	}
	
	public Valuation(Map<String, Map<String, Object>> valueMap) {
		mValueMap = valueMap;
	}
	
	/**
	 * deep copy all variables.
	 */
	@Override
	public Valuation clone()
    {
		final Map<String, Map<String, Object>> val = new HashMap<>();
		for(String procName : mValueMap.keySet()) {
			final Map<String, Object> id2v = new HashMap<>(mValueMap.get(procName));
			val.put(procName, id2v);
		}
		return new Valuation(val);
    }
	
	/**
	 * deep copy locals and shallow copy globals.
	 */
	public Valuation cloneLocals()
    {
		final Map<String, Map<String, Object>> val = new HashMap<>();
		val.put(null, mValueMap.get(null));
		for(String procName : mValueMap.keySet()) {
			if(procName != null) {
				final Map<String, Object> id2v = new HashMap<>(mValueMap.get(procName));
				val.put(procName, id2v);
			}
		}
		return new Valuation(val);
    }

	
	/**
	 * Update the value map.
	 * @param procOrFuncName
	 * @param varName
	 * @param value
	 */
	public void setValue(final String procOrFuncName, final String varName, final Object value) {
		final Map<String, Object> id2v = new HashMap<>();
		id2v.put(varName, value);

		if(mValueMap.containsKey(procOrFuncName)) {
			if(mValueMap.get(procOrFuncName).containsKey(varName)) {
				mValueMap.get(procOrFuncName).replace(varName, value);
			}
			else {
				mValueMap.get(procOrFuncName).putAll(id2v);
			}
		} else {
			mValueMap.put(procOrFuncName, id2v);
		}
	}
	
	public final Set<String> getProcOrFuncNames() {
		return mValueMap.keySet();
	}
	
	public Map<String, Object> getProcOrFuncId2V(String procOrFunName) {
		return mValueMap.get(procOrFunName);
	}
	
	public final Object lookUpValue(final String procOrFuncName, String varName) {
		return mValueMap.get(procOrFuncName).get(varName);
	}
	
	/**
	 * make globalValuation reference.
	 * Once the globals change, the linked valuation also changes. 
	 */
	public void linkGlobals(final Valuation globalValuation) {
		mValueMap.remove(null);
		mValueMap.putAll(globalValuation.mValueMap);
	}
	
	public void resetLocals(final Valuation v) {
		Map<String, Object> globalId2v = mValueMap.get(null);
		mValueMap.putAll(v.mValueMap);
		mValueMap.put(null, globalId2v);
	}
	
	public boolean containsProcOrFunc(final String procOrFuncName) {
		return mValueMap.containsKey(procOrFuncName);
	}
	
	/**
	 * Two valuations are equivalent if the non-old variables has the same values.
	 */
	public boolean equals(final Valuation anotherValuation) {
		final Valuation v1 = this.getRemoveOldsValuation();
		final Valuation v2 = anotherValuation.getRemoveOldsValuation();
		return v1.mValueMap.equals(v2.mValueMap);
	}
	
	/**
	 * Copy the original value map and remove old variables.
	 * Use the result to construct a new valuation.
	 */
	private Valuation getRemoveOldsValuation() {
		final Map<String, Map<String, Object>> val = new HashMap<>();
		for(String procName : mValueMap.keySet()) {
			final Map<String, Object> id2v = new HashMap<>(mValueMap.get(procName));
			final Map<String, Object> removeOldId2v = new HashMap<>();
			for(final String varName : id2v.keySet()) {
				if(!isOld(varName)) {
					removeOldId2v.put(varName, id2v.get(varName));
				}
			}
			val.put(procName, removeOldId2v);
		}
		return new Valuation(val);
	}

	/**
	 * Check if all non-old global variables and non-auxiliary variables
	 *  have been initialized.
	 * They should be initialized before doing 
	 * the transition in the never claim automata.
	 * @return true if all are initialized, false otherwise.
	 */
	public boolean allNonOldNonAuxGlobalInitialized() {
		final Map<String, Object> globalVarMap = mValueMap.get(null);
		for(final String globalVarName : globalVarMap.keySet()) {
			boolean isOld = isOld(globalVarName);
			boolean isAux = isAux(globalVarName);
			boolean isNull = isNull(globalVarMap.get(globalVarName));
		    if(!isOld && !isAux && isNull) {
		    	return false;
		    }
		}
		return true;
	}

	private boolean isNull(Object v) {
		if(v instanceof Object[]) {
			return isNull(((Object[]) v)[0]);
		} else {
			return v ==null;
		}
	}

	/**
	 * Check the variable whose name is old or not.
	 * If it is old variable, then it must begin with "old(" 
	 * and end with ")".
	 */
	public boolean isOld(String s) {
		if(s.length() <= 5) {
			return false;
		} else {
			if(s.startsWith("old(") && s.endsWith(")")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check the variable is a auxiliary variable or not (in Boogie).
	 */
	public boolean isAux(String s) {
		if(s.startsWith("#")) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return mValueMap.toString();
	}
}
