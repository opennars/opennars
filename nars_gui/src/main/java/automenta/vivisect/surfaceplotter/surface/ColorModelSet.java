package automenta.vivisect.surfaceplotter.surface;

import automenta.vivisect.surfaceplotter.surface.SurfaceModel.PlotColor;
import automenta.vivisect.surfaceplotter.surface.SurfaceModel.PlotType;

import java.awt.*;


/** A simple {@link SurfaceColor} implementations that uses two ColorMode per plot type. 

 * @author Eric
 * @date jeudi 8 avril 2004 15:45:40
 */
public class ColorModelSet implements SurfaceColor
{
	
	public static float RED_H=0.941896f;
	public static float RED_S=0.7517241f;
	public static float RED_B=0.5686275f;
	
	public static float GOLD_H=0.1f;
	public static float GOLD_S=0.9497207f;
	public static float GOLD_B=0.7019608f;
	
	
	protected ColorModel dualshade;
	protected ColorModel grayscale;
	protected ColorModel spectrum;
	protected ColorModel fog;
	protected ColorModel opaque;

	protected ColorModel alt_dualshade;
	protected ColorModel alt_grayscale;
	protected ColorModel alt_spectrum;
	protected ColorModel alt_fog;
	protected ColorModel alt_opaque;
	
	protected Color lineColor=Color.DARK_GRAY;
	protected  Color lineboxColor=Color.getHSBColor(0.0f, 0.0f,0.5f);
	protected  Color lightColor=Color.WHITE;
	// Color(192,220,192); existing
	protected  Color boxColor=Color.getHSBColor(0.0f, 0.0f,0.95f);//Color.getHSBColor(226f/240f,145f/240f,1f);
	
	public ColorModelSet()
	{
		/*
		float[] f = Color.RGBtoHSB(255,255,255, new float[4]);
		System.out.print("DP_RED=(");
		for (int i=0;i<3;i++) { System.out.println((i==0?"":",")+f[i]);}
		*/
		
		dualshade= new ColorModel(		ColorModel.DUALSHADE,	RED_H	,	RED_S	,	RED_B	,	0.4f	, 1.0f);
		grayscale= new ColorModel(		ColorModel.DUALSHADE, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		spectrum= new ColorModel(		ColorModel.SPECTRUM	, 0.0f, 1.0f, 1.0f, 0.0f, 0.6666f);
		fog= new ColorModel(			ColorModel.FOG		,	RED_H	,	RED_S	,	RED_B	, 0.0f, 1.0f);
		opaque= new ColorModel(			ColorModel.OPAQUE	,	RED_H	,	0.1f	, 1.0f, 0.0f, 0.0f);

		
		
		alt_dualshade= new ColorModel(	ColorModel.DUALSHADE,	GOLD_H	,	GOLD_S	,	GOLD_B	,	0.4f	, 1.0f);
		alt_grayscale= new ColorModel(	ColorModel.DUALSHADE, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		alt_spectrum= new ColorModel(	ColorModel.SPECTRUM	, 0.0f, 1.0f,	0.8f	, 0.0f, 0.6666f);
		alt_fog= new ColorModel(		ColorModel.FOG		,	GOLD_H	, 0.0f,	GOLD_B	, 0.0f, 1.0f);
		alt_opaque= new ColorModel(		ColorModel.OPAQUE	,	GOLD_H	,	0.1f	, 1.0f, 0.0f, 0.0f);
		
	}
	
	protected PlotColor color_mode= PlotColor.SPECTRUM;
	public void setPlotColor(PlotColor v)	
	{
		color_mode =v;
	}
	protected PlotType plot_mode= PlotType.CONTOUR;
	public void setPlotType(PlotType type)
	{
		plot_mode =type;
	}
	
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getBackgroundColor()
	 */
	@Override
	public Color getBackgroundColor(){return lightColor;}
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getLineBoxColor()
	 */
	@Override
	public Color getLineBoxColor() {return lineboxColor;}
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getBoxColor()
	 */
	@Override
	public Color getBoxColor() {return boxColor;}
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getLineColor()
	 */
	@Override
	public Color getLineColor(){return lineColor;}
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getTextColor()
	 */
	@Override
	public Color getTextColor() {return lineColor;}
	
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getLineColor(int, float)
	 */
	@Override
	public Color getLineColor(int curve, float z)
	{
		return Color.BLACK;
		//return Color.BLUE;
		/**
		if (plot_mode==PlotType.WIREFRAME)
		{
			return Color.BLACK;
		}
		return getPolygonColor(curve, 1-z);
		/*
		if (
			color_mode==PlotColor.GRAYSCALE	|| 
			color_mode==PlotColor.SPECTRUM||
			color_mode==PlotColor.DUALSHADE)
		return grayscale.getPolygonColor(1-z);
		else return Color.DARK_GRAY;
		*/
		/*
		Color c= getPolygonColor(curve, z);
		float[] f= c.getComponents(new float[4]);
		float ff=f[2];
		if (ff<0.5f) ff=1f;
		if (ff<0) ff++;
		*/
		//return Color.getHSBColor(f[0],f[1],ff);
		/**/
	}
	
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getPolygonColor(int, float)
	 */
	@Override
	public Color getPolygonColor(int curve, float z)
	{
		if (curve==1) return getFirstPolygonColor(z);
		if (curve==2) return getSecondPolygonColor(z);
		return Color.blue;
	}
	
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getFirstPolygonColor(float)
	 */
	@Override
	public Color getFirstPolygonColor(float z)
	{
		//contour,density  plot does not fit with opaque color 
		if(plot_mode==PlotType.CONTOUR ||plot_mode==PlotType.DENSITY)
		{
			if (color_mode==PlotColor.OPAQUE)
				return dualshade.getPolygonColor(z);
		}
		
		switch ( color_mode)
		{
		case OPAQUE	:return opaque.getPolygonColor(z);
		case GRAYSCALE	:return grayscale.getPolygonColor(z);
		case SPECTRUM	:return spectrum.getPolygonColor(z);
		case DUALSHADE	:return dualshade.getPolygonColor(z);
		case FOG		:return fog.getPolygonColor(z);
		default: return Color.blue;
		}
	}
	
	/* (non-Javadoc)
	 * @see net.ericaro.surfaceplotter.SurfaceColor#getSecondPolygonColor(float)
	 */
	@Override
	public Color getSecondPolygonColor(float z)
	{
		//contour,density  plot does not fit with opaque color 
		if(plot_mode==PlotType.CONTOUR ||plot_mode==PlotType.DENSITY)
		{
			if (color_mode==PlotColor.OPAQUE)
				return alt_dualshade.getPolygonColor(z);
		}
		switch ( color_mode)
		{
		case OPAQUE	:return alt_opaque.getPolygonColor(z);
		case GRAYSCALE	:return alt_grayscale.getPolygonColor(z);
		case SPECTRUM	:return alt_spectrum.getPolygonColor(z);
		case DUALSHADE	:return alt_dualshade.getPolygonColor(z);
		case FOG		:return alt_fog.getPolygonColor(z);
		default: return Color.blue;
		}
	}
	
	/*
	protected float dualshadeColorFirstHue=0.2f;//0.7f;//0.1f;//0.941f; // first curve hue color
	protected float dualshadeColorSecondHue=0.7f;
	protected float dualshadeSaturation = 0.9125f;//0.604f; // 
	protected float dualshadeOffset=0.3f;
	protected float whiteblack = 0.3f;
	
	
	*/
	
}//end of class
