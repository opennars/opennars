package nars.rl.horde.functions;

import java.io.Serializable;

public interface OutcomeFunction extends Serializable {
    double outcome();
}
