//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.comm.SmartConversation;
//import automenta.spacegraph.swing.SwingWindow;
//import com.restfb.DefaultFacebookClient;
//import com.restfb.FacebookClient;
//import com.restfb.FacebookException;
//import com.restfb.types.Note;
//import com.restfb.types.User;
//import java.awt.BorderLayout;
//import java.util.List;
//import javax.swing.JPanel;
//import twitter4j.Status;
//
///**
// *
// * @author seh
// */
//public class RunSmartConversation {
//
//    public static class ConversationPanel extends JPanel {
//
//        private final SmartConversation con;
//
//        public ConversationPanel(SmartConversation c) {
//            super(new BorderLayout());
//
//            this.con = c;
//
//            {
//                //            {
//                //                try {
//                //                    Twitter t = new TwitterFactory().getInstance();
//                //                    ResponseList rl = t.getPublicTimeline();
//                //                    for (int i = 0; i < rl.size(); i++) {
//                //                        Object o = rl.get(i);
//                //                        if (o instanceof Status) {
//                //                            addStatus((Status) o);
//                //                        }
//                //                    }
//                //                } catch (TwitterException ex) {
//                //                    ex.printStackTrace();
//                //                }
//                //            }
//
//            }
//        }
//
//        protected void addStatus(Status status) {
//            String retweet = null, replyTo = null;
//            if (status.isRetweet()) {
//                retweet = "twitter: " + status.getRetweetedStatus().getId();
//            }
//            if (status.getInReplyToStatusId() != -1) {
//                replyTo = "twitter: " + status.getInReplyToStatusId();
//            }
//            //con.addMessage("twitter:" + status.getId(), status.getUser().getScreenName(), status.getText(), retweet, replyTo);
//        }
//    }
//
//    public static void main(String[] args) {
//
//        new SwingWindow(new ConversationPanel(new SmartConversation()), 800, 600, true);
//    }
//}
