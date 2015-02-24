/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package automenta.vivisect.javafx;

import automenta.vivisect.javafx.tag.OperatorTagPane;
import javafx.application.Platform;

import java.util.Collection;


/**
*
* @author me
*/
public class WikiTagger extends WikiBrowser {



    public WikiTagger(String startPage) {
        super(startPage);


    }


    @Override
    public void onTagClicked(final String id) {
        setBottom(new OperatorTagPane(id, this) {

            public void onFinished(boolean save, final Object _subject, final Collection<String> tags) {
                if (save && tags!=null) {

                    Object subject;
                    /*if (_subject == null)
                        subject = core.getMyself();
                    else*/
                        subject = _subject;


                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            //TODO create Nobject

                            /*
                            NObject n = core.newObject(subject.name + ": " + id + "=" + tags);

                            n.setSubject(subject.id);

                            for (String t : tags) {
                                n.add(t, id);
                            }

                            core.publish(n);
                            */


                        }
                    });


                }


                WikiTagger.this.setBottom(null);
            }

        });
    }


}
