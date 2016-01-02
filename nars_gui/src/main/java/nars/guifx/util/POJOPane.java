package nars.guifx.util;

import com.google.common.collect.Lists;
import com.gs.collections.api.tuple.Twin;
import com.gs.collections.impl.tuple.Tuples;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import za.co.knonchalant.builder.Ignore;
import za.co.knonchalant.builder.POJONode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static javafx.application.Platform.runLater;

/**
 * Contains controls bound to fields and/or methods reflected from the
 * class of an instance to be widgetized
 */
public class POJOPane<X> extends BorderPane {

    public final X obj;

    public POJOPane(X obj) {
        this.obj = obj;

        List<Node> pn = propertyNodes(obj);

        if (pn!=null && !pn.isEmpty()) {
            VBox controls = new VBox();

            //controls.maxWidth(Double.MAX_VALUE);
            //controls.maxHeight(Double.MAX_VALUE);
            controls.getChildren().addAll(pn);

            ToggleButton toggle = new ToggleButton("[X] " + obj);
            toggle.selectedProperty().addListener(e->{
                setCenter(toggle.isSelected() ? controls : null);
                layout();
            });
            setTop(toggle);

            runLater(()-> toggle.setSelected(true));
        }
        else {
            setCenter(new Label(obj.toString()));
        }


    }

    /**
     * Build up a GUI from the given POJO, as read-only or not, using the provided Layout direction
     * and additional information provided by parameters.
     *
     * @param object   POJO to read
     * @return the GUI
     */
//    public static Collection<Node> methodNodes(Object object, TaggedParameters params) {
//        Method[] declaredMethods = object.getClass().getMethods();
//
//        List<Node> nodes = Global.newArrayList();
//
//        for (Method method : declaredMethods) {
//            if (method.getAnnotation(Ignore.class) != null) {
//                continue;
//            }
//
//            HBox methodBox = produceMethodNode(object, /* readOnly*/ false, params, method);
//
//            nodes.add(methodBox);
//        }
//        return nodes;
//    }

    public static List<Node> propertyNodes(Object object) {

        if (object == null) return Lists.newArrayList(new Text("null")); //Collections.emptyList();

        Field[] fields = object.getClass()
                //.getDeclaredFields();
                .getFields();

        //System.out.println("fields for : " + object + " (" + object.getClass() + "): " + Arrays.toString(fields));

        //List<Twin<Node>> nodes = Global.newArrayList();

        //final Pane node = new VBox(); //TODO use a lambda parameter builder

        List<Node> nodes = new ArrayList();
        for (Field f : fields) {
            if (f.getAnnotation(Ignore.class) != null)
                continue;

            if (Modifier.isStatic(f.getModifiers()))
                continue;

//            if (!Property.class.isAssignableFrom(f.getType()))
//                continue;

            int mod = f.getModifiers();
            if (!Modifier.isPublic(mod)
                    || !Modifier.isFinal(mod)) {
                //restrict to only: public final
                continue;
            }


            Twin<Node> methodBox = newFieldNode(object, /* readOnly*/ false, f);
            if (methodBox == null) return null;



            Node label = methodBox.getOne();
            Tooltip.install(label, new Tooltip(f + "\n\t" + methodBox.getTwo()));
            nodes.add(new FlowPane(label, methodBox.getTwo()));
        }
        return nodes;
    }

    public static Twin<Node> newFieldNode(Object object, boolean readOnly, Field field) {


        String name = POJONode.getName(field);
        Label label = new Label(name);
        label.getStyleClass().addAll("built-label");
        //label.setPrefWidth(200);
        //Node valueNode = getValueField(object, method, readOnly || getSetter(method) == null, name, params);*/

        Node valueNode;
        try {
            Object value = POJONode.getField(object, field);
            if (value == object)
                return null;

            valueNode = value != null ?
                    POJONode.valueToNode(value, field) :
                    new Text("null");
        } catch (IllegalAccessException e) {
            valueNode =
                    new /*Error*/Label(field.getType().toString() + '\n' + e.toString());
        }


        return Tuples.twin(label, valueNode);

    }
}
