package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.List;


public interface State<S extends State<S>> {
	public boolean equals(S anotherS);
}
