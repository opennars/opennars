package nars.nario;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Recorder {
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private final DataOutputStream dos = new DataOutputStream(baos);

	private byte lastTick = 0;
	private int tickCount = 0;

	public void addLong(long val) {
		try {
			dos.writeLong(val);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addTick(byte tick) {
		try {
			if (tick == lastTick) {
				tickCount++;
			} else {
				dos.writeInt(tickCount);
				dos.write(tick);
				lastTick = tick;
				tickCount = 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getBytes() {
		try {
			dos.writeInt(tickCount);
			dos.write(-1);
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
}