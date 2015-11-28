package nars.rl.gng;

import nars.util.data.id.Named;

import java.util.Objects;

/**
 * Created by Scadgek on 11/3/2014.
 */
public class Connection implements Named<String> {
    public final Node from;
    public final Node to;
    private int age;

    public Connection(Node from, Node to) {
        //sort by id
        if (from.id > to.id) {
            Node t = to;
            to = from;
            from = t;
        }

        this.age = 0;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        Connection c = (Connection)obj;
        return from.id == c.from.id && to.id == c.to.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from.id, to.id);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void age() {
        age += 1;
    }

    @Override
    public String name() {
        return from.id + ":" + to.id;
    }
}
