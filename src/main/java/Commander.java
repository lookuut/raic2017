import model.VehicleType;


import java.util.*;

class Commander {

    private ArmyDivisions divisions;
    protected MyStrategy strategy;
    protected BehaviourTree<ArmyAlly> behaviourTree;
    protected Queue<BTreeAction> activeActions;

    private static CommandNuclearAttack nuclearAttack;

    private Map<VehicleType, List<SmartVehicle>> noArmyVehicles;
    private Map<VehicleType, Square> noArmySquaereMap;

    /**
     * @desc all armies must be init in constructor
     * @param strategy
     */
    public Commander(MyStrategy strategy) {
        this.strategy = strategy;
        divisions = new ArmyDivisions();
        activeActions = new LinkedList<>();
        behaviourTree = new BehaviourTree<>();
        noArmyVehicles = new HashMap<>();
        noArmySquaereMap = new HashMap<>();
    }


    public void logic (BattleField battleField) throws Exception {
        constructArmies();

        nuclearAttack();
        //run divisions logic
        for (ArmyAllyOrdering army : divisions.getArmyList()) {
            if (army.isArmyAlive()) {
                army.run(battleField);
                army.check();
            }
        }
    }

    public void constructArmies() {
        for (Map.Entry<VehicleType, List<SmartVehicle>> entry : noArmyVehicles.entrySet()) {
            try {
                if (entry.getValue().size() > CustomParams.minVehiclesCountInArmy) {
                    Square vehicleTypeSquare = noArmySquaereMap.get(entry.getKey());

                    if (entry.getKey() == VehicleType.ARRV && entry.getKey() != VehicleType.HELICOPTER && entry.getValue().size() > CustomParams.maxVehiclesCountInArmy) {
                        double centreX = (vehicleTypeSquare.getRightTopAngle().getX() - vehicleTypeSquare.getLeftBottomAngle().getX()) / 2;
                        double centreY = (vehicleTypeSquare.getRightTopAngle().getY() - vehicleTypeSquare.getLeftBottomAngle().getY()) / 2;
                        Point2D centrePoint = new Point2D(vehicleTypeSquare.getLeftBottomAngle().getX() + centreX, vehicleTypeSquare.getLeftBottomAngle().getY() + centreY );

                        Square armySquare = new Square(vehicleTypeSquare.getLeftBottomAngle(), centrePoint);
                        divisions.addArmy(armySquare, entry.getKey());

                        armySquare = new Square(centrePoint, vehicleTypeSquare.getRightTopAngle());
                        divisions.addArmy(armySquare, entry.getKey());

                        armySquare = new Square(new Point2D(vehicleTypeSquare.getLeftBottomAngle().getX(), centrePoint.getY()), new Point2D(centrePoint.getX(), vehicleTypeSquare.getRightTopAngle().getY()));
                        divisions.addArmy(armySquare, entry.getKey());

                        armySquare = new Square(new Point2D(centrePoint.getX(), vehicleTypeSquare.getLeftBottomAngle().getY()), new Point2D(vehicleTypeSquare.getRightTopAngle().getX(), centrePoint.getY()));
                        divisions.addArmy(armySquare, entry.getKey());
                    } else if (entry.getKey() == VehicleType.FIGHTER ||
                            entry.getKey() == VehicleType.HELICOPTER ||
                            entry.getKey() == VehicleType.TANK ||
                            entry.getKey() == VehicleType.IFV) {
                        divisions.addArmy(vehicleTypeSquare, entry.getKey());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        noArmyVehicles.clear();
    }

    public void check () {
        for (ArmyAllyOrdering army : divisions.getArmyList()) {
            army.check();
        }
    }

    public ArmyDivisions getDivisions() {
        return divisions;
    }

    public void nuclearAttack () {
        try {

            if (isDelayNuclearAttack() || MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() > 0) {
                return;
            }

            for (NuclearAttackPoint attackPoint : MyStrategy.enemyField.getNuclearAttackPointsRating()) {
                for (ArmyAllyOrdering army : divisions.getArmyList()) {
                    if (army.getForm().isPointInDistance(attackPoint.getPoint(), MyStrategy.game.getTacticalNuclearStrikeRadius())) {//point in allow distance need cabum
                        SmartVehicle vehicle = army.getGunnerVehicle(attackPoint.getPoint());
                        Point2D fromPoint = vehicle.getPoint();

                        BattleFieldCell battleFieldCell = MyStrategy.battleField.getBattleFieldCell(MyStrategy.battleField.pointTransform(attackPoint.getPoint()));
                        Map<Long, SmartVehicle> vehicles = battleFieldCell.getVehicles(MyStrategy.getEnemyPlayerId());

                        double vehicleSpeed = 0;
                        if (vehicles != null && vehicles.size() > 0) {
                            SmartVehicle firstVehicle = vehicles.values().stream().findFirst().orElse(null);
                            if (firstVehicle != null) {
                                vehicleSpeed = firstVehicle.getVehicleOnTickSpeed();
                            }
                        }

                        Point2D targetVector = attackPoint.getPoint().subtract(fromPoint);

                        if (vehicleSpeed > 0) {
                            targetVector.multiply(vehicleSpeed * MyStrategy.game.getTacticalNuclearStrikeDelay());
                        }

                        double visionRange = vehicle.getMinVisionRange();

                        if (army.getRunningCommand() instanceof  CommandMove) {
                            CommandMove command = ((CommandMove) army.getRunningCommand());
                            Point2D moveTargetVector = command.getTargetVector();
                            double angle = moveTargetVector.angle(targetVector);
                            if (angle >= 90 && angle < 270) {
                                fromPoint = vehicle.getVehiclePointAtTick(moveTargetVector.normalize(), Math.min(MyStrategy.game.getTacticalNuclearStrikeDelay(),command.getMaxRunnableTick()));
                            }
                        }

                        Point2D targetPoint = attackPoint.getPoint();
                        Point2D targetPointDirection = attackPoint.getPoint().subtract(fromPoint);

                        if (targetPointDirection.magnitude() > visionRange) {
                            targetPoint = targetPointDirection.multiply(visionRange / targetPointDirection.magnitude()).add(fromPoint);
                        }

                        nuclearAttack = new CommandNuclearAttack(vehicle, targetPoint);
                        nuclearAttack.run(army);
                        break;
                    }
                }
            }
        } catch (Exception e)  {
            e.printStackTrace();
        }
    }

    public void addNoArmyVehicle(SmartVehicle vehicle){
        if (vehicle.isAlly()) {
            if (!noArmyVehicles.containsKey(vehicle.getType())){
                noArmyVehicles.put(vehicle.getType(), new ArrayList<>());
                noArmySquaereMap.put(vehicle.getType(), new Square(vehicle.getLeftBottomAngle(), vehicle.getRightTopAngle()));
            } else {
                Square square = noArmySquaereMap.get(vehicle.getType());
                square.addPoint(vehicle.getLeftBottomAngle());
                square.addPoint(vehicle.getRightTopAngle());
            }

            List<SmartVehicle> vehicleList = noArmyVehicles.get(vehicle.getType());
            vehicleList.add(vehicle);
        }
    }

    static public boolean isDelayNuclearAttack () {
        if (Commander.nuclearAttack == null || Commander.nuclearAttack.isFinished()) {
            return false;
        }

        return Commander.nuclearAttack.check(null);
    }
}