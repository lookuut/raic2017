import model.VehicleType;

import java.util.*;

public class Track {

    private SortedMap<Integer, Map<Integer, Step>> terrainTrackMap;
    private SortedMap<Integer, Map<Integer, Step>> aerialTrackMap;

    public Track() {
        terrainTrackMap = new TreeMap<>();
        aerialTrackMap = new TreeMap<>();
    }

    public void addStep(Integer tick, Step step, VehicleType type) {
        SortedMap<Integer, Map<Integer, Step>> trackMap = getVehicleTypeTrack(type);
        if (!trackMap.containsKey(tick)) {
            trackMap.put(tick, new HashMap<>());
        }

        if (trackMap.get(tick).containsKey(step.getIndex())) {
            step.addPower(trackMap.get(tick).get(step.getIndex()).getPower());
        }
        trackMap.get(tick).put(step.getIndex(), step);
    }

    public SortedMap<Integer, Map<Integer, Step>> getVehicleTypeTrack(VehicleType type) {
        if (SmartVehicle.isTerrain(type)) {
            return terrainTrackMap;
        }
        return aerialTrackMap;
    }

    public void addTrack(Track sourceTrack, Integer tick) {
        aerialTrackMap = sumTrackMap(sourceTrack.aerialTrackMap, this.aerialTrackMap, tick, 1);
        terrainTrackMap = sumTrackMap(sourceTrack.terrainTrackMap, this.terrainTrackMap, tick, 1);
    }

    public void addLastTick(Track sourceTrack) {
        aerialTrackMap = sumTrackMap(sourceTrack.aerialTrackMap, this.aerialTrackMap, sourceTrack.getLastAerialTick() - 1, 1);
        terrainTrackMap = sumTrackMap(sourceTrack.terrainTrackMap, this.terrainTrackMap, sourceTrack.getLastTerrainTick() - 1, 1);
    }

    public Integer getLastAerialTick () {
        return aerialTrackMap.size() == 0 ? 0 : aerialTrackMap.lastKey();
    }

    public Integer getLastTerrainTick () {
        return terrainTrackMap.size() == 0 ? 0 : terrainTrackMap.lastKey();
    }

    public void minusTrack(Track fromTrack, Track minusTrack, Integer tick) {
        aerialTrackMap = sumTrackMap(fromTrack.aerialTrackMap, minusTrack.aerialTrackMap, tick, -1);
        terrainTrackMap = sumTrackMap(fromTrack.terrainTrackMap, minusTrack.terrainTrackMap, tick, -1);
    }

    public SortedMap<Integer, Map<Integer, Step>> sumTrackMap(
            SortedMap<Integer, Map<Integer, Step>> fromTrackMap,
            SortedMap<Integer, Map<Integer, Step>> usageTrackMap,
            Integer tick,
            int operator)
    {
        SortedMap<Integer, Map<Integer, Step>> newTrackMap = new TreeMap<>();
        HashSet<Integer> keys = new HashSet<>();
        keys.addAll(fromTrackMap.keySet());
        keys.addAll(usageTrackMap.keySet());

        for (Integer tickItem : keys) {

            if (tickItem < tick ) {//cut nose
                continue;
            }
            Map<Integer, Step> newStepTrackMap;
            if (!newTrackMap.containsKey(tickItem)) {
                newStepTrackMap = new TreeMap<>();
                newTrackMap.put(tickItem, newStepTrackMap);
            } else {
                newStepTrackMap = newTrackMap.get(tickItem);
            }

            if (fromTrackMap.containsKey(tickItem) && usageTrackMap.containsKey(tickItem)) {
                for (Map.Entry<Integer, Step> stepEntry : fromTrackMap.get(tickItem).entrySet()){
                    Integer power = operator * stepEntry.getValue().getPower();
                    if (usageTrackMap.containsKey(tickItem) && usageTrackMap.get(tickItem).containsKey(stepEntry.getKey())) {
                        power += usageTrackMap.get(tickItem).get(stepEntry.getKey()).getPower() ;
                    }
                    newStepTrackMap.put(stepEntry.getKey(), new Step(stepEntry.getValue().getPoint(), power));
                }
            } else if (fromTrackMap.containsKey(tickItem)) {
                newTrackMap.put(tickItem, new HashMap<>(fromTrackMap.get(tickItem)));
            } else if (usageTrackMap.containsKey(tickItem)) {
                newTrackMap.put(tickItem, new HashMap<>(usageTrackMap.get(tickItem)));
            }
        }

        return newTrackMap;
    }

    public Map<Integer, Step> sumTracks(Set<VehicleType> types, Integer tick) {
        SortedMap<Integer, Map<Integer, Step>> newTrackMap = null;

        if (types.contains(VehicleType.ARRV) || types.contains(VehicleType.TANK) || types.contains(VehicleType.IFV)) {
            newTrackMap = getVehicleTypeTrack(VehicleType.TANK);
        }

        if (types.contains(VehicleType.HELICOPTER) || types.contains(VehicleType.FIGHTER)) {
            if (newTrackMap != null) {//sum
                sumTrackMap(this.aerialTrackMap, newTrackMap, tick, 1);
            }
        }

        return newTrackMap.get(tick);
    }

    public void clearPast(Integer tick) {
        this.terrainTrackMap.headMap(tick).clear();
        this.aerialTrackMap.headMap(tick).clear();
    }

    public void clearFuture(Integer tick) {
        this.terrainTrackMap.tailMap(tick).clear();
        this.aerialTrackMap.tailMap(tick).clear();
    }


}
