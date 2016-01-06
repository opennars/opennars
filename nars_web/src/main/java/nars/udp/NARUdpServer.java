//package nars.udp;
//
//import nars.NAR;
//import nars.nar.Terminal;
//
//import java.net.SocketException;
//
///**
// * Created by me on 10/12/15.
// */
//public class NARUdpServer {
//
//    public static void main(String[] args) throws SocketException, InterruptedException {
//        /*if (args.length!=1) {
//            args = new String[] { "10001"  };
//        }
//        int port = Integer.valueOf(args[0]);*/
//        int port = 10001+(int)(Math.random()*10);
//
//        float netHz = 25f;
//        float narHz = 1f;
//
//        NAR n = new Terminal();
//        UDPNetwork net = new UDPNetwork(port).setFrequency(netHz);
//
//        //n.log();
//
//        n.input("a:b. b:c. c:d.");
//
//        net.in.on(x -> {
//            n.input("recv:\"" + x + "\".");
//        });
//        n.loop(narHz);
//
//        Thread.sleep(5000);
//
//        for (int i = 0; i < 10; i++) {
//            net.peer("localhost", 10001 + i, 1f);
//        }
//
//        net.out(port + " here");
//
//        System.out.println(net.peer.getPeers());
//
//    }
// }
