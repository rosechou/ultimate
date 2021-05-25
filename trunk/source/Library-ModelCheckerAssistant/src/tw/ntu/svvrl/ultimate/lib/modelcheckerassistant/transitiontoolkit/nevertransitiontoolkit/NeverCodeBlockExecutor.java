package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.CodeBlockExecutor;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsChecker;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;

public class NeverCodeBlockExecutor extends CodeBlockExecutor<NeverState> {
	/**
	 * Only for NeverClaim Automata.
	 */
	private final ProgramState mCorrespondingProgramState;
	
	/**
	 * For Never Claim Automata.
	 * @param codeBlock
	 * @param state
	 * @param autType
	 * @param correspondingProgramState
	 */
	public NeverCodeBlockExecutor(final CodeBlock codeBlock, final NeverState state
			, final ProgramState correspondingProgramState) {
		mCodeBlock = codeBlock;
		mCurrentState = state;
		mCorrespondingProgramState = correspondingProgramState;
	}
	

	public boolean checkEnable() {
		if(mCodeBlock instanceof StatementSequence) {
			List<Statement> stmts = ((StatementSequence) mCodeBlock).getStatements();
			final ProgramStatementsChecker statementChecker = new ProgramStatementsChecker(stmts, mCorrespondingProgramState);
			return statementChecker.checkStatementsEnable();
		} else {
			throw new UnsupportedOperationException("Suppose the CodeBlock Type: "
					+ mCodeBlock.getClass().getSimpleName() + " should not be in Never Claim"
							+ " Automata.");
		}
	}

}
