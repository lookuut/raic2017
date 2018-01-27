package strategy;

import model.VehicleType;

import java.util.*;
import java.util.stream.Collectors;

public class BattleField {

    protected BattleFieldCell[][] battleField;

    protected int pFieldWidth;
    protected int pFieldHeight;
    
    private Map<Long, List<Army>> armies;
    private int armiesDefineTick = -1;

    //temporary code remove it before upload
    private Integer cellSize;

    public BattleField (Integer cellSize) {
        this.cellSize = cellSize;

        pFieldWidth = (int)(Math.ceil(MyStrategy.game.getWorldWidth() / cellSize));
        pFieldHeight = (int)(Math.ceil(MyStrategy.game.getWorldHeight() / cellSize));

        battleField = new BattleFieldCell[pFieldHeight][pFieldWidth];

        for (int y = 0; y < pFieldHeight; y++) {
            for (int x = 0; x < pFieldWidth; x++) {
                this.battleField[y][x] = new BattleFieldCell(x,y);
            }
        }
        armies = new HashMap<>();
        armies.put(MyStrategy.getEnemyPlayerId(), new ArrayList<>());
        armies.put(MyStrategy.player.getId(), new ArrayList<>());
    }

    public Point2D pointTransform(Point2D point) {
        return new Point2D((int)Math.floor(point.getX() / cellSize), (int)Math.floor(point.getY() / cellSize));
    }

    public void addVehicle(SmartVehicle vehicle) {

        if (vehicle.isAlly() && (vehicle.getDurability() == 0 || vehicle.isVehicleMoved())) {
            ArmyAllyOrdering army = vehicle.getArmy();
            if (army != null) {
                Command command = army.getRunningCommand();
                if (command != null) {
                    army.getRunningCommand().processing(vehicle);
                }
            }
        }

        Integer battleFieldX = (int)Math.floor(vehicle.getX() / cellSize);
        Integer battleFieldY = (int)Math.floor(vehicle.getY() / cellSize);

        BattleFieldCell battleFieldCell =  this.battleField[battleFieldY][battleFieldX];
        BattleFieldCell vehicleBattleFieldCell = vehicle.getBattleFieldCell();

        if ((
                vehicleBattleFieldCell != null &&
                        (vehicleBattleFieldCell != battleFieldCell
                        || vehicle.getDurability() == 0
                        || vehicle.isDurabilityChanched()
                        || vehicle.isAttackCooldownChanched()
                        ))
                ) {

            vehicleBattleFieldCell.remove(MyStrategy.getVehiclePrevState(vehicle.getId()));

            Integer lastVehicleX = vehicleBattleFieldCell.getX();
            Integer lastVehicleY = vehicleBattleFieldCell.getY();

            MyStrategy.enemyField.removeFromCellVehicle(lastVehicleX, lastVehicleY, MyStrategy.getVehiclePrevState(vehicle.getId()));

        }

        if (vehicle.getDurability() == 0) {
            vehicleBattleFieldCell.remove(vehicle);
        }

        if (vehicle.getDurability() > 0 &&
                (
                        (
                                vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell)
                        )
                    || vehicle.isDurabilityChanched()
                    || vehicle.isAttackCooldownChanched()
                )
                ) {
            battleFieldCell.addVehicle(vehicle);
            MyStrategy.enemyField.addVehicleToCell(battleFieldX, battleFieldY, vehicle);
        }

        vehicle.setBattleFieldCell(battleFieldCell);
     }


    public int getWidth() {
        return pFieldWidth;
    }

    public int getHeight() {
        return pFieldHeight;
    }

    public BattleFieldCell getBattleFieldCell(int x, int y) {
        return battleField[y][x];
    }

    public BattleFieldCell getBattleFieldCell(Point2D point) {
        return battleField[point.getIntY()][point.getIntX()];
    }

    public void addNearVehicles(SmartVehicle vehicle, Queue<Long> toVisitVehicleIds, Set<Long> visitedVehicleIds, Set<Long> haveArmyVehicles, Set<Long> edgesVehicles, Army army) {
        army.addVehicle(vehicle);
        haveArmyVehicles.add(vehicle.getId());

        while (toVisitVehicleIds.size() > 0) {
            Long vehicleId = toVisitVehicleIds.poll();

            if (!visitedVehicleIds.contains(vehicleId)) {
                visitedVehicleIds.add(vehicleId);
                SmartVehicle nVehicle = MyStrategy.getVehicles().get(vehicleId);
                if (nVehicle.getNearVehicles().size() >= CustomParams.groupMinItemCount) {
                    toVisitVehicleIds.addAll(nVehicle.getNearVehicles());
                }
            }

            if (!haveArmyVehicles.contains(vehicleId)) {
                haveArmyVehicles.add(vehicleId);
                army.addVehicle(MyStrategy.getVehicles().get(vehicleId));
                if (edgesVehicles.contains(vehicleId)) {
                    edgesVehicles.remove(vehicleId);
                }
            }
        }
    }

    public void clusteringVehicles(Collection<SmartVehicle> clusteringVehicles, Long playerId) {

        armies.get(playerId).clear();

        Set<Long> edgesVehicles = new HashSet<>();
        Set<Long> haveArmyVehicles = new HashSet<>();
        Set<Long> visitedVehicles = new HashSet<>();

        for (SmartVehicle vehicle : clusteringVehicles) {
            if (visitedVehicles.contains(vehicle.getId())) {
                continue;
            }
            visitedVehicles.add(vehicle.getId());

            if (vehicle.getNearVehicles().size() < CustomParams.groupMinItemCount) {
                edgesVehicles.add(vehicle.getId());
            } else {
                Army army = new Army();
                Queue<Long> queue = vehicle.getNearVehicles().stream().collect(Collectors.toCollection(() -> new LinkedList<>()));
                addNearVehicles(vehicle, queue,
                        visitedVehicles,
                        haveArmyVehicles,
                        edgesVehicles, army);

                army.getForm().update(army.getVehicles());
                armies.get(playerId).add(army);
            }
        }

        if (edgesVehicles.size() > 0) {
            for (Long vehicleId : edgesVehicles) {
                if (!haveArmyVehicles.contains(vehicleId)) {
                    haveArmyVehicles.add(vehicleId);
                    Army army = new Army();
                    army.addVehicle(MyStrategy.getVehicles().get(vehicleId));
                    for (Long nearVehicleId : MyStrategy.getVehicles().get(vehicleId).getNearVehicles()) {
                        if (!haveArmyVehicles.contains(nearVehicleId)) {
                            army.addVehicle(MyStrategy.getVehicles().get(nearVehicleId));
                            haveArmyVehicles.add(nearVehicleId);
                        }
                    }

                    army.getForm().update(army.getVehicles());
                    armies.get(playerId).add(army);
                }
            }
        }
    }

    public void defineArmies() {

        if (armiesDefineTick == MyStrategy.world.getTickIndex()) {
            return;
        }

        /*
        if (MyStrategy.world.getTickIndex() - armiesDefineTick < CustomParams.enemyArmiesDefineInterval) {
            return;
        }*/

        clusteringVehicles(MyStrategy.getAllyVehicles().values(),
                MyStrategy.player.getId()
        );

        clusteringVehicles(MyStrategy.getEnemyVehicles().values(),
                MyStrategy.getEnemyPlayerId()
        );

        armiesDefineTick = MyStrategy.world.getTickIndex();
    }

    public List<Army> getEnemyArmies() {
        return armies.get(MyStrategy.getEnemyPlayerId());
    }

    public List<Army> getAllyArmies() {
        return armies.get(MyStrategy.player.getId());
    }

    public BattleFieldCell searchTargetEnemiesAround(int radious, Point2D point, Set<VehicleType> vehiclesTypes) {
        for (int i = -radious; i <= radious; i++) {
            for (int j = -radious; j <= radious; j++) {
                if (i * i + j * j <= radious * radious &&
                        point.getIntX() + i >= 0 &&
                        point.getIntX() + i < getWidth() &&
                        point.getIntY() + j >= 0 &&
                        point.getIntY() + j < getHeight()) {

                    for (VehicleType vehicleType : vehiclesTypes) {
                        if (getBattleFieldCell(point.getIntX() + i, point.getIntY() + j).getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                            VehicleType enemyVehicleType = getBattleFieldCell(point.getIntX() + i, point.getIntY() + j).getVehicles(MyStrategy.getEnemyPlayerId()).values().iterator().next().getType();
                            if (SmartVehicle.isTargetVehicleType(vehicleType, enemyVehicleType)) {
                                return getBattleFieldCell(point.getIntX() + i, point.getIntY() + j);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public BattleFieldCell searchEnemiesAround(int radious, Point2D point, Set<VehicleType> vehiclesTypes) {
        for (int i = -radious; i <= radious; i++) {
            for (int j = -radious; j <= radious; j++) {
                if (i * i + j * j <= radious * radious &&
                        point.getIntX() + i >= 0 &&
                        point.getIntX() + i < getWidth() &&
                        point.getIntY() + j >= 0 &&
                        point.getIntY() + j < getHeight()) {

                    for (VehicleType vehicleType : vehiclesTypes) {
                        if (getBattleFieldCell(point.getIntX() + i, point.getIntY() + j).getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                            return getBattleFieldCell(point.getIntX() + i, point.getIntY() + j);
                        }
                    }
                }
            }
        }
        return null;
    }
}
