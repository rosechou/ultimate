package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;

public class CodeBlockExecutor {
	private final ExprEvaluator mExprEvaluator;
	
	public CodeBlockExecutor(ExprEvaluator exprEvaluator) {
		mExprEvaluator = exprEvaluator;
	}

	public ProgramState executeStatementSequence(StatementSequence stmtSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramState executeCall(Call call) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramState executeSummary(Summary summary) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramState executeReturn(Return returnn) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramState executeForkThreadCurrent(ForkThreadCurrent forkThreadCurrent) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramState executeForkThreadOther(ForkThreadOther forkThreadOther) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramState executeJoinThreadCurrent(JoinThreadCurrent joinThreadCurrent) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramState executeJoinThreadOther(JoinThreadOther joinThreadOther) {
		// TODO Auto-generated method stub
		return null;
	}



}
