package automenta.rdp.rdp5.snd;

import automenta.rdp.AbstractRdpPacket;
import automenta.rdp.RdesktopException;
import automenta.rdp.crypto.CryptoException;
import automenta.rdp.rdp5.VChannel;
import automenta.rdp.rdp5.VChannels;

import java.io.IOException;

public class SoundChannel extends VChannel {

    @Override
    public String name() {
        return "rdpsnd";
    }

    @Override
    public int flags() {
        return VChannels.CHANNEL_OPTION_INITIALIZED | VChannels.CHANNEL_OPTION_ENCRYPT_RDP;
    }

    @Override
    public void process(AbstractRdpPacket data) throws RdesktopException, IOException,
            CryptoException {
        // do nothing

    }

}
