package nars.guifx;

import automenta.vivisect.javafx.graph3.SpaceNet;
import automenta.vivisect.javafx.graph3.Xform;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import nars.NAR;
import nars.bag.Bag;
import nars.budget.Itemized;
import nars.concept.Concept;
import nars.event.FrameReaction;
import nars.link.TLink;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.nar.Default;
import nars.task.Sentence;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static javafx.application.Platform.runLater;
import static nars.guifx.NARfx.scrolled;

/**
 * Created by me on 8/10/15.
 */
public class ConceptPane extends BorderPane implements ChangeListener {

    private final Concept concept;
    private final NAR nar;
    private final LinkView links;
    private final BagView<Sentence, TaskLink> taskLinkView;
    private final BagView<TermLinkKey, TermLink> termLinkView;
    private FrameReaction reaction;

    public class LinkView extends SpaceNet {

        final ColorArray ca = new ColorArray(32, Color.BLUE, Color.RED);

        public LinkView() {
            super();



            frame();
            setPickOnBounds(true);
            setMouseTransparent(false);
        }

        @Override
        public Xform getRoot() {
            final Xform g = new Xform();


//            final PhongMaterial redMaterial = new PhongMaterial();
//            redMaterial.setDiffuseColor(Color.DARKRED);
//            redMaterial.setSpecularColor(Color.RED);
//
//            final PhongMaterial whiteMaterial = new PhongMaterial();
//            whiteMaterial.setDiffuseColor(Color.WHITE);
//            whiteMaterial.setSpecularColor(Color.LIGHTBLUE);
//
//            final PhongMaterial greyMaterial = new PhongMaterial();
//            greyMaterial.setDiffuseColor(Color.DARKGREY);
//            greyMaterial.setSpecularColor(Color.GREY);
//
//            // Molecule Hierarchy
//            // [*] moleculeXform
//            //     [*] oxygenXform
//            //         [*] oxygenSphere
//            //     [*] hydrogen1SideXform
//            //         [*] hydrogen1Xform
//            //             [*] hydrogen1Sphere
//            //         [*] bond1Cylinder
//            //     [*] hydrogen2SideXform
//            //         [*] hydrogen2Xform
//            //             [*] hydrogen2Sphere
//            //         [*] bond2Cylinder
//            Xform moleculeXform = new Xform();
//            Xform oxygenXform = new Xform();
//            Xform hydrogen1SideXform = new Xform();
//            Xform hydrogen1Xform = new Xform();
//            Xform hydrogen2SideXform = new Xform();
//            Xform hydrogen2Xform = new Xform();
//
//            Sphere oxygenSphere = new Sphere(40.0);
//            oxygenSphere.setMaterial(redMaterial);
//
//            Sphere hydrogen1Sphere = new Sphere(30.0);
//            hydrogen1Sphere.setMaterial(whiteMaterial);
//            hydrogen1Sphere.setTranslateX(0.0);
//
//            Sphere hydrogen2Sphere = new Sphere(30.0);
//            hydrogen2Sphere.setMaterial(whiteMaterial);
//            hydrogen2Sphere.setTranslateZ(0.0);
//
//            Cylinder bond1Cylinder = new Cylinder(5, 100);
//            bond1Cylinder.setMaterial(greyMaterial);
//            bond1Cylinder.setTranslateX(50.0);
//            bond1Cylinder.setRotationAxis(Rotate.Z_AXIS);
//            bond1Cylinder.setRotate(90.0);
//
//            Cylinder bond2Cylinder = new Cylinder(5, 100);
//            bond2Cylinder.setMaterial(greyMaterial);
//            bond2Cylinder.setTranslateX(50.0);
//            bond2Cylinder.setRotationAxis(Rotate.Z_AXIS);
//            bond2Cylinder.setRotate(90.0);
//
//            moleculeXform.getChildren().add(oxygenXform);
//            moleculeXform.getChildren().add(hydrogen1SideXform);
//            moleculeXform.getChildren().add(hydrogen2SideXform);
//            oxygenXform.getChildren().add(oxygenSphere);
//            hydrogen1SideXform.getChildren().add(hydrogen1Xform);
//            hydrogen2SideXform.getChildren().add(hydrogen2Xform);
//            hydrogen1Xform.getChildren().add(hydrogen1Sphere);
//            hydrogen2Xform.getChildren().add(hydrogen2Sphere);
//            hydrogen1SideXform.getChildren().add(bond1Cylinder);
//            hydrogen2SideXform.getChildren().add(bond2Cylinder);
//
//            hydrogen1Xform.setTx(100.0);
//            hydrogen2Xform.setTx(100.0);
//            hydrogen2SideXform.setRotateY(156);
//
//            g.getChildren().add(moleculeXform);
            return g;

        }



        public class TLinkNode extends Group {

            public final TLink link;
            private final Box shape;
            private final PhongMaterial mat = new PhongMaterial();

            public TLinkNode(TLink tl) {
                super();

                shape = new Box(0.8, 0.8, 0.8);

                shape.setMaterial(mat);
                //shape.onMouseEnteredProperty()

                getChildren().add(shape);

                this.link = tl;

                frame();

            }

            public void frame() {
                mat.setDiffuseColor(ca.get(link.getPriority()));
            }
        }

        final Map<TLink, TLinkNode> linkShape = new LinkedHashMap();

        final Set<TLink> dead = new UnifiedSet();
        double n;

        public void frame() {

            dead.addAll(linkShape.keySet());

            n = 0;
            concept.getTermLinks().forEach(tl -> {

                dead.remove(tl);

                List<TLinkNode> toAdd = new ArrayList();
                TLinkNode  b = linkShape.get(tl);
                if (b==null) {
                    b = new TLinkNode(tl);
                    linkShape.put(tl, b);
                    b.setTranslateX(n++);
                    b.setTranslateY(tl.getPriority() * 4f);

                    toAdd.add(b);
                }

                b.frame();

                runLater(() -> {
                    getChildren().addAll(toAdd);
                });
            });

            //TODO this may be inefficient
            linkShape.keySet().removeAll(dead);

            dead.clear();
        }
    }

    public class BagView<X, Y extends Itemized<X>> extends FlowPane implements Runnable {

        final Map<X,Node> componentCache = new WeakHashMap<>();
        private final Bag<X, Y> bag;
        private Function<Y, Node> builder;
        final List<Node> pending = new ArrayList();
        final AtomicBoolean queued = new AtomicBoolean();

        public BagView(Bag<X, Y> bag, Function<Y,Node> builder) {
            super();
            this.bag = bag;
            this.builder = builder;
            frame();
        }

        Node getNode(Y c) {
            final X n = c.name();
            Node existing = componentCache.get(n);
            if (existing == null) {
                componentCache.put(n, existing = builder.apply(c));
            }
            return existing;
        }

        public void frame() {
            synchronized (pending) {
                pending.clear();
                bag.forEach(b -> {
                    pending.add(getNode(b));
                });
            }

            if (!getChildren().equals(pending) && queued.compareAndSet(false, true)) {
                Platform.runLater(this);
            }
        }

        @Override
        public void run() {
            synchronized (pending) {
                getChildren().setAll(pending);
                queued.set(false);
            }

            for (final Node n : getChildren())
                if (n instanceof Runnable)
                    ((Runnable) n).run();
        }
    }

    public ConceptPane(NAR nar, Concept c) {

        this.concept = c;
        this.nar = nar;

        setTop(new Label(c.toInstanceString()));

        //Label termlinks = new Label("Termlinks diagram");
        //Label tasklinks = new Label("Tasklnks diagram");
        links = new LinkView();
        //TilePane links = new TilePane(links.content);

//        Label beliefs = new Label("Beliefs diagram");
//        Label goals = new Label("Goals diagram");
//        Label questions = new Label("Questions diagram");
        Pane tasks = new TilePane(8, 8,
                scrolled(termLinkView = new BagView<TermLinkKey, TermLink>(c.getTermLinks(),
                                (t) -> new ItemButton( t, (i) -> i.toString(),
                                        (i) -> {

                                        }

                                )
                        )
                ),
                scrolled(taskLinkView = new BagView<Sentence, TaskLink>(c.getTaskLinks(),
                        (t) -> new ItemButton( t, (i) -> i.toString(),
                                (i) -> {

                                }
                        )
                ))
        );
        tasks.maxHeight(Double.MAX_VALUE);
        tasks.prefHeight(Double.MAX_VALUE);

        setCenter(new SplitPane(tasks, links.content));

        Label controls = new Label("Control Panel");
        setBottom(controls);

        visibleProperty().addListener(this);
        changed(null, null, null);
    }




    protected void frame() {
        links.frame();
        taskLinkView.frame();
        termLinkView.frame();
    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        if (isVisible()) {
            reaction = new FrameReaction(nar) {
                @Override public void onFrame() {
                    frame();
                }
            };
        }
        else {
            if (reaction!=null) {
                reaction.off();
                reaction = null;
            }
        }
    }


    /* test example */
    public static void main(String[] args) {
        NAR n = new NAR(new Default());
        n.input("<a-->b>. <b-->c>. <c-->a>.");
        n.frame(516);

        NARfx.run((a,s) -> {
            NARfx.window(n, n.concept("<a-->b>"));
//            s.setScene(new ConsolePanel(n, n.concept("<a-->b>")),
//                    800,600);

            new Thread(() -> n.loop(10)).start();
        });




    }

}
