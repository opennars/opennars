package za.co.knonchalant.builder;

import com.google.common.util.concurrent.AtomicDouble;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import nars.data.Range;
import nars.guifx.annotation.Implementation;
import nars.guifx.annotation.ImplementationProperty;
import nars.guifx.annotation.Implementations;
import nars.guifx.graph2.layout.None;
import nars.guifx.util.MutableFloatProperty;
import nars.guifx.util.NSlider;
import nars.guifx.util.POJOPane;
import nars.util.data.MutableInteger;
import org.apache.commons.lang3.mutable.MutableFloat;
import za.co.knonchalant.builder.converters.IValueFieldConverter;
import za.co.knonchalant.builder.exception.ComponentException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generate JavaFX layouts based on POJO.
 */
public enum POJONode {
    ;
    private static final String GETTER_PREFIX = "get";

    /**
     * Convert a list of type T into a table
     *
     * @param objects list of objects
     * @param clazz   the type of the objects
     * @param <T>     standard POJO
     * @return a table view containing the objects
     */
    public static <T> TableView build(List<T> objects, Class<T> clazz) {
        return build(objects, clazz, true, null);
    }

    /**
     * Convert a list of type T into a table
     *
     * @param objects      list of objects
     * @param clazz        the type of the objects
     * @param sortable     true if the table's columns should be sortable
     * @param eventHandler if set, an Action column with a button will be added. Clicking the button will invoke the callback with the object passed in.
     * @param <T>          standard POJO
     * @return a table view containing the objects
     */
    public static <T> TableView build(List<T> objects, Class<T> clazz, boolean sortable, TableCallback<T> eventHandler) {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        List<Method> getterMethods = getValidGetters(clazz);

        for (Method method : getterMethods) {
            TableColumn<T, Boolean> tableColumn = new TableColumn<>(getName(method));
            if (!sortable) {
                tableColumn.setSortable(false);
            }

            tableColumn.getStyleClass().add("column-header-text");
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(getFieldName(method)));

            table.getColumns().add(tableColumn);
        }

        if (eventHandler != null) {
            EventHandler<ActionEvent> wrappedHandler = actionEvent -> {
                TableButton<T> button = (TableButton<T>) actionEvent.getSource();
                T object = button.getTableView().getItems().get(button.getButtonCell().getIndex());
                eventHandler.handle(object);
            };
            TableColumn actionColumn = new TableColumn<>("Action");
            table.getColumns().add(actionColumn);

            actionColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<?, Boolean>, ObservableValue<Boolean>>() {

                @Override
                public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<?, Boolean> p) {
                    return new SimpleBooleanProperty(p.getValue() != null);
                }
            });

            actionColumn.setCellFactory(p -> new ButtonCell<>(wrappedHandler, table, eventHandler.getActionName()));
        }

        table.setItems(FXCollections.observableList(objects));
        IntegerBinding size = Bindings.size(table.getItems());
        if (size.getValue() == 0) {
            size = size.add(1);
        }
        table.prefHeightProperty().bind(size.multiply(eventHandler == null ? 30 : 40).add(30));
        return table;
    }

    /**
     * Given a Node, convert it back into a POJO of type T
     *
     * @param node  the node
     * @param clazz the class to be read
     * @param <T>   parametrized type of the POJO
     * @return new instantiated class of type T populated with values from the node
     */
    public static <T> T read(Node node, Class<T> clazz) {
        Set<Node> values = node.lookupAll(".value");

        T result = getInstance(clazz);

        List<Method> getterMethods = getValidGetters(clazz);

        for (Method method : getterMethods) {
            handleSettingValueFromGetterMethod(values, result, method);
        }

        return result;
    }

    /**
     * Get the list of valid getters - that is, methods starting with "get" that take no arguments and are not annotated @Ignore
     *
     * @param clazz the class to examine
     * @param <T>   parametrized type
     * @return list of the methods
     */
    private static <T> List<Method> getValidGetters(Class<T> clazz) {
        List<Method> getterMethods = new ArrayList<>();
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getAnnotation(Ignore.class) != null) {
                continue;
            }

            if (validGetterMethod(method)) {
                getterMethods.add(method);
            }
        }
        return getterMethods;
    }

    /**
     * For the given getter, extract the value from the set of value nodes, and invoke the appropriate setter on the POJO.
     *
     * @param values the set of value nodes
     * @param result the POJO
     * @param method the getter method
     * @param <T>    parametrized type
     */
    private static <T> void handleSettingValueFromGetterMethod(Set<Node> values, T result, Method method) {
        String name = getName(method);

        Node valueNode = find(getStyleClass(name), values);

        if (valueNode == null) {
            return;
        }

        Method setter = getSetter(method);

        if (setter == null) {
            return;
        }

        IValueFieldConverter converter = getConverter(method);
        Object value = converter.toValue(valueNode, false);

        try {
            setter.invoke(result, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ComponentException("Could not set value on result object");
        }
    }


    /**
     * Find the first node matching the given styleclass
     *
     * @param styleClass the style class
     * @param values     the set of value nodes
     * @return the first node matching
     */
    private static Node find(String styleClass, Set<Node> values) {
        for (Node node : values) {
            if (node.getStyleClass().contains(styleClass)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Instantiate the given class, wrap any thrown exceptions in a component exception.
     *
     * @param clazz required class
     * @param <T>   parametrized type
     * @return the new instance
     */
    private static <T> T getInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ComponentException("Could not instantiate result object - does it have a default constructor?");
        }
    }

    /**
     * Build up a GUI for the given POJO
     *
     * @param object POJO to read
     * @return the GUI
     */
    public static Pane build(Object object) {
        return build(object, false);
    }


    /**
     * Build up a GUI from the given POJO, optionally read-only.
     *
     * @param object   POJO to read
     * @param readOnly true if e.g., labels should be produced instead of text fields.
     * @return the GUI
     */
    public static Pane build(Object object, boolean readOnly) {
        return build(object, readOnly, Layout.VERTICAL, null);
    }

    /**
     * Build up a GUI from the given POJO, using the provided Layout direction.
     *
     * @param object POJO to read
     * @param layout horizontal or vertical layout
     * @return the GUI
     */
//    public static Pane build(Object object, Layout layout) {
//        return build(object, false, layout, null);
//    }

    /**
     * Build up a GUI from the given POJO, as read-only or not, using the provided Layout direction.
     *
     * @param object   POJO to read
     * @param readOnly true if e.g., labels should be produced instead of text fields.
     * @param layout   horizontal or vertical layout
     * @return the GUI
     */
    public static Pane build(Object object, boolean readOnly, Layout layout) {
        return build(object, readOnly, layout, null);
    }

//    /**
//     * Build up a GUI from the given POJO, using the provided Layout direction
//     * and additional information provided by parameters.
//     *
//     * @param object           POJO to read
//     * @param layout           horizontal or vertical layout
//     * @param taggedParameters additional parameters
//     * @return the GUI
//     */
//    public static Node build(Object object, Layout layout, TaggedParameters taggedParameters) {
//        return build(object, false, layout, taggedParameters);
//    }

    /**
     * Build up a GUI from the given POJO, as read-only or not, using the provided Layout direction
     * and additional information provided by parameters.
     *
     * @param object   POJO to read
     * @param readOnly true if e.g., labels should be produced instead of text fields.
     * @param layout   horizontal or vertical layout
     * @param params   additional parameters
     * @return the GUI
     */
    public static Pane build(Object object, boolean readOnly, Layout layout, TaggedParameters params) {

        Method[] declaredMethods = object.getClass().getDeclaredMethods();
        Pane pane = getPane(layout);

        for (Method method : declaredMethods) {
            if (method.getAnnotation(Ignore.class) != null) {
                continue;
            }

            HBox methodBox = produceMethodNode(object, readOnly, params, method);

            pane.getChildren().add(methodBox);
        }
        return pane;
    }

    /**
     * Produce the node for the given method, with label and appropriate controls
     *
     * @param object   the POJO
     * @param readOnly true if e.g., labels should be produced instead of text fields.
     * @param params   additional parameters
     * @param method   the getter method
     * @return the GUI for this field
     */
    private static HBox produceMethodNode(Object object, boolean readOnly, TaggedParameters params, Method method) {
        HBox methodBox = new HBox();
        methodBox.setSpacing(10);

        if (validGetterMethod(method)) {
            String name = getName(method);
            Label label = new Label(name);
            label.getStyleClass().addAll("built-label");
            Node valueNode = getValueField(object, method, readOnly || getSetter(method) == null, name, params);

            methodBox.getChildren().addAll(label, valueNode);
        }
        return methodBox;
    }

    public static Object getField(Object object, Field field) throws IllegalAccessException {
        Object o = field.get(object);
        return o;
    }

    /**
     * Check if the method is a valid getter method - that is, does it begin with get and take no parameters.
     *
     * @param method the method
     * @return if it is valid
     */
    private static boolean validGetterMethod(Method method) {
        return method.getName().startsWith(GETTER_PREFIX) && method.getParameterTypes().length == 0;
    }

    /**
     * Get the type of Pane that should be return for the requeted layout
     *
     * @param layout horizontal or vertical layout
     * @return the appropriate Pane
     */
    private static Pane getPane(Layout layout) {
        switch (layout) {
            case VERTICAL:
                return new VBox();
            case HORIZONTAL:
                // fall through to default.
            default:
                HBox hBox = new HBox();
                hBox.setSpacing(10);
                return hBox;
        }
    }

    /**
     * Given a getter method, find the corresponding setter method or throw an exception if none could be found
     *
     * @param method the getter method
     * @return the setter method
     */
    private static Method getSetter(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?> returnType = method.getReturnType();
        String name = method.getName().replaceFirst(GETTER_PREFIX, "set");
        try {
            Method setterMethod = declaringClass.getMethod(name, returnType);
            if (setterMethod == null) {
                return null;
            }
            return setterMethod;
        } catch (NoSuchMethodException e) {
            return null;
            // should not be able to happen
            //throw new ComponentException("No such method exception invoking setter: " + name);
        }
    }

    /**
     * Get the field representing the value of the getter method
     *
     * @param object   the POJO
     * @param method   the getter method
     * @param readOnly true if e.g., labels should be produced instead of text fields.
     * @param name     the name to use for the field
     * @param params   additional parameters
     * @return the Node for the field's value
     */
    private static Node getValueField(Object object, Method method, boolean readOnly, String name, TaggedParameters params) {
        if (params == null) {
            params = new TaggedParameters();
        }

        try {
            Object result = getValue(object, method);

            IValueFieldConverter converter = getConverter(method);
            if (converter == null) {
                return new Label("Unknown: " + method);
            }
            String prompt = getPrompt(method);
            params.addTag("prompt", prompt);
            Node returned = converter.toNode(result, readOnly, params);
            returned.getStyleClass().addAll("value", getStyleClass(name));

            return returned;
        }
        catch (Exception e) {
            return new Label(e.toString());
        }
    }

    /**
     * Retrieve the appropriate converter for the method - either based on its return type, or a specific @Type annotation
     *
     * @param method the getter method
     * @return the appropriate converter
     */
    private static IValueFieldConverter getConverter(Method method) {
        IValueFieldConverter converter;
        Type annotation = method.getAnnotation(Type.class);
        if (annotation != null) {
            converter = annotation.value().getFieldConverter();
            converter.setTag(annotation.tag());
        } else {
            EType converterForClass = EType.getConverterForClass(method.getReturnType());
            if (converterForClass == null)
                return null;
            converter = converterForClass.getFieldConverter();
        }
        return converter;
    }

    /**
     * Convert a name to a style-class by lower-casing it and replacing spaces with dashes.
     *
     * @param name the name
     * @return the style-class
     */
    private static String getStyleClass(String name) {
        return name.replaceAll(" ", "-").toLowerCase();
    }

    /**
     * Attempt to get a value from an object by invoking a method, throw a ComponentException if
     * invoking the method caused an error
     *
     * @param object the POJO
     * @param method the getter method
     * @return the value
     */
    private static Object getValue(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ComponentException("Could not get value from object for method: " + method.getName());
        }
    }

    /**
     * Get the name of the field based on the getter method
     *
     * @param method the getter method
     * @return the field name
     */
    private static String getFieldName(Method method) {
        String result = method.getName().replaceAll(GETTER_PREFIX, "");
        return result.substring(0, 1).toLowerCase() + result.substring(1);
    }

    /**
     * Get the string that should be used as the component's label's text.
     *
     * @param method the method for the component
     * @return string for its label
     */
    private static String getName(Method method) {
        Name nameAnnotation = method.getAnnotation(Name.class);

        if (nameAnnotation != null) {
            return nameAnnotation.value();
        }

        return method.getName().replaceFirst(GETTER_PREFIX,
                "");
    }

    public static String getName(Field field) {
        Name nameAnnotation = field.getAnnotation(Name.class);

        if (nameAnnotation != null) {
            return nameAnnotation.value();
        }

        return field.getName().replaceFirst(GETTER_PREFIX, "");
    }

    /**
     * Get the string that should be used as the component's prompt text.
     *
     * @param method the method for the component
     * @return string for its prompt
     */
    public static String getPrompt(Method method) {
        Name nameAnnotation = method.getAnnotation(Name.class);

        if (nameAnnotation != null) {
            return nameAnnotation.prompt();
        }

        return "";
    }

    public static <X> Region valueToNode(X obj, Field parentValue) {


        if (obj instanceof ImplementationProperty) {
            ImplementationProperty d = (ImplementationProperty)obj;

            Class[] impls = null;

            if (parentValue instanceof Field) {
                Field p = parentValue;

                Implementations ii = p.getAnnotation(Implementations.class);
                if (ii!=null) {
                    Implementation[] iv = ii.value();
                    impls = new Class[iv.length+1];
                    impls[0] = None.class;//the null value option
                    for (int i = 0; i < iv.length; i++)
                        impls[i+1] = iv[i].value();

                }
            }
            ComboBox<Class> w = new ComboBox();
            w.setConverter(new StringConverter<Class>() {
                @Override
                public String toString(Class aClass) {
                    return aClass.getSimpleName();
                }

                @Override
                public Class fromString(String s) {
                    return null;
                }
            });

            if (impls == null) { w.setDisable(true); }
            else {
                w.getItems().addAll(impls);
            }
            d.bind(w.valueProperty());
            return w;
        }

        if (obj instanceof ObjectProperty) {

            ObjectProperty op = (ObjectProperty)obj;
            return new ObjectPropertyNode(op);
        }

        VBox ctl = new VBox();
        ObservableList<Node> chi = ctl.getChildren();
        ctl.getStyleClass().add("graphpopup"); //TODO revise these css rules


        if (obj instanceof StringProperty) {
            StringProperty d = (StringProperty)obj;

            TextField n = new TextField();
            n.setText(d.getValue());
            d.bind(n.textProperty());
            chi.add(n);
        }
        if (obj instanceof DoubleProperty) {
            DoubleProperty d = (DoubleProperty)obj;

            NSlider w = new NSlider("" /*shortLabel(parentValue)*/,
                    64, 16, NSlider.BarSlider, 0);


            applyFieldAnnotationsToNSlider(parentValue, w);
            w.value(d.getValue());
            d.bind(w.value[0]);
            chi.add(w);
        }

        final int nSliderHeight = 25;
        final int nSliderWidth = 75;
        if (obj instanceof AtomicDouble) {
            NSlider w;
            chi.add( w = new NSlider(nSliderWidth, nSliderHeight, (((AtomicDouble)obj).doubleValue()) ) {
                @Override public SimpleDoubleProperty newValueEntry(int i) {
                    return new MutableFloatProperty(((AtomicDouble)obj));
                }
            });
            applyFieldAnnotationsToNSlider(parentValue, w);
        }
        if (obj instanceof MutableFloat) {
            NSlider w;
            chi.add( w = new NSlider(nSliderWidth, nSliderHeight, (((MutableFloat)obj).doubleValue())) {
                @Override public SimpleDoubleProperty newValueEntry(int i) {
                    return new MutableFloatProperty(((MutableFloat)obj));
                }
            });
            applyFieldAnnotationsToNSlider(parentValue, w);
        }
        if (obj instanceof MutableInteger) {

            NSlider w;
            chi.add( w = new NSlider(nSliderWidth, nSliderHeight, (((MutableInteger)obj).intValue())) {
                @Override public SimpleDoubleProperty newValueEntry(int i) {
                    return new MutableFloatProperty((MutableInteger)obj);
                }
            });

            applyFieldAnnotationsToNSlider(parentValue, w);
        }
        /*if (chi.isEmpty()) {
            try {
                Node n = POJONode.propertyNodes(obj, taggedParameters);
                if (n != null)
                    chi.add(n);
            }
            catch (Throwable e) {
                System.err.println(e);
            }
        }*/
        return ctl;
    }

    public static void applyFieldAnnotationsToNSlider(Object parentValue, NSlider w) {
        double min = 0;
        double max = 1;
        if (parentValue instanceof Field) {
            Field p = (Field)parentValue;
            Range range = p.getDeclaredAnnotation(Range.class);
            if (range!=null) {
                min = range.min();
                max = range.max();
            }
        }
        w.min.set(min);
        w.max.set(max);
        w.addValueLabel();
    }

    private static String shortLabel(Object v) {

        if (v instanceof Field) {
            return ((Field)v).getName();
        }

        return v.toString();
    }

    /** //attach listener that updates content inside its own managed wrapper */
    public static class ObjectPropertyNode<O> extends VBox implements ChangeListener<O> {
        public ObjectPropertyNode(ObjectProperty<O> op) {
            op.addListener(this);
            changed(op, null, op.get());
        }

        @Override
        public void changed(ObservableValue<? extends O> observableValue, O o, O newValue) {
            if (newValue == null)
                getChildren().setAll(new Text("null"));
            else
                getChildren().setAll( POJOPane.propertyNodes( newValue ) );
        }
    }
}
