package backEnd;

import java.io.Serializable;

public class PathType implements Serializable {
	private static final long serialVersionUID = 1L;
	public int x = 0;
	public int y = 0;
	
	public PathType() {

	};
	
	public PathType(int x, int y) {
		this.x = x;
		this.y = y;
	}
}