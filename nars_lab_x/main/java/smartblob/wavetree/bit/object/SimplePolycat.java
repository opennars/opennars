/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit.object;

import smartblob.wavetree.bit.Bits;
import smartblob.wavetree.bit.BitsUtil;
import smartblob.wavetree.bit.Fast0To16Bits;

public class SimplePolycat implements Polycat{
	
	/** If Bits comes in all in 1 piece, these are null until observed */ 
	protected final Bits cat[];
	
	/** If Bits comes in as pieces, this is null until observed */
	protected Bits data;
	
	protected Bits header, headerThenData;
	
	/** First index is 0. Last index is total size.
	1 bigger than cat.length so can be used as start and end.
	*/
	protected long start[];
	
	public SimplePolycat(Bits... cat){
		this.cat = cat.clone();
		start = new long[cat.length+1];
		start[0] = 0;
		for(int i=0; i<cat.length; i++){
			start[i+1] = start[i]+cat[i].siz();
		}
	}
	
	public SimplePolycat(Bits data, long... sizes){
		this.data = data;
		start = new long[sizes.length+1];
		for(int i=0; i<sizes.length; i++){
			start[i+1] = start[i]+sizes[i];
		}
		if(start[start.length-1] != data.siz()) throw new IllegalArgumentException(
			"Bits size "+data.siz()+" but sizes array sums to "+start[start.length-1]);
		cat = new Bits[sizes.length];
	}
	
	public long size(){
		return start[start.length-1];
	}
	
	public long partSize(long partIndex){
		return start[(int)partIndex+1]-start[(int)partIndex];
	}
	
	public long parts(){
		return start.length-1;
	}
	
	public Bits part(long partIndex){
		int i = (int)partIndex;
		if(cat[i] == null){
			cat[i] = data.sub(start[i], start[i+1]);
		}
		return cat[i];
	}
	
	public Bits data(){
		if(data == null){
			Bits b = Fast0To16Bits.EMPTY;
			for(Bits part : cat){
				b = b.cat(part);
			}
			data = b;
		}
		return data;
	}

	public Bits header(){
		if(header == null){
			long p = parts();
			Bits b = BitsUtil.longToBits(parts());
			for(long i=0; i<p; i++){
				b = b.cat(BitsUtil.longToBits(partSize(i)));
			}
			header = b;
		}
		return header;
	}
	
	public Bits headerThenData(){
		if(headerThenData == null){
			headerThenData = header().cat(data());
		}
		return headerThenData;
	}

}
