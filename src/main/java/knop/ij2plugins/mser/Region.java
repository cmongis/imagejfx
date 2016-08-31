package knop.ij2plugins.mser;

import java.util.Vector;

public class Region {

	public Region         parent;
	public Vector<Region> children;

	public int      size;
	public double[] center;

	public Region(int size, double[] center) {

		this.size     = size;
		this.center   = new double[center.length];
		System.arraycopy(center, 0, this.center, 0, center.length);
		this.parent   = null;
		this.children = new Vector<Region>();
	}

	public void setParent(Region parent) {

		this.parent = parent;
	}

	public void setChildren(Vector<Region> children) {

		this.children.addAll(children);
	}

	public String toString() {

		String ret = "Region at ";

		for (int d = 0; d < center.length; d++)
			ret += " " + center[d];

		ret += ", size: " + size;

		return ret;
	}
}
