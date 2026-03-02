package com.narxoz.rpg.battle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class BattleEngine {

    private static BattleEngine instance;
    private Random random = new Random(1L);

    private BattleEngine() {
    }

    public static BattleEngine getInstance() {
        if (instance == null) {
            instance = new BattleEngine();
        }
        return instance;
    }

    public BattleEngine setRandomSeed(long seed) {
        this.random = new Random(seed);
        return this;
    }

    public void reset() {
        this.random = new Random(1L);
    }

    public EncounterResult runEncounter(List<Combatant> teamA, List<Combatant> teamB) {
        Objects.requireNonNull(teamA, "teamA must not be null");
        Objects.requireNonNull(teamB, "teamB must not be null");

        List<Combatant> a = new ArrayList<>(teamA);
        List<Combatant> b = new ArrayList<>(teamB);

        EncounterResult result = new EncounterResult();

        result.addLog("Battle started!");
        result.addLog("Team A size: " + a.size());
        result.addLog("Team B size: " + b.size());

        int rounds = 0;

        while (hasLiving(a) && hasLiving(b)) {
            rounds++;
            result.addLog("\n=== Round " + rounds + " ===");

            attackPhase(a, b, "Team A", "Team B", result);
            removeDead(b, result, "Team B");

            if (!hasLiving(b)) break;

            attackPhase(b, a, "Team B", "Team A", result);
            removeDead(a, result, "Team A");
        }

        result.setRounds(rounds);

        if (hasLiving(a)) result.setWinner("Team A");
        else if (hasLiving(b)) result.setWinner("Team B");
        else result.setWinner("Draw");

        result.addLog("\nBattle finished. Winner: " + result.getWinner());

        return result;
    }

    private void attackPhase(List<Combatant> attackers,
                             List<Combatant> defenders,
                             String atkName,
                             String defName,
                             EncounterResult result) {

        for (Combatant attacker : attackers) {
            if (!attacker.isAlive()) continue;
            if (!hasLiving(defenders)) return;

            Combatant target = firstLiving(defenders);
            int damage = attacker.getAttackPower();

            boolean crit = random.nextInt(100) < 10;
            if (crit) damage *= 2;

            target.takeDamage(damage);

            String log = atkName + " → " + attacker.getName()
                    + " hits " + target.getName()
                    + " for " + damage;

            if (crit) log += " (CRITICAL!)";

            result.addLog(log);
        }
    }

    private void removeDead(List<Combatant> team,
                            EncounterResult result,
                            String teamName) {

        Iterator<Combatant> iterator = team.iterator();
        while (iterator.hasNext()) {
            Combatant c = iterator.next();
            if (!c.isAlive()) {
                result.addLog(teamName + ": " + c.getName() + " has fallen.");
                iterator.remove();
            }
        }
    }

    private boolean hasLiving(List<Combatant> team) {
        for (Combatant c : team) {
            if (c.isAlive()) return true;
        }
        return false;
    }

    private Combatant firstLiving(List<Combatant> team) {
        for (Combatant c : team) {
            if (c.isAlive()) return c;
        }
        return null;
    }
}