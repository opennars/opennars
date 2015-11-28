/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

public class WaveOps{

	public static String describeWave(Wave wave, int targetStringLen){
		if(targetStringLen < 10) return "a Wave";
		if(targetStringLen < 60) return "a "+wave.chan()
			+" channel Wave with length "+wave.len();
		if(targetStringLen < 120) return "a "+wave.chan()
			+" channel Wave with length "+wave.len()+" and average amplitude "+wave.aveAmp();
		throw new RuntimeException();
	}

}
