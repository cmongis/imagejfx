package knop.ij2plugins.mser;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;



import mpicbg.imglib.cursor.LocalizableByDimCursor;

import mpicbg.imglib.image.Image;

import mpicbg.imglib.type.numeric.RealType;

public class MSER<T extends RealType<T>> {

	/*
	 * Parameters
	 */

	private int delta;

	// minimum and maximum size of connected components
	private int minArea;
	private int maxArea;

	// maximum variation and minumum diversity for a stable region
	private double maxVariation;
	private double minDiversity;

	/*
	 * Region tree representation
	 */
	private HashSet<Region> topMsers;

	/*
	 * Internal Data
	 */

	// the image to process
	private Image<T> image;
	// an image with all found regions
	private Image<T> regions;
	private int size;
	private int[] dimensions;
	private int numDimensions;
	private int[] values;
	private int neighbors;
	private int[] neighborOffsets;
	private int[][] neighborRelPositions;

	// true for each visited pixel
	private boolean[] visited;

	// the next nextNeighbors to explore for each pixel
	private int[] nextNeighbors;

	// indices of the next and previous image position in a linked list
	private int[] prev;
	private int[] next;
	private final static int NONE = -1;

	// all connected components
	private Vector<ConnectedComponent> components;
	private int curComponent = 0;

	// all histories
	private Vector<GrowHistory> histories;
	private int curHistory = 0;

	// boundary pixel stacks (for each possible value)
	private Vector<Stack<Integer>> stacks;

	private class OffsetsPostions {

		public int[] offsets;
		public int[][] positions;
	}

	private class GrowHistory {

		public GrowHistory shortcut;
		public GrowHistory child;

		// the size of the component when it was stable, zero if the component
		// was never stable
		public int stableSize;
		// the current gray value of the component
		public int value;
		// the current size of the component
		public int size;
	}

	private class ConnectedComponent {

		public int head;
		public int tail;

		public GrowHistory history;

		public int value;
		public int size;
		public double[] center;
		public boolean varChanged;
		public double variation;

		private Vector<Region> childRegions;

		public ConnectedComponent(int value) {

			this.value = value;
			size = 0;
			center = new double[numDimensions];
			variation = 0.0;
			varChanged = true;
			history = null;
			childRegions = new Vector<Region>();
		}

		public void addHistory(GrowHistory newHistory) {

			newHistory.child = newHistory;

			if (history == null) {

				newHistory.shortcut = newHistory;
				newHistory.stableSize = 0;
			} else {

				history.child = newHistory;
				newHistory.shortcut = history.shortcut;
				newHistory.stableSize = history.stableSize;
			}

			newHistory.value = value;
			newHistory.size = size;
			history = newHistory;
		}

		public void merge(ConnectedComponent other, GrowHistory newHistory) {

			// TODO: Why is that always the other one?
			this.value = other.value;

			newHistory.child = newHistory;

			ConnectedComponent bigger;
			ConnectedComponent smaller;

			// find the bigger region
			if (this.size >= other.size) {

				bigger = this;
				smaller = other;
			} else {

				bigger = other;
				smaller = this;
			}

			if (bigger.history == null) {

				newHistory.shortcut = newHistory;
				newHistory.stableSize = 0;
			} else {

				bigger.history.child = newHistory;
				newHistory.shortcut = bigger.history.shortcut;
				newHistory.stableSize = bigger.history.stableSize;
			}

			if (smaller.history != null
					&& smaller.history.stableSize > newHistory.stableSize)
				newHistory.stableSize = smaller.history.stableSize;

			newHistory.value = bigger.value;
			newHistory.size = bigger.size;

			this.variation = bigger.variation;
			this.varChanged = bigger.varChanged;

			// merge pixel sets
			if (bigger.size > 0 && smaller.size > 0) {

				next[bigger.tail] = smaller.head;
				prev[smaller.head] = bigger.tail;
			}

			int newHead = (bigger.size > 0) ? bigger.head : smaller.head;
			int newTail = (smaller.size > 0) ? smaller.tail : bigger.tail;

			// update center position
			double thisCenterWeight = (double) (this.size)
					/ (this.size + other.size);

			for (int d = 0; d < numDimensions; d++)
				this.center[d] = thisCenterWeight * this.center[d]
						+ (1.0 - thisCenterWeight) * other.center[d];

			this.head = newHead;
			this.tail = newTail;
			this.history = newHistory;
			this.size = this.size + other.size;

			// update children
			this.addChildRegions(other.getChildRegions());
		}

		public double calculateVariation() {

			if (history != null) {

				GrowHistory shortcut = history.shortcut;
				while (shortcut != shortcut.shortcut
						&& shortcut.value + delta > value)
					shortcut = shortcut.shortcut;

				GrowHistory child = shortcut.child;
				while (child != child.child && child.value + delta <= value) {
					shortcut = child;
					child = child.child;
				}

				// store the shortcut for later calls
				history.shortcut = shortcut;

				// NOTE: this is not how it is proposed in the paper! this is
				// the opencv version:
				//
				// OpenCV: |R_{i} - R_{i-delta}|/|R_{i-delta}|
				// Paper : |R_{i+delta} - R_{i-delta}|/|R_{i}|
				return (double) (size - shortcut.size) / (double) shortcut.size;
			}

			return 1.0;
		}

		public boolean isStable() {

			if (history == null || history.size <= minArea
					|| history.size >= maxArea)
				return false;

			double div = (double) (history.size - history.stableSize)
					/ (double) history.size;
			double var = calculateVariation();

			// change in variation?
			boolean dvar = (variation < var || history.value + 1 < value);
			boolean isStable = (dvar && !varChanged && variation < maxVariation && div > minDiversity);

			variation = var;
			varChanged = dvar;

			if (isStable)
				history.stableSize = history.size;

			return isStable;
		}

		public void addPosition(int index) {

			if (size > 0) {

				prev[index] = tail;
				next[tail] = index;
				next[index] = NONE;
			} else {

				prev[index] = NONE;
				next[index] = NONE;
				head = index;
			}

			tail = index;
			size++;

			// update center
			// TODO: find less expensive calculation
			int[] position = new int[numDimensions];
			indexToPosition(index, position);
			double weight = 1.0 / (double) (size);
			for (int d = 0; d < numDimensions; d++)
				center[d] = (1.0 - weight) * center[d] + weight * position[d];
		}

		public Vector<Region> getChildRegions() {

			return childRegions;
		}

		public void setChildRegion(Region child) {

			childRegions.clear();
			childRegions.add(child);
		}

		public void addChildRegions(Vector<Region> children) {

			childRegions.addAll(children);
		}
	}

	/**
	 * @param dimensions
	 *            The dimensions of the image(s) to process.
	 * @param delta
	 *            Number of gray levels to consider for variation calculation
	 * @param minArea
	 *            Minimum size of a connected component
	 * @param maxArea
	 *            Maximum size of a connected component
	 * @param maxVariation
	 *            Maximum variation for a component to be considered stable
	 * @param minDiversity
	 *            Minimum diversity for a component to be considered stable
	 */
	public MSER(int[] dimensions, int delta, int minArea, int maxArea,
			double maxVariation, double minDiversity) {

		this.dimensions = dimensions;
		numDimensions = dimensions.length;
		size = 1;
		for (int d : dimensions)
			size *= d; // = 1*w*h*channels*slices*frames

		OffsetsPostions op = createIndexOffsets(dimensions);
		neighborOffsets = op.offsets;
		neighborRelPositions = op.positions;
		neighbors = op.offsets.length;

		// allocate memory
		values = new int[size];
		visited = new boolean[size];
		prev = new int[size];
		next = new int[size];

		nextNeighbors = new int[size];

		// TODO: preallocate stacks
		stacks = new Vector<Stack<Integer>>(256);
		stacks.setSize(256);
		histories = new Vector<GrowHistory>(size);
		histories.setSize(size);
		components = new Vector<ConnectedComponent>(257);
		components.setSize(257);

		topMsers = new HashSet<Region>();

		setParameters(delta, minArea, maxArea, maxVariation, minDiversity);
	}

	/**
	 * @param delta
	 *            Number of gray levels to consider for variation calculation
	 * @param minArea
	 *            Minimum size of a connected component
	 * @param maxArea
	 *            Maximum size of a connected component
	 * @param maxVariation
	 *            Maximum variation for a component to be considered stable
	 * @param minDiversity
	 *            Minimum diversity for a component to be considered stable
	 */
	public void setParameters(int delta, int minArea, int maxArea,
			double maxVariation, double minDiversity) {

		this.delta = delta;
		this.minArea = minArea;
		this.maxArea = maxArea;
		this.maxVariation = maxVariation;
		this.minDiversity = minDiversity;
	}

	public void process(Image<T> image, boolean darkToBright,
			boolean brightToDark) {

		process(image, darkToBright, brightToDark, null);
	}

	public void process(Image<T> image, boolean darkToBright,
			boolean brightToDark, Image<T> regions) {

		this.regions = regions;

		topMsers.clear();
		// Hashset of Region

		// copy image data
		LocalizableByDimCursor<T> cursor = image.createLocalizableByDimCursor();
		// cursor <=> pixel number of the image
		while (cursor.hasNext()) {
			cursor.fwd();
			values[cursor.getArrayIndex()] = (int) cursor.getType()
					.getRealFloat();
			// values = new int[size]
		}

		if (darkToBright) {
			setupBuffers();
			process(true);
		}

		if (brightToDark) {
			setupBuffers();
			process(false);
		}
	}

	/**
	 * Returns the root element of the region tree.
	 * 
	 * @return The root element of the region tree
	 */
	public HashSet<Region> getTopMsers() {
		return topMsers;
	}

	private void setupBuffers() {
		// Initialize everything
		for (int i = 0; i < size; i++) {
			visited[i] = false;
			nextNeighbors[i] = 0;
			histories.set(i, new GrowHistory());
		}

		for (int i = 0; i < 256; i++)
			stacks.set(i, new Stack<Integer>());

		curComponent = 0;
		curHistory = 0;
	}

	private void process(boolean darkToBright) {

            System.out.println("Processing from " 
				+ (darkToBright ? "dark to bright" : "bright to dark"));

		// add dummy component
		components.set(curComponent, new ConnectedComponent(256));
		  // components: vector<ConnectedComponents>
		  // .set(curElementToReplace, newElement
		curComponent++; // Current component

		int curIndex = 0;
		int[] curPosition = new int[numDimensions];
		int[] neighborPosition = new int[numDimensions];
		int curValue = (darkToBright ? values[curIndex]
				: 255 - values[curIndex]);

		components.set(curComponent, new ConnectedComponent(curValue));

		visited[curIndex] = true;

		int curStack = curValue;

		int progress = 0;
		while (true) {

			//IJ.showProgress(progress, size);
			while (nextNeighbors[curIndex] < neighbors) {

				// don't run out of the image
				if (!addPosition(curPosition,
						neighborRelPositions[nextNeighbors[curIndex]],
						neighborPosition)) {
					nextNeighbors[curIndex]++;
					continue;
				}

				int neighborIndex = curIndex
						+ neighborOffsets[nextNeighbors[curIndex]];

				if (!visited[neighborIndex]) {

					visited[neighborIndex] = true;
					progress++;

					int neighborValue = (darkToBright ? values[neighborIndex]
							: 255 - values[neighborIndex]);

					// neighbor value smaller than current value?
					if (neighborValue < curValue) {

						// add current pixel to bundary heap and continue
						// processing the neighbor pixel instead
						stacks.get(curStack).push(curIndex);

						// done with this neighbor
						nextNeighbors[curIndex]++;

						// continue with neighbor pixel
						curStack = neighborValue;
						curIndex = neighborIndex;
						curValue = neighborValue;
						System.arraycopy(neighborPosition, 0, curPosition, 0,
								numDimensions);

						// create a new connected component for it
						curComponent++;
						components.set(curComponent, new ConnectedComponent(
								curValue));

						continue;

						// neighbor value equal or bigger than current value
					} else {

						// add the neighbor pixel to the boundary heap
						stacks.get(neighborValue).push(neighborIndex);
					}
				} // alrady visited

				nextNeighbors[curIndex]++;
			}

			// add current pixel to current connected component
			components.get(curComponent).addPosition(curIndex);

			// get the next pixel from the boundary heap
			if (!stacks.get(curStack).empty()) {

				curIndex = stacks.get(curStack).pop();
				curValue = (darkToBright ? values[curIndex]
						: 255 - values[curIndex]);
				indexToPosition(curIndex, curPosition);
			} else {

				// consider boundary pixels of next value
				curStack++;

				// for that, find next non-empty boundary heap
				int nextValue = 0;
				for (int i = (darkToBright ? values[curIndex]
						: 255 - values[curIndex]) + 1; i < 256; i++) {

					if (!stacks.get(curStack).empty()) {

						nextValue = i;
						break;
					}
					curStack++;
				}

				// there was a next non-empty heap
				if (nextValue != 0) {

					curIndex = stacks.get(curStack).pop();
					curValue = (darkToBright ? values[curIndex]
							: 255 - values[curIndex]);
					indexToPosition(curIndex, curPosition);

					if (nextValue < components.get(curComponent - 1).value) {

						// check for stability of the current component
						if (components.get(curComponent).isStable())
							addMser(components.get(curComponent));

						components.get(curComponent).addHistory(
								histories.get(curHistory));
						components.get(curComponent).value = nextValue;
						curHistory++;
					} else {

						// merge the latest two components until their gray
						// value >= than nextValue
						while (true) {

							components.get(curComponent - 1).merge(
									components.get(curComponent),
									histories.get(curHistory));
							curHistory++;
							curComponent--;

							if (components.get(curComponent).value >= nextValue)
								break;
							if (components.get(curComponent - 1).value > nextValue) {

								// check for stability of the current component
								if (components.get(curComponent).isStable())
									addMser(components.get(curComponent));

								components.get(curComponent).addHistory(
										histories.get(curHistory));
								components.get(curComponent).value = nextValue;
								curHistory++;

								break;
							}
						}
					}
				} else
					// no next non-empty heap
					break;
			} // current heap empty
		} // while (true)

		//IJ.showProgress(1.0);
		
	} // process(boolean)

	/**
	 * Creates the index offsets and position offsets for the neighbors of a
	 * pixel.
	 */
	private final OffsetsPostions createIndexOffsets(int[] dimensions) {

		int numDimensions = dimensions.length;
		int[] dimensionOffset = new int[numDimensions];
		for (int d = 0; d < numDimensions; d++) {
			int offset = 1;
			for (int e = 0; e < d; e++)
				offset *= dimensions[e];
			// = 1*w * w*h * w*h*nbChannel * w*h*nbChannel*nbSlices
			// * w*h*nbChannel*nbSlices*nbFrames

			dimensionOffset[d] = offset;
		}

		int numOffsets = numDimensions * 2;
		OffsetsPostions op = new OffsetsPostions();
		op.offsets = new int[numOffsets];
		op.positions = new int[numOffsets][numDimensions];

		for (int d = 0; d < numDimensions; d++) {

			op.offsets[2 * d] = dimensionOffset[d];
			op.offsets[2 * d + 1] = -dimensionOffset[d];

			op.positions[2 * d][d] = 1;
			op.positions[2 * d + 1][d] = -1;
		}

		return op;
	}

	private void addMser(ConnectedComponent component) {

		visualizeMser(component);

		Region newRegion = new Region(component.size, component.center);

		for (Region child : component.getChildRegions()) {
			topMsers.remove(child);
			child.setParent(newRegion);
		}
		topMsers.add(newRegion);

		newRegion.setChildren(component.getChildRegions());

		component.setChildRegion(newRegion);
	}

	private void visualizeMser(ConnectedComponent component) {

		if (regions == null)
			return;

		int index = component.head;
		int[] position = new int[numDimensions];
		LocalizableByDimCursor<T> cursor = regions
				.createLocalizableByDimCursor();
		while (next[index] != index && next[index] != NONE) {

			indexToPosition(index, position);
			cursor.setPosition(position);

			cursor.getType().setReal(cursor.getType().getRealFloat() + 10.0);

			index = next[index];
		}
	}

	/**
	 * Adds two positions.
	 * 
	 * @param position
	 *            The original position
	 * @param delta
	 *            The value to add to the position
	 * @param result
	 *            The resulting position
	 * @return true, if delta can be added to index without leaving the image
	 */
	private final boolean addPosition(int[] position, int[] delta, int[] result) {

		for (int d = 0; d < numDimensions; d++) {
			result[d] = position[d] + delta[d];
			if (result[d] < 0 || result[d] >= dimensions[d])
				return false;
		}
		return true;
	}

	private final void indexToPosition(int index, int[] position) {

		int prod = 1;

		for (int d = 0; d < numDimensions; d++) {

			position[d] = (index / prod) % dimensions[d];
			prod *= dimensions[d];
		}
	}

	private String arrayString(double[] array) {
		String ret = "";
		for (int d = 0; d < array.length; d++)
			ret += " " + array[d];
		return ret;
	}
}