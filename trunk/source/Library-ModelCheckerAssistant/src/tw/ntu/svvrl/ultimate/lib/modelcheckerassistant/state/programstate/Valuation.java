package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Valuation implements Cloneable {
	private final Map<String, Map<String, Object>> mValueMap = new HashMap<>();
	
	public Valuation() {
	}
	
	public Valuation(Map<String, Map<String, Object>> valueMap) {
		mValueMap.putAll(valueMap);
	}
	
	/**
	 * deep copy
	 */
	@Override
	public Valuation clone()
    {
		Map<String, Map<String, Object>> val = new HashMap<>();
		for(String procName : mValueMap.keySet()) {
			Map<String, Object> id2v = new HashMap<>(mValueMap.get(procName));
			val.put(procName, id2v);
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
			mValueMap.get(procOrFuncName).putAll(id2v);
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
	
	public boolean containsProcOrFunc(final String procOrFuncName) {
		return mValueMap.containsKey(procOrFuncName);
	}
	
	public boolean equals(final Valuation anotherValuation) {
		return mValueMap.equals(anotherValuation.mValueMap);
	}

	public boolean allNonOldGlobalInitialized() {
		Map<String, Object> globalVarMap = mValueMap.get(null);
		for(String globalVarName : globalVarMap.keySet()) {
			boolean isOld = isOld(globalVarName);
		    if(!isOld && globalVarMap.get(globalVarName) == null) {
		    	return false;
		    }
		}
		return true;
	}

	private boolean isOld(String s) {
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
