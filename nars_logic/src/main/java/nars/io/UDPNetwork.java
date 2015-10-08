package nars.io;

import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.tuple.Tuples;
import hellblazer.gossip.GossipPeer;
import nars.NAR;
import nars.nal.nal2.Instance;
import nars.nal.nal7.Tense;
import nars.narsese.NarseseParser;
import nars.term.Atom;
import nars.term.Term;
import nars.util.event.DefaultTopic;
import nars.util.event.Topic;
import nars.util.java.DefaultTermizer;
import nars.util.java.Termizer;

import java.io.Serializable;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** a UDP Gossip network interface (NIC) that provides a send and receive stream
 * see: Gossip Protocol
 * */
public class UDPNetwork<O extends Serializable>  /* implements NARStream.. */
    implements Consumer<O> {

    public final Term id; //used for the stream identifier term

    final Termizer termizer = new DefaultTermizer();
    public final GossipPeer peer;

    public final Topic<Pair<UUID,O>> in = new DefaultTopic<>();
    public final Topic<O> out = new DefaultTopic();

    public UDPNetwork(int port) throws SocketException {
        this("udp:" + port, port);
    }
    public UDPNetwork(String id, int port) throws SocketException {
        this( (Term)NarseseParser.the().term(id), port );
    }
    public UDPNetwork(Term id, int port) throws SocketException {
        super();

        this.id = id;

        peer = new GossipPeer(port) {
            public final void onUpdate(UUID id, Object j) {
                try {
                    in.emit( Tuples.pair(id, (O) j) );
                }
                catch (Exception e) {
                    peer.log(e);
                }
            }
        };

        out.on(this);

    }

    public UDPNetwork setFrequency(float hz) {
        long ms = (long)(1000f/hz);
        peer.gossip.setInterval((int)ms, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public final void accept(O o) {
        try {
            peer.put(o);
        }
        catch (Exception e) {
            peer.log(e);
        }
    }


    /** initialize a NAR with operators for this network */
    public void connect(NAR nar) {
        nar.on("send", (Term[] args) -> {
            if (args.length < 2) return null;
            Term stream = args[0];

            //TODO use a central dispatcher for all streams; not all streams watching for send(
            if (stream.equals(id)) {
                Term message = args[1];
                send(nar, message);
            }
            return null;
        });
        //HACK dont use regs directly
        nar.regs.add(
            in.on(p -> {
                UUID u = p.getOne();
                O o = p.getTwo();

                nar.believe(
                        Instance.make(
                                //Atom.quote(u.toString()),
                                termizer.term(o),
                                Atom.the("recv")
                                //Temporal.ORDER_FORWARD
                        ),
                        Tense.Present,
                        1.0f,
                        0.9f
                );
            })
        );
    }

    void send(NAR sender, Term message) {
        peer.put(message);
    }


}
