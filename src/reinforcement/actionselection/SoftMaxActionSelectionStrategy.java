package reinforcement.actionselection;

import java.util.Random;
import java.util.Set;

import reinforcement.models.QModel;
import reinforcement.utils.IndexValue;


/**
 * Created by xschen on 9/27/2015 0027.
 */
public class SoftMaxActionSelectionStrategy extends AbstractActionSelectionStrategy {
    private Random random = new Random();

    @Override
    public Object clone(){
        SoftMaxActionSelectionStrategy clone = new SoftMaxActionSelectionStrategy(random);
        return clone;
    }

    @Override
    public boolean equals(Object obj){
        return obj != null && obj instanceof SoftMaxActionSelectionStrategy;
    }

    public SoftMaxActionSelectionStrategy(){

    }

    public SoftMaxActionSelectionStrategy(Random random){
        this.random = random;
    }

    @Override
    public IndexValue selectAction(int stateId, QModel model, Set<Integer> actionsAtState) {
        return model.actionWithSoftMaxQAtState(stateId, actionsAtState, random);
    }
}
