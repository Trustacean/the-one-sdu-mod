package reinforcement.actionselection;

import java.io.Serializable;
// import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import reinforcement.models.QModel;
import reinforcement.models.UtilityModel;
import reinforcement.utils.IndexValue;


/**
 * Created by xschen on 9/27/2015 0027.
 */
public interface ActionSelectionStrategy extends Serializable, Cloneable {
    IndexValue selectAction(int stateId, QModel model, Set<Integer> actionsAtState);
    IndexValue selectAction(int stateId, UtilityModel model, Set<Integer> actionsAtState);
    String getPrototype();
    Map<String, String> getAttributes();
}
