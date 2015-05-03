package nars.rl.horde.functions;

import java.io.Serializable;

public interface RewardFunction extends Serializable {
    double reward();
}
