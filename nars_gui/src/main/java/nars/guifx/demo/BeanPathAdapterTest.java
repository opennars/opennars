package nars.guifx.demo;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nars.NAR;
import nars.nar.Default;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanProperty;
import org.controlsfx.property.BeanPropertyUtils;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Created by me on 10/2/15.
 */
public class BeanPathAdapterTest extends Application {





    public static ObservableList<PropertySheet.Item> getProperties(Object bean, Predicate<PropertyDescriptor> test) {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
            for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
                if (test.test(p)) {
                    list.add(new BeanProperty(bean, p));
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }


        return list;
    }


    /** advanced widgetization of data objects */
    public static class Fxizer extends DefaultPropertyEditorFactory {

        public Fxizer() {

        }

        @Override
        public PropertyEditor<?> call(PropertySheet.Item item) {
            /*
            if ((type == boolean.class || type == Boolean.class)) {
                return Editors.createCheckEditor(item);
            }*/

            return super.call(item);
        }
    }

    @Override
    public void start(Stage w) throws Exception {
        PropertySheet p = new PropertySheet();
        p.setMode(PropertySheet.Mode.NAME);
        p.setModeSwitcherVisible(false);
        p.getPropertyEditorFactory().call(new MyItem());

        p.getItems().addAll(
                BeanPropertyUtils.getProperties(
                        new SampleBean()
                )
        );
        w.setScene(new Scene(p, 500,300));
        w.show();
    }

    public static void main(String[] args) {
        Application.launch();
    }

    private static class MyItem implements PropertySheet.Item {

        @Override
        public Class<?> getType() {
            return null;
        }

        @Override
        public String getCategory() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void setValue(Object value) {

        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return null;
        }
    }

    public static class SampleBean {

        public final NAR n = new Default();

        private String id = UUID.randomUUID().toString();
        private String firstName;
        private String lastName;

        private String hiddenValue;
        private int age;
        boolean alive;

        public String x = "x";

        public boolean isAlive() {
            return alive;
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }

        List<String> items = new ArrayList();

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }

        public SampleBean() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }

        /**
         * @return the firstName
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         * @param firstName the firstName to set
         */
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        /**
         * @return the lastName
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }


        /**
         * @return the hiddenValue
         */
        public String getHiddenValue() {
            return hiddenValue;
        }

        /**
         * @param hiddenValue the hiddenValue to set
         */
        public void setHiddenValue(String hiddenValue) {
            this.hiddenValue = hiddenValue;
        }

    }
}
