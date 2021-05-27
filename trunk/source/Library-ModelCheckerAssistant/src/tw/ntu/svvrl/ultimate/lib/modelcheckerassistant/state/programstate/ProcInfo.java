package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

/**
 * Keep the needed information on the procedure stack.
 * Including procedure names and their valuation copy when 
 * executing {@link CallStatement} and {@link Return}.
 */
public class ProcInfo {
	private final String mProcName;
	private Valuation mValuationRecord;
	
	/**
	 * <code> mValuationRecord </code> is initialized with null.
	 * Once the {@link CallStatement} is invoked, the valuation is recorded.
	 */
	public ProcInfo(final String procName) {
		mProcName = procName;
		mValuationRecord = null;
	}
	
	public String getProcName() {
		return mProcName;
	}
	
	public Valuation getValuationRecord() {
		return mValuationRecord;
	}
	
	public void setValuationRecord(Valuation v) {
		mValuationRecord = v;
	}
}
