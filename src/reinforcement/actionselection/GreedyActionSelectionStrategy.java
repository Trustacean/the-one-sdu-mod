package reinforcement.actionselection;

import java.util.Set;

import reinforcement.models.QModel;
import reinforcement.utils.IndexValue;


/**
 * Created by xschen on 9/27/2015 0027.
 */
public class GreedyActionSelectionStrategy extends AbstractActionSelectionStrategy {
    @Override
    public IndexValue selectAction(int stateId, QModel model, Set<Integer> actionsAtState) {
        return model.actionWithMaxQAtState(stateId, actionsAtState);
    }

    @Override
    public Object clone(){
        GreedyActionSelectionStrategy clone = new GreedyActionSelectionStrategy();
        return clone;
    }

    @Override
    public boolean equals(Object obj){
        return obj != null && obj instanceof GreedyActionSelectionStrategy;
    }
}
