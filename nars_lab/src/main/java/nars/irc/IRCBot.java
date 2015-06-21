package nars.irc;


import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.ReflectPanel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.NARReaction;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.io.out.TextOutput;
import nars.model.impl.Default;
import nars.nal.concept.Concept;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.op.io.say;
import nars.rl.example.MarkovObservationsGraph;
import nars.util.language.Twokenize;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

abstract public class IRCBot {

    private final String server;
    public final String nick;
    private final String login;
    protected final String channel;
    boolean outputting = false;


    protected BufferedWriter writer = null;

    public void setOutputting(boolean outputting) {
        this.outputting = outputting;
    }

    public IRCBot(String server, String nick, String channel) throws Exception {
        this(server, nick, nick.toLowerCase(), channel);
    }

    public IRCBot(String server, String nick, String login, String channel) throws Exception {

        this.server = server;
        this.nick = nick;
        this.login = login;
        this.channel = channel;

        //new NWindow(this.toString(), new ReflectPanel(this)).show(500,300);


        /*
        new BufferedOutput(nar, 1, 1000, 64) {

            @Override
            protected void output(List<BufferedOutput.OutputItem> buffer) {
               System.out.println(buffer);
            }
        };
        */


        //new TextOutput(nar, System.out).setShowErrors(true).setOutputPriorityMin(0.95f);

        // Connect directly to the IRC server.
        Socket socket = new Socket(server, 6667);
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream( )));
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream( )));

        // Log on to the server.
        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + login + " 8 * : " + nick + "\r\n");
        writer.flush();

        new Thread(new Runnable() {

            @Override
            public void run() {

                // Join the channel.
                try {
                    // Read lines from the server until it tells us we have connected.
                    String line = null;
                    while ((line = reader.readLine( )) != null) {
                        if (line.indexOf("004") >= 0) {
                            // We are now logged in.
                            break;
                        }
                        else if (line.indexOf("433") >= 0) {
                            System.out.println("Nickname is already in use.");
                            return;
                        }
                    }


                    writer.write("JOIN " + channel + "\r\n");
                    writer.flush();
                    // Keep reading lines from the server.
                    while ((line = reader.readLine( )) != null) {
                        String pingHead = "unknown: PING ";
                        if (line.toLowerCase( ).startsWith(pingHead)) {
                            // We must respond to PINGs to avoid being disconnected.
                            writer.write("PONG " + line.substring(pingHead.length()) + "\r\n");
                            //writer.write("PRIVMSG " + channel + " :I got pinged!\r\n");
                            //writer.flush( );
                        }
                        else {
                            // Print the raw line received by the bot.
                            //System.out.println(line);
                            if (line.contains(" PRIVMSG " )) {
                                System.err.println(line);
                                String part = "PRIVMSG " + channel;
                                int s = line.indexOf(part);
                                if (s!=-1) {
                                    String msg = line.substring(s + part.length() + 2);
                                    String ch = channel;
                                    if (ch.startsWith("#"))
                                        ch = ch.substring(1);

                                    onMessage(IRCBot.this, channel, msg);
                                }

                            }
                            else {
                                System.err.println("unknown: " + line);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    protected abstract void onMessage(IRCBot bot, String channel, String msg);

    protected boolean send(String channel, String message) {
        try {
            writer.write("PRIVMSG " + channel + " :" + message + "\r\n");
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /** to default channel */
    public boolean send(String message) {
        return send(channel, message);
    }

}
