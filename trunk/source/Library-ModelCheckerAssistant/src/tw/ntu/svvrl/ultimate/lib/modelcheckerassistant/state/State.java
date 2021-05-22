package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.List;




public interface State<S extends State<S, Trans>, Trans> {
	public List<Trans> getEnableTrans();
	public S doTransition(Trans t);
}
