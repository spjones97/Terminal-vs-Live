package com.c1games.terminal.starteralgo;
import com.c1games.terminal.algo.*;
import com.c1games.terminal.algo.io.GameLoop;
import com.c1games.terminal.algo.io.GameLoopDriver;
import com.c1games.terminal.algo.map.GameState;
import com.c1games.terminal.algo.map.MapBounds;
import com.c1games.terminal.algo.map.Unit;
import com.c1games.terminal.algo.units.UnitType;

import java.util.*;
/**
 * Java implementation of the standard starter algo.
 */
public class StarterAlgo implements GameLoop {
    public static void main(String[] args) {
        new GameLoopDriver(new StarterAlgo()).run();
    }
    private static final Coords[] filterProtectDestructors = {
            new Coords(8, 9),
            new Coords(9, 9),
            new Coords(10, 9),
            new Coords(11, 9),
            new Coords(12, 9),
            new Coords(13, 9),
            new Coords(14, 9),
            new Coords(15, 9),
            new Coords(16, 9),
            new Coords(17, 9),
            new Coords(18, 9)
    };

    private static final Coords[] defensiveDestructorLocations = {
            new Coords(0, 13),
            new Coords(27, 13),
            new Coords(1, 13),
            new Coords(26, 13),
            new Coords(22, 11),
            new Coords(5, 11),
            new Coords(25, 12),
            new Coords(13, 11),
            new Coords(14, 11),
            new Coords(15, 13),
            new Coords(12, 13),
            new Coords(23, 11),
            new Coords(4, 11),
            new Coords(8, 8),
            new Coords(19, 8),
            new Coords(9, 7),
            new Coords(18, 7),
            new Coords(5, 10),
            new Coords(22, 10)
    };
    private static final Coords[] encryptorLocations = {
            new Coords(8, 10),
            new Coords(19, 10),
            new Coords(13, 10),
            new Coords(14, 10),
    };
    private final Random rand = new Random();
    private ArrayList<Coords> scoredOnLocations = new ArrayList<>();
    @Override
    public void initialize(GameIO io, Config config) {
        GameIO.debug().println("Configuring your custom java algo strategy...");
        long seed = rand.nextLong();
        rand.setSeed(seed);
        GameIO.debug().println("Set random seed to: " + seed);
    }
    /**
     * Make a move in the game.
     */
    @Override
    public void onTurn(GameIO io, GameState move) {
        GameIO.debug().println("Performing turn " + move.data.turnInfo.turnNumber + " of your custom algo strategy");
        buildDefenses(move);
        buildReactiveDefenses(move);
        if (move.data.turnInfo.turnNumber % 10 == 0) {
            deployRandomPings(move);
            Coords bestLoc = leastDamageSpawnLocation(move, List.of(new Coords(13, 0), new Coords(14, 0)));
            for (int i = 0; i < 100; i++) {
                move.attemptSpawnMultiple(Arrays.asList(encryptorLocations), UnitType.Encryptor);
                move.attemptSpawn(bestLoc,UnitType.Ping);
            }
        } else {
            empLineStrategy(move);
        }
    }
    /**
     * Save process action frames. Careful there are many action frames per turn!
     */
    @Override
    public void onActionFrame(GameIO io, GameState move) {
        // Save locations that the enemy scored on against us to reactively build defenses
        for (FrameData.Events.BreachEvent breach : move.data.events.breach) {
            if (breach.unitOwner != PlayerId.Player1) {
                scoredOnLocations.add(breach.coords);
            }
        }
    }

    /**
     * Once the C1 logo is made, attempt to build some defenses.
     */
    private void buildDefenses(GameState move) {
        /*
        First lets protect ourselves a little with destructors.
         */
        move.attemptSpawnMultiple(Arrays.asList(defensiveDestructorLocations), UnitType.Destructor);
        move.attemptSpawnMultiple(Arrays.asList(encryptorLocations), UnitType.Encryptor);

        /*
        Lets protect our destructors with some filters.
         */
        if (move.data.turnInfo.turnNumber % 5 == 0) {
            move.attemptSpawnMultiple(Arrays.asList(filterProtectDestructors), UnitType.Filter);
        }
        /*
        Lastly, lets upgrade those important filters that protect our destructors.
         */
        move.attemptUpgradeMultiple(Arrays.asList(filterProtectDestructors));
    }

    /**
     * Build defenses reactively based on where we got scored on
     */
    private void buildReactiveDefenses(GameState move) {
        for (Coords loc : scoredOnLocations) {
            // Build 1 space above the breach location so that it doesn't block our spawn locations
            move.attemptSpawn(new Coords(loc.x, loc.y +1), UnitType.Destructor);
        }
    }

    /**
     * Deploy offensive units.
     */
    private void deployRandomScramblers(GameState move) {
        /*
        Lets send out Scramblers to help destroy enemy information units.
        A complex algo would predict where the enemy is going to send units and
        develop its strategy around that. But this algo is simple so lets just
        send out scramblers in random locations and hope for the best.

        Firstly information units can only deploy on our edges. So lets get a
        list of those locations.
         */
        List<Coords> friendlyEdges = new ArrayList<>();
        friendlyEdges.addAll(Arrays.asList(MapBounds.EDGE_LISTS[MapBounds.EDGE_BOTTOM_LEFT]));
        friendlyEdges.addAll(Arrays.asList(MapBounds.EDGE_LISTS[MapBounds.EDGE_BOTTOM_RIGHT]));

        /*
        While we have remaining bits to spend lets send out scramblers randomly.
        */
        while (move.numberAffordable(UnitType.Scrambler) >= 1) {
            Coords c = friendlyEdges.get(rand.nextInt(friendlyEdges.size()));
            move.attemptSpawn(c, UnitType.Scrambler);
            /*
            We don't have to remove the location since multiple information
            units can occupy the same space. Note however, if all edge locations are blocked this will infinite loop!
             */
        }
    }

    private void deployRandomPings(GameState move) {
        /*
        Lets send out Scramblers to help destroy enemy information units.
        A complex algo would predict where the enemy is going to send units and
        develop its strategy around that. But this algo is simple so lets just
        send out scramblers in random locations and hope for the best.

        Firstly information units can only deploy on our edges. So lets get a
        list of those locations.
         */
        List<Coords> friendlyEdges = new ArrayList<>();
        friendlyEdges.addAll(Arrays.asList(MapBounds.EDGE_LISTS[MapBounds.EDGE_BOTTOM_LEFT]));
        friendlyEdges.addAll(Arrays.asList(MapBounds.EDGE_LISTS[MapBounds.EDGE_BOTTOM_RIGHT]));

        /*
        While we have remaining bits to spend lets send out scramblers randomly.
        */
        while (move.numberAffordable(UnitType.Ping) >= 1) {
            Coords c = friendlyEdges.get(rand.nextInt(friendlyEdges.size()));
            move.attemptSpawn(c, UnitType.Ping);
            /*
            We don't have to remove the location since multiple information
            units can occupy the same space. Note however, if all edge locations are blocked this will infinite loop!
             */
        }
    }

    /**
     * Goes through the list of locations, gets the path taken from them,
     * and loosely calculates how much damage will be taken by traveling that path assuming speed of 1.
     * @param move
     * @param locations
     * @return
     */
    private Coords leastDamageSpawnLocation(GameState move, List<Coords> locations) {
        List<Float> damages = new ArrayList<>();

        for (Coords location : locations) {
            List<Coords> path = move.pathfind(location, MapBounds.getEdgeFromStart(location));
            float totalDamage = 0;
            for (Coords dmgLoc : path) {
                List<Unit> attackers = move.getAttackers(dmgLoc);
                for (Unit unit : attackers) {
                    totalDamage += unit.unitInformation.attackDamageWalker.orElse(0);
                }
            }
            GameIO.debug().println("Got dmg:" + totalDamage + " for " + location);
            damages.add(totalDamage);
        }

        int minIndex = 0;
        float minDamage = 9999999;
        for (int i = 0; i < damages.size(); i++) {
            if (damages.get(i) <= minDamage) {
                minDamage = damages.get(i);
                minIndex = i;
            }
        }
        return locations.get(minIndex);
    }

    /**
     * Counts the number of a units found with optional parameters to specify what locations and unit types to count.
     * @param move GameState
     * @param xLocations Can be null, list of x locations to check for units
     * @param yLocations Can be null, list of y locations to check for units
     * @param units Can be null, list of units to look for, null will check all
     * @return count of the number of units seen at the specified locations
     */
    private int detectEnemyUnits(GameState move, List<Integer> xLocations, List<Integer> yLocations, List<UnitType> units) {
        if (xLocations == null) {
            xLocations = new ArrayList<Integer>();
            for (int x = 0; x < MapBounds.BOARD_SIZE; x++) {
                xLocations.add(x);
            }
        }
        if (yLocations == null) {
            yLocations = new ArrayList<Integer>();
            for (int y = 0; y < MapBounds.BOARD_SIZE; y++) {
                yLocations.add(y);
            }
        }

        if (units == null) {
            units = new ArrayList<>();
            for (Config.UnitInformation unit : move.config.unitInformation) {
                if (unit.startHealth.isPresent()) {
                    units.add(move.unitTypeFromShorthand(unit.shorthand.get()));
                }
            }
        }

        int count = 0;
        for (int x : xLocations) {
            for (int y : yLocations) {
                Coords loc = new Coords(x,y);
                if (MapBounds.inArena(loc)) {
                    for (Unit u : move.allUnits[x][y]) {
                        if (units.contains(u.type)) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private void empLineStrategy(GameState move) {
        /*
        First lets fine the cheapest type of firewall stationary unit. We could hardcode this to FILTER probably
        depending on the config but lets demonstrate how to use java-algo features.
         */
        Config.UnitInformation cheapestUnit = null;
        for (Config.UnitInformation uinfo : move.config.unitInformation) {
            if (uinfo.unitCategory.isPresent() && move.isFirewall(uinfo.unitCategory.getAsInt())) {
                float[] costUnit = uinfo.cost();
                if((cheapestUnit == null || costUnit[0] + costUnit[1] <= cheapestUnit.cost()[0] + cheapestUnit.cost()[1])) {
                    cheapestUnit = uinfo;
                }
            }
        }
        if (cheapestUnit == null) {
            GameIO.debug().println("There are no firewalls?");
        }

        for (int x = 27; x>=5; x--) {
            move.attemptSpawn(new Coords(x, 11), move.unitTypeFromShorthand(cheapestUnit.shorthand.get()));
        }

        for (int i = 0; i<22; i++) {
            move.attemptSpawn(new Coords(24, 10), UnitType.EMP);
        }
    }

}