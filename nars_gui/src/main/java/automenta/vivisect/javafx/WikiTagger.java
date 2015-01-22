///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package automenta.vivisect.javafx;
//
//import java.util.Collection;
//import javafx.application.Platform;
//import jnetention.Core;
//import jnetention.NObject;
//
///**
// *
// * @author me
// */
//public class WikiTagger extends WikiBrowser {
//
//
//
//    public WikiTagger(Core core, String startPage) {
//        super(core, startPage);
//
//
//    }
//
//
//    @Override
//    public void onTagClicked(final String id) {
//        setBottom(new OperatorTagPane(core, id, this) {
//
//            @Override
//            public void onFinished(boolean save, final NObject _subject, final Collection<String> tags) {
//                if (save && tags!=null) {
//
//                    NObject subject;
//                    if (_subject == null)
//                        subject = core.getMyself();
//                    else
//                        subject = _subject;
//
//
//                    Platform.runLater(new Runnable() {
//                        @Override public void run() {
//                            //TODO create Nobject
//
//                            NObject n = core.newObject(subject.name + ": " + id + "=" + tags);
//
//                            n.setSubject(subject.id);
//
//                            for (String t : tags) {
//                                n.add(t, id);
//                            }
//
//                            core.publish(n);
//
//
//                        }
//                    });
//
//
//                }
//
//
//                WikiTagger.this.setBottom(null);
//            }
//
//        });
//    }
//
//
//}
