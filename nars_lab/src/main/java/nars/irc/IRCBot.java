package nars.irc;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class IRCBot {

    private final String server;
    public final String nick;
    private final String login;
    protected final String channel;
    boolean outputting = false;

    static final String pingHead = "PING ";

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

        new Thread(() -> {

            // Join the channel.
            try {
                // Read lines from the server until it tells us we have connected.
                String line = null;
                while ((line = reader.readLine( )) != null) {
                    if (line.contains("004")) {
                        // We are now logged in.
                        break;
                    }
                    else if (line.contains("433")) {
                        System.out.println("Nickname is already in use.");
                        return;
                    }
                }


                writer.write("JOIN " + channel + "\r\n");
                writer.flush();
                // Keep reading lines from the server.
                while ((line = reader.readLine( )) != null) {

                    try {
                        Message m = Message.parse(line);

                        System.err.println("in: " + m + " from " + line);

                        switch (m.command) {
                            case "PING":
                                writer.write("PONG " + m.params.get(0) + "\r\n");
                                writer.flush();
                                break;
                            case "PRIVMSG":
                                System.err.println(line);

                                onMessage(IRCBot.this, m.params.get(0), m.nick, m.params.get(1));
                                break;
                            default:
                                System.err.println("unknown: " + m + " from " + line);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }).start();
    }

    protected abstract void onMessage(IRCBot bot, String channel, String nick, String msg);

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



    public static class Message extends HashMap<String,Object> {

        String prefix;
        String nick;
        String command;
        ArrayList<String> params = new ArrayList<>();

        @Override
        public String toString() {
            return command + ", " + prefix + ", " + super.toString() + ", " + params;
        }

        public static Message parse(String line) {
            Message message = new Message();
            int position = 0;
            int nextspace = 0;
            // parsing!
            if (line.charAt(0) == '@') {
                String[] rawTags;

                nextspace = line.indexOf(' ');
                System.out.println(nextspace);
                if (nextspace == -1) {
                    return null;
                }

                rawTags = line.substring(1, nextspace).split(";");

                for (String tag : rawTags) {
                    String[] pair = tag.split("=");

                    if (pair.length == 2) {
                        message.put(pair[0], pair[1]);
                    } else {
                        message.put(pair[0], true);
                    }
                }
                position = nextspace + 1;
            }

            while (line.charAt(position) == ' ') {
                position++;
            }

            if (line.charAt(position) == ':') {
                nextspace = line.indexOf(' ', position);
                if (nextspace == -1) {
                    return null;
                }
                message.prefix = line.substring(position + 1, nextspace);
                position = nextspace + 1;

                while (line.charAt(position) == ' ') {
                    position++;
                }

                if ((message.prefix.length() > 1) && (message.prefix.indexOf('!')!=-1))
                    message.nick = message.prefix.substring(0, message.prefix.indexOf('!'));
            }

            nextspace = line.indexOf(' ', position);

            if (nextspace == -1) {
                if (line.length() > position) {
                    message.command = line.substring(position);
                }
                return message;
            }

            message.command = line.substring(position, nextspace);

            position = nextspace + 1;

            while (line.charAt(position) == ' ') {
                position++;
            }

            while (position < line.length()) {
                nextspace = line.indexOf(' ', position);

                if (line.charAt(position) == ':') {
                    String param = line.substring(position + 1);
                    message.params.add(param);
                    break;
                }

                if (nextspace != -1) {
                    String param = line.substring(position, nextspace);
                    message.params.add(param);
                    position = nextspace + 1;

                    while (line.charAt(position) == ' ') {
                        position++;
                    }
                    continue;
                }

                if (nextspace == -1) {
                    String param = line.substring(position);
                    message.params.add(param);
                    break;
                }
            }

            return message;
        }
    }
}
