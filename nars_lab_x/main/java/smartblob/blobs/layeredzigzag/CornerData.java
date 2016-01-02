/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.blobs.layeredzigzag;

/** TODO similar to TriData.
Maybe I'll put distance constraints here between the up to 6 adjacent TriData
or up to 6 adjacent CornerData. */
public class CornerData extends Adjacent{
	
	public final LayeredZigzag smartblob;

	public final Corner corner;

	public float y, x, speedX, speedY;

	/** addtoX and addToY are similar to speed vars in that position is updated using them
	at the same time as speed vars (counts as isUpdatingSpeed) but are different in that they
	add directly to position and then are set to 0, as a 1 time thing during bounce calculations.
	*/
	public float addToX, addToY;

	/** This may be ignored, since TriData color is the main thing thats drawn.
	This would be drawn as a small circle or single pixel.
	*/
	public Color colorOrNull;

	public CornerData(LayeredZigzag smartblob, Corner corner, boolean edge){
		super(edge?3:6, edge?4:6, edge?4:6);
		this.smartblob = smartblob;
		this.corner = corner;
	}

	public void connectAdjacent(){
		final int lay = corner.layer;
		final int pt = corner.point;
		final int laySiz = smartblob.layerSize;

		//TODO pointers to TriData

		//TODO pointers to CornerData
		final CornerData[][] sc = smartblob.corners;
		final CornerData[] ac = this.adjacentCorners;
		ac[0] = sc[lay][(pt+1)%laySiz];
		ac[1] = sc[lay][(pt-1+laySiz)%laySiz];
		boolean layerIsOdd = (corner.layer&1)==1;
		int highPInOtherLayer = layerIsOdd ? (pt+1)%laySiz : pt;
		final int hpi = (highPInOtherLayer - 1 + laySiz) % laySiz;
		final int hpl = (highPInOtherLayer) % laySiz;

		if(ac.length == 6){ //6 adjacentCorners, all other 4
			final CornerData[] scn = sc[lay - 1];
			final CornerData[] scp = sc[lay + 1];
			ac[2] = scn[(hpl)];
			ac[3] = scn[(hpi)];
			ac[4] = scp[(hpl)];
			ac[5] = scp[(hpi)];
		}else if(corner.layer == 0){ //4 adjacentCorners, other 2 are at higher layer
			final CornerData[] scp = sc[lay + 1];
			ac[2] = scp[(hpl)];
			ac[3] = scp[(hpi)];
		}else{ //4 adjacentCorners, other 2 are at lower layer
			final CornerData[] scn = sc[lay - 1];
			ac[2] = scn[(hpl)];
			ac[3] = scn[(hpi)];
		}

		//TODO pointers to LineData
		for(int i = 0; i< ac.length; i++){
			//matches as key when either corner does this
			Line line = new Line(corner, ac[i].corner);
			adjacentLines[i] = smartblob.lineData(line);
		}
	}

}
