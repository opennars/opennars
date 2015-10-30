package nars.io;

import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.tuple.Tuples;
import hellblazer.gossip.GossipPeer;
import nars.NAR;
import nars.nal.nal2.Instance;
import nars.nal.nal4.Product;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Term;
import nars.util.event.DefaultTopic;
import nars.util.event.On;
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

    public static final Atom udp = Atom.the("udp");

    public final Term id; //used for the stream identifier term

    final Termizer termizer = new DefaultTermizer();
    public final GossipPeer peer;

    public final Topic<Pair<UUID,O>> in = new DefaultTopic<>();
    public final Topic<O> out = new DefaultTopic();

    public UDPNetwork(int port) throws SocketException {
        this("udp:" + port, port);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + peer;
    }

    public UDPNetwork(String id, int port) throws SocketException {
        this( Atom.the(id), port );
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
    public UDPNetwork connect(NAR nar) {
        nar.onExecTerm("send", (Term[] args) -> {
            if (args.length < 2) return null;
            Term stream = args[0];

            //TODO use a central dispatcher for all streams; not all streams watching for send(
            if (stream.equals(id)) {
                Term message = args[1];
                send(message);
            }
            return null;
        });

        nar.onExec("peer", (Task<Operation> t) -> {
            Term[] args = t.getTerm().args();
            if (args.length < 1) return null;
            Term stream = args[0];

            //TODO use a central dispatcher for all streams; not all streams watching for send(
            if (stream.equals(id)) {
                Term message = args[1];

                //TODO Term pattern match strings
                //Map<Term, Atom /* generic type */> matches =
                //      message.extract("<%port --> udp>");

                if (message instanceof Product) {
                    Product i = (Product)message;
                    String host = ((Atom)i.term(0)).toStringUnquoted();
                    int port = Integer.parseInt(i.term(1).toString());

                    peer(host, port, t.getExpectation());

                    //Term port = i.getSubject();
                    //int pp = Integer.parseInt(port.toString());


                }


                //peer(t, message, nar);


            }
            return null;
        });


        //HACK dont modify regs directly
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
        return this;
    }

    void send(Term message) {
        peer.put(message);
    }

    /** specifies how strongly to "peer" with another host:port */
    public void peer(String host, int port, float strength) {
        if (strength > 0) {
            peer.connect(host, port);
        }
        else {
            //??
            //peer.gossip.view.
        }
    }

    public final On onIn(Consumer<Pair<UUID, O>> x) {
        return in.on(x);
    }
    public final On onOut(Consumer<O> x) {
        return out.on(x);
    }

    public void out(O s) {
        out.emit(s);
    }

    public void stop() {
        //TODO remove handlers

        peer.stop();
    }
}
