/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

public class WaveCastException extends ClassCastException{

	/** the Wave that could not be cast to some type. Can be null */
	public final Wave wave;

	/** failed to cast wave to this type. Can be null */
	public final Class type;

	public WaveCastException(){
		super();
		wave = null;
		type = null;
	}

	public WaveCastException(String message){
		super(message);
		wave = null;
		type = null;
	}

	public WaveCastException(Wave wave, Class type){
		super(type.getName());
		this.wave = wave;
		this.type = type;
	}

	public WaveCastException(Wave wave, Class type, String message){
		super(message);
		this.wave = wave;
		this.type = type;
	}

}
