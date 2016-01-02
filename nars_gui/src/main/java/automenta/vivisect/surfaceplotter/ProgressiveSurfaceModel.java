package automenta.vivisect.surfaceplotter;

import automenta.vivisect.surfaceplotter.surface.AbstractSurfaceModel;
import automenta.vivisect.surfaceplotter.surface.SurfaceVertex;

import javax.swing.*;
import java.util.List;

/**
 * {@link ProgressiveSurfaceModel} fills the surface with increasing "definition".
 * <p>
 * It allocate an HD array , and start with a single face, then 4, then 16 etc. until reaching the goal HD definition.
 * <p>
 * This defines the
 * 
 * <pre>
 * def
 * </pre>
 * 
 * variable: def=0 means 1 face, def=1 means 4 faces etc.
 */
public class ProgressiveSurfaceModel extends AbstractSurfaceModel {

	protected SurfaceVertex[][] highDefinitionVertex;
	protected SurfaceVertex[][] surfaceVertex;
	protected Mapper mapper;
	int currentDefinition = -1;
	int availableDefinition = -1;
	int maxDefinition = 6;

	/**
	 * Empty Surface Model
	 */
	public ProgressiveSurfaceModel() {
	}

	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	
	public SwingWorker<Void, Void> plot() {
		return plot(null);
	}
	
	public SwingWorker<Void, Void> plot(Runnable callback) {
		highDefinitionVertex = allocateMemory(hasFunction1, hasFunction2, maxDefinition);
		currentDefinition = -1;
		availableDefinition = -1;
		
		return new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				setProgress(0);
				while (maxDefinition > availableDefinition) {
					increaseDefinition();
					// Thread.sleep(1000);
					if (isCancelled()) return null;
					publish();
				}
				setProgress(100);
				return null;
			}

			@Override
			protected void process(List<Void> chunks) {
				setCurrentDefinition(availableDefinition);
			}

			/**
			 * computes all the vertices, and increase the current definition.
			 */
			private void increaseDefinition() throws Exception{
				int def = availableDefinition + 1; // increasing the def (but not offically to not propagate the consequences
				
				int max = 2+vertices(def)-vertices(def-1); // number of calls to compute
				int ci =1;
				setProgress(ci);
				// loop over all values BUT computes only for the ones in the current def
				
				int k = segments(maxDefinition) + 1;
				for (int i = 0; i < k; i++)
					for (int j = 0; j < k; j++)
						if (definition(i, j) == def){
							if (isCancelled()) return;
							compute(i, j);
							setProgress(( ci++ *100 )/ max ) ;
						}
				availableDefinition = def;

				z1Min = (float) floor(z1Min, 2);
				z1Max = (float) ceil(z1Max, 2);
				z2Min = (float) floor(z2Min, 2);
				z2Max = (float) ceil(z2Max, 2);
			}

			@Override protected void done() {
				if (callback !=null) callback.run();
			}
			
			
			
		};
	}

	/**
	 * creates a lower resolution surfaceVertex array
	 * 
	 * @param def
	 * @return
	 */
	private SurfaceVertex[][] extractResolution(int def) {
		SurfaceVertex[][] vertex = allocateMemory(hasFunction1, hasFunction2, def);
		int k = segments(def) + 1;

		for (int i = 0; i < k; i++)
			for (int j = 0; j < k; j++)
				// i, and j are in def coordinate
				copy(i, j, def, vertex);
		return vertex;
	}

	/**
	 * copy surfaceVertex from the highdefinition in coordinate i,j, in definition def, into the target
	 * 
	 * @param i
	 * @param j
	 * @param def
	 * @param vertex
	 */
	private void copy(int i, int j, int def, SurfaceVertex[][] vertex) {
		int offset = maxDefinition - def;
		int hi = i << offset;
		int hj = j << offset;
		int k = i * (segments(def) + 1) + j;
		int hk = hi * (segments(maxDefinition) + 1) + hj;

		vertex[0][k] = highDefinitionVertex[0][hk];
		vertex[1][k] = highDefinitionVertex[1][hk];

	}

	/**
	 * compute the point at i,j in high definition coordinate
	 * 
	 * @param i
	 * @param j
	 */
	private void compute(int i, int j) {

		int steps = segments(maxDefinition);
		float xWidth = xMax - xMin;
		float yWidth = yMax - yMin;

		float x = xMin + i * xWidth / steps;
		float y = yMin + j * yWidth / steps;
		// magic number ? no, 20 comes from the SurfaceVertex that requires values to be in [-10,10]
		float xfactor = 20 / (xWidth);
		float yfactor = 20 / (yWidth);
		int k = i * (steps + 1) + j;

		if (hasFunction1) {
			float f1 = mapper.f1(x, y);
			if (Float.isInfinite(f1))
				f1 = Float.NaN;
			if (!Float.isNaN(f1)) {
				if (Float.isNaN(z1Max) || (f1 > z1Max))
					z1Max = f1;
				else if (Float.isNaN(z1Min) || (f1 < z1Min))
					z1Min = f1;
			}

			highDefinitionVertex[0][k] = new SurfaceVertex((x - xMin) * xfactor - 10, (y - yMin) * yfactor - 10, f1);
		}
		if (hasFunction2) {
			float f2 = mapper.f2(x, y);

			if (Float.isInfinite(f2))
				f2 = Float.NaN;
			if (!Float.isNaN(f2)) {
				if (Float.isNaN(z2Max) || (f2 > z2Max))
					z2Max = f2;
				else if (Float.isNaN(z2Min) || (f2 < z2Min))
					z2Min = f2;
			}

			highDefinitionVertex[1][k] = new SurfaceVertex((x - xMin) * xfactor - 10, (y - yMin) * yfactor - 10, f2);
		}
	}

	/**
	 * Allocates Memory
	 */

	private SurfaceVertex[][] allocateMemory(boolean f1, boolean f2, int def) {
		SurfaceVertex[][] vertex = null;
		int total = vertices(def); // compute total size

		try {
			vertex = new SurfaceVertex[2][total];
			if (!f1)
				vertex[0] = null;
			if (!f2)
				vertex[1] = null;
		} catch (OutOfMemoryError e) {
			setMessage("Not enough memory");
		} catch (Exception e) {
			setMessage("Error: " + e);
		}
		return vertex;
	}

	/**
	 * return the definition this points belongs too.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public int definition(int i, int j) {
		// odd numbers always belongs to the last definition,
		// numberOfTrailingZeros of odd number is 0
		// therefore i belongs to the definition maxdef - numberOfTrailingZeros(i)
		// cave at, there is an exeception for 0, numberOfTrailingZeros is 64 but we think of it as "0"
		int offset = Math.min(Long.numberOfTrailingZeros(i), Long.numberOfTrailingZeros(j));
		return Math.max(0, maxDefinition - offset);
	}

	/**
	 * computes the number of faces in a given definition
	 * 
	 * @param def
	 * @return
	 */
	public static int faces(int def) {
		int segPerDim = segments(def);
		return segPerDim * segPerDim;
	}

	/**
	 * computes the number of segment per dimension in a given definition
	 * 
	 * @param def
	 * @return
	 */
	public static int segments(int def) {
		return 1 << def; // 2 ^ def +1
	}

	/**
	 * computes the number of vertices in a given definition
	 * 
	 * @param def
	 * @return
	 */
	public static int vertices(int def) {
		int dotsPerDim = segments(def) + 1;
		return dotsPerDim * dotsPerDim;
	}

	@Override
	public SurfaceVertex[][] getSurfaceVertex() {
		return surfaceVertex;
	}

	protected void setSurfaceVertex(SurfaceVertex[][] surfaceVertex) {
		getPropertyChangeSupport().firePropertyChange("surfaceVertex", this.surfaceVertex, this.surfaceVertex = surfaceVertex);
	}

	public int getCurrentDefinition() {
		return currentDefinition;
	}

	/**
	 * called only when data are available in the given definition, this causes a lot of changes
	 * 
	 * @param currentDefinition
	 */
	public void setCurrentDefinition(int currentDefinition) {
		assert currentDefinition <= maxDefinition && currentDefinition <= availableDefinition : "cannot change definition higher than " + maxDefinition;

		SurfaceVertex[][] vertex = extractResolution(currentDefinition);
		// extract a new surfaceVertex on the given definition, and pass it to the setSurfaceVertex method
		getPropertyChangeSupport().firePropertyChange("currentDefinition", this.currentDefinition, this.currentDefinition = currentDefinition);
		int step = segments(currentDefinition);
		setSurfaceVertex(vertex);
		setCalcDivisions(step);
		setDispDivisions(step);
		if (currentDefinition >= 0)
			setDataAvailable(true);
		autoScale();
		fireStateChanged();
	}

	public int getMaxDefinition() {
		return maxDefinition;
	}

	public void setMaxDefinition(int maxDefinition) {
		getPropertyChangeSupport().firePropertyChange("maxDefinition", this.maxDefinition, this.maxDefinition = maxDefinition);
	}

	
	
	
}// end of class
