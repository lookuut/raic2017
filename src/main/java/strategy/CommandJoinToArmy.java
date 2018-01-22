package strategy;

import model.ActionType;

import java.util.function.Consumer;

public class CommandJoinToArmy extends Command {

        public CommandJoinToArmy() {
        }

        public boolean check (ArmyAllyOrdering army) {
            return false;
        }

        @Override
        public void prepare(ArmyAllyOrdering army) throws Exception {}

        public void run(ArmyAllyOrdering army) throws Exception {

        }

        @Override
        public void pinned(){
            super.pinned();
        }

        @Override
        public void processing(SmartVehicle vehicle) {

        }
    }
