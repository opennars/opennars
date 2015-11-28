package nars.rover.robot;

import org.jbox2d.dynamics.contacts.Contact;

/**
 * Created by me on 7/19/15.
 */
public interface Collidable {


    void onCollision(Contact c);
}
