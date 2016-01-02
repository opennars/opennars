package automenta.vivisect.surfaceplotter;

import automenta.vivisect.surfaceplotter.surface.AbstractSurfaceModel;
import automenta.vivisect.surfaceplotter.surface.SurfaceVertex;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * {@link DefaultSurfaceModel} provides a simple way to fill the {@link AbstractSurfaceModel} using the Plotter interface.
 */
public class DefaultSurfaceModel extends AbstractSurfaceModel {

	

	private Mapper mapper;
	protected SurfaceVertex[][] surfaceVertex;
	/**
	 * Empty Surface Model
	 */
	public DefaultSurfaceModel() {
	}

	
	public SwingWorker<Void, Void> plot() {
		if ((xMin >= xMax) || (yMin >= yMax))
			throw new NumberFormatException();
		setDataAvailable(false); // clean space
		// reads the calcDivision that will be used
		float stepx = (xMax - xMin) / calcDivisions;
		float stepy = (yMax - yMin) / calcDivisions;
		float xfactor = 20 / (xMax - xMin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
		float yfactor = 20 / (yMax - yMin);
		
		int total = (calcDivisions + 1) * (calcDivisions + 1); // compute total size
                
                if ((surfaceVertex==null) || (surfaceVertex[0].length < total))
                    surfaceVertex = allocateMemory(hasFunction1,hasFunction2,  total); // allocate surfaceVertex
                
		
		// fill the surface surfaceVertex with NaN
		for (int i = 0; i <= calcDivisions; i++)
			for (int j = 0; j <= calcDivisions; j++) {
				int k = i * (calcDivisions + 1) + j;

				float x = xMin + i * stepx;
				float y = yMin + j * stepy;
				if (hasFunction1) 
					surfaceVertex[0][k] = new SurfaceVertex((x - xMin) * xfactor - 10, (y - yMin) * yfactor - 10, Float.NaN);
				if (hasFunction2) 
					surfaceVertex[1][k] = new SurfaceVertex((x - xMin) * xfactor - 10, (y - yMin) * yfactor - 10, Float.NaN);
			}
		
		
		setSurfaceVertex(surfaceVertex); // define as the current surfaceVertex
		setDataAvailable(true); // cause the JSurface to display an empty plot
		
		getProjector();

		
		return  new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				// fill the surface surfaceVertex with NaN
				setProgress(0);
				setProgress(1) ;
				for (int i = 0; i <= calcDivisions; i++)
					for (int j = 0; j <= calcDivisions; j++) {
						int k = i * (calcDivisions + 1) + j;

						float x = xMin + i * stepx;
						float y = yMin + j * stepy;
						if (hasFunction1) {
							float v1 = mapper.f1(x,y);
							if (Float.isInfinite(v1))
								v1 = Float.NaN;
							if (!Float.isNaN(v1)) {
								if (Float.isNaN(z1Max) || (v1 > z1Max))
									z1Max = v1;
								else if (Float.isNaN(z1Min) || (v1 < z1Min))
									z1Min = v1;
							}
							
							surfaceVertex[0][k] = new SurfaceVertex((x - xMin) * xfactor - 10, (y - yMin) * yfactor - 10, v1);
						}
						if (hasFunction2) {
							float v2 = mapper.f2(x, y);
							if (Float.isInfinite(v2))
								v2 = Float.NaN;
							if (!Float.isNaN(v2)) {
								if (Float.isNaN(z2Max) || (v2 > z2Max))
									z2Max = v2;
								else if (Float.isNaN(z2Min) || (v2 < z2Min))
									z2Min = v2;
							}
							
							surfaceVertex[1][k] = new SurfaceVertex((x - xMin) * xfactor - 10, (y - yMin) * yfactor - 10, v2);
						}
						publish();
						setProgress((100*k)/total);
					}
				setProgress(100);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				z1Min = (float) floor(z1Min, 2);
				z1Max = (float) ceil(z1Max, 2);
				z2Min = (float) floor(z2Min, 2);
				z2Max = (float) ceil(z2Max, 2);

				autoScale();
				fireStateChanged();
			}

			@Override
			protected void process(List<Void> chunks) {
				fireStateChanged();
			}
			
		};
		
		

	}

	
	/**
	 * Allocates Memory
	 */

	private SurfaceVertex[][] allocateMemory(boolean f1, boolean f2, int total) {
		SurfaceVertex[][] vertex = null;
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

	
	

	public Mapper getMapper() {
		return mapper;
	}


	public void setMapper(Mapper mapper) {
		getPropertyChangeSupport().firePropertyChange("mapper", this.mapper, this.mapper = mapper);
	}


	@Override
	public SurfaceVertex[][] getSurfaceVertex() {
		return surfaceVertex;
	}

	protected void setSurfaceVertex(SurfaceVertex[][] surfaceVertex) {
		getPropertyChangeSupport().firePropertyChange("surfaceVertex", this.surfaceVertex, this.surfaceVertex = surfaceVertex);
	}
}// end of class
