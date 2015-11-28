/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.bit.object;


import smartblob.wavetree.bit.Bits;
import smartblob.wavetree.bit.BitsUtil;

import java.io.InputStream;

/** Can only handle quantityOfDims in int range, and probably much less than that. */
public class SimplePolydim implements Polydim{
	
	public final Bits data;
	public Bits data(){ return data; }
	
	protected Bits header, headerThenData;
	
	protected final long dimSizes[];
	
	protected final long dimSizesCumulative[];
	
	static{System.out.println("TODO MultiDim.bits(int...) and SimpleMultiDim.dimSizes(Bits) and constructor with int[], decide on bigEndian (as it is now) or littleEndian (default for this software) list of dims should be");}
	
	/** Bits includes part of header thats after any part that says this is a MultiDim,
	like common.DatastructHeaders.MULTIDIMBITARRAY.
	Bits may be longer than needed since this may be part of a streaming process
	without knowing how long the data is without first doing the parsing of the header which is done here.
	*/
	public SimplePolydim(Bits headerThenData){
		this(
			//after int howManyDims and that many ints for size of each dim
			headerThenData.suf(64*(1+headerThenData.longAt(0))),
			dimSizes(headerThenData)
		);
		header = headerThenData.pre(64*(1+headerThenData.longAt(0)));
	}
	
	/** startsAtHeader is after any part of header which means this is a multidim.
	It can have more bits after header, but those are ignored.
	*/
	public static long[] dimSizes(Bits startsAtHeader){
		long dims = startsAtHeader.longAt(0);
		if(Integer.MAX_VALUE < dims) throw new RuntimeException(
			"Dims="+dims+" not fit in int range. Its a long mostly so the quantity of dims is the"
			+" same kind of integer as size of each dim, but it may later be useful for"
			+" sparse representations of dims.");
		long dimSizes[] = new long[(int)dims];
		long index = 64;
		for(int i=0; i<dimSizes.length; i++){
			dimSizes[i] = startsAtHeader.longAt(index);
			index += 64;
		}
		return dimSizes;
	}
	/*
	public static int[] dimSizes(Bits startsAtHeader){
		int dimSizes[] = new int[startsAtHeader.intAt(0)];
		long index = 32;
		for(int i=0; i<dimSizes.length; i++){
			dimSizes[i] = startsAtHeader.intAt(index);
			index += 32;
		}
		return dimSizes;
	}*/
	
	public SimplePolydim(Bits startsWithData, long... dimSizes){
		this.dimSizes = dimSizes.clone();
		long mult = 1;
		for(long dimSize : dimSizes) mult *= dimSize; //TODO what if it wraps around?
		if(mult != startsWithData.siz()) throw new IllegalArgumentException(
			"Multiply of all dimSizes is "+mult+" but bits size is "+startsWithData.siz());
		dimSizesCumulative = new long[dimSizes.length];
		long size = 1;
		for(int d=dimSizesCumulative.length-1; d>=0; d--){
			dimSizesCumulative[d] = size;
			size *= dimSizes[d];
		}
		this.data = startsWithData.pre(size);
	}
	/*public SimpleMultiDim(Bits startsWithData, int... dimSizes){
		this.dimSizes = dimSizes.clone();
		long mult = 1;
		for(int dimSize : dimSizes) mult *= dimSize; //TODO what if it wraps around?
		if(mult != startsWithData.siz()) throw new IllegalArgumentException(
			"Multiply of all dimSizes is "+mult+" but bits size is "+startsWithData.siz());
		dimSizesCumulative = new long[dimSizes.length];
		long size = 1;
		for(int d=dimSizesCumulative.length-1; d>=0; d--){
			dimSizesCumulative[d] = size;
			size *= dimSizes[d];
		}
		this.data = startsWithData.pre(size);
	}*/
	
	public Bits header(){
		//could check headerThenData==null but if header!=null then its faster to only refer to 1 var
		if(header == null) createHeaderAndHeaderThenDataBits();
		return header;
	}
	
	public Bits headerThenData(){
		//could check header==null but if headerThenData!=null then its faster to only refer to 1 var
		if(headerThenData == null) createHeaderAndHeaderThenDataBits();
		return headerThenData;
	}
	
	protected void createHeaderAndHeaderThenDataBits(){
		if(header == null){
			Bits h = BitsUtil.longToBits(dimSizes.length);
			for(long dimSize : dimSizes) h = h.cat(BitsUtil.longToBits(dimSize));
			header = h;
			headerThenData = header.cat(data);
		}
	}
	/*protected void createHeaderAndHeaderThenDataBits(){
		if(header == null){
			Bits h = BitsUtil.intToBits(dimSizes.length);
			for(int dimSize : dimSizes) h = h.cat(BitsUtil.intToBits(dimSize));
			header = h;
			headerThenData = header.cat(data);
		}
	}*/
	
	/** Does not close stream. Reads only as many bytes as header says to
	but last byte may have unused lower bits if not a multiple of 8 bits.
	*/
	public SimplePolydim(InputStream in){
		throw new RuntimeException("TODO");
	}
	
	public long dims(){
		return dimSizes.length;
	}
	
	public long dimSize(long dimIndex){
		return dimSizes[(int)dimIndex];
	}
	/*public int dimSize(int dimIndex){
		return dimSizes[dimIndex];
	}*/

	/** TODO This is not littleEndian of array indexs. littleEndian is the standard for this software.
	Change it later after get it working on screen again, if it can be done,
	because I dont know if the bits function that returns MultiDim, recursively, can work as littleEndian.
	Or does it simply mean that the array would be used in reverse in these params (probably so)?
	*/
	public boolean bit(int... indexs){
		long sum = 0;
		for(int d=0; d<dimSizes.length; d++){
			sum += dimSizesCumulative[d]*indexs[d];
		}
		return data.bitAt(sum);
	}
	
	public boolean bit(long... indexs){
		long sum = 0;
		for(int d=0; d<dimSizes.length; d++){
			sum += dimSizesCumulative[d]*indexs[d];
		}
		return data.bitAt(sum);
	}
	
	/** Same indexs as bit(int...) except only the first n dims */
	public Polydim bits(int... indexsButLessDims){
		//TODO optimize by rewriting the commented code at end of this func for ints?
		long g[] = new long[indexsButLessDims.length];
		for(int i=0; i<g.length; i++) g[i] = indexsButLessDims[i];
		return bits(g);
		/*
		if(indexsButLessDims.length == 0) return this;
		if(indexsButLessDims.length != 1) throw new RuntimeException("TODO");
		long start = dimSizesCumulative[0]*indexsButLessDims[0];
		long end = start+dimSizesCumulative[0];
		Bits b = data.sub(start,end);
		int remainingDimSizes[] = new int[dimSizes.length-indexsButLessDims.length];
		System.arraycopy(dimSizes, indexsButLessDims.length, remainingDimSizes, 0, remainingDimSizes.length);
		return new SimpleMultiDim(b, remainingDimSizes);
		*/
	}
	
	public Polydim bits(long... indexsButLessDims){
		if(indexsButLessDims.length == 0) return this;
		if(indexsButLessDims.length != 1) throw new RuntimeException("TODO");
		long start = dimSizesCumulative[0]*indexsButLessDims[0];
		long end = start+dimSizesCumulative[0];
		Bits b = data.sub(start,end);
		long remainingDimSizes[] = new long[dimSizes.length-indexsButLessDims.length];
		System.arraycopy(dimSizes, indexsButLessDims.length, remainingDimSizes, 0, remainingDimSizes.length);
		return new SimplePolydim(b, remainingDimSizes);
	}

}
