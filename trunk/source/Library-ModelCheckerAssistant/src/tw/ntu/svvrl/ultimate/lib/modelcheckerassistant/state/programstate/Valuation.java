package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Valuation implements Cloneable {
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
			Map<String, Object> id2v = new HashMap<>(mValueMap.get(procName));
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
				Map<String, Object> id2v = new HashMap<>(mValueMap.get(procName));
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
	
	public boolean containsProcOrFunc(final String procOrFuncName) {
		return mValueMap.containsKey(procOrFuncName);
	}
	
	public boolean equals(final Valuation anotherValuation) {
		return mValueMap.equals(anotherValuation.mValueMap);
	}

	/**
	 * Check if all non-old global variables have been initialized.
	 * All non-old global variables should be initialized before doing 
	 * the transition in the never claim automata.
	 * @return true if all are initialized, false otherwise.
	 */
	public boolean allNonOldGlobalInitialized() {
		final Map<String, Object> globalVarMap = mValueMap.get(null);
		for(final String globalVarName : globalVarMap.keySet()) {
			boolean isOld = isOld(globalVarName);
		    if(!isOld && globalVarMap.get(globalVarName) == null) {
		    	return false;
		    }
		}
		return true;
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
	
	@Override
	public String toString() {
		return mValueMap.toString();
	}
}
