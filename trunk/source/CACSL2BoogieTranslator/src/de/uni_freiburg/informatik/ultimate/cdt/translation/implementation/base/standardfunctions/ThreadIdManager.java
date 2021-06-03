package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.standardfunctions;

import java.util.HashMap;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.AuxVarInfo;

public class ThreadIdManager {
	final Map<String, AuxVarInfo> mProcName2TmpThreadId = new HashMap<>();
	
	public ThreadIdManager() {
	}
	
	public void setThreadIdMapping(final String threadName, final AuxVarInfo tmpThreadId) {
		mProcName2TmpThreadId.put(threadName, tmpThreadId);
	}
	
	public AuxVarInfo getThreadId(final String threadName) {
		return mProcName2TmpThreadId.get(threadName);
	}
}
