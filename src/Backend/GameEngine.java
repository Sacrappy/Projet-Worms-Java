package Backend;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private final List<Team> teams;
    private final Terrain terrain;
    private final LinkedList<Pair> turnOrder;// ordre de passage des joueurs
    private int currentTurnIndex;
    private ActorModel currentPlayer;
    private Projectile activeProjectile;
    private long turnStartTime;
    private static final long TURN_DURATION_MS = 30000;
    private boolean hasShot = false;

    // Game settings
    private static final float PROJECTILE_SPEED = 15.0f;
    private static final float EXPLOSION_RADIUS = 30.0f;
    private static final int EXPLOSION_DAMAGE = 50;

    public GameEngine(Terrain terrain, List<Team> teams) {
        this.terrain = terrain;
        this.teams = new ArrayList<>();
        this.teams.addAll(teams);
        this.turnOrder = initializeTurnOrder();
        this.currentTurnIndex = 0;
        startTurn();
    }

    private void startTurn(){
        this.currentPlayer = getPlayerFromPair(turnOrder.get(currentTurnIndex));
        this.turnStartTime = System.currentTimeMillis();
        this.hasShot = false;
        this.activeProjectile = null;
    }

    private LinkedList<Pair> initializeTurnOrder() { // Create the turn order with all players and randomize
        LinkedList<Pair> order = new LinkedList<>();
        for (int teamIndex = 0; teamIndex < teams.size(); teamIndex++) {
            Team team = teams.get(teamIndex);
            for (int playerIndex = 0; playerIndex < team.nbPlayers(); playerIndex++) {
                order.add(new Pair(playerIndex,teamIndex));
            }
        }
        // Shuffle for randomness
        java.util.Collections.shuffle(order, new Random());
        return order;
    }

    public ActorModel getPlayerFromPair(Pair pair) { // gets player from Team/Pair
        if (pair.x >= 0 && pair.x < teams.size()) {
            Team team = teams.get(pair.x);
            ActorModel[] teammates = team.getTeammates();
            if (pair.y >= 0 && pair.y < teammates.length) {
                return teammates[pair.y];
            }
        }
        return null;
    }

    public ActorModel getCurrentPlayer() {
        return currentPlayer;
    }

    public Projectile getActiveProjectile() {
        return activeProjectile;

    }

    public List<Team> getTeams() {
        return teams;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public int getCurrentTurnIndex() {return currentTurnIndex;}

    public boolean hasShot() {return hasShot;}

    public LinkedList<Pair> getTurnOrder() {
        return turnOrder;
    }

    public void update() {
        if (currentPlayer != null && !currentPlayer.isAlive()) {
            endActionPhase();
            nextTurn();
            return;
        }
        // updates physics
        // first update player gravity
        long currentTime = System.currentTimeMillis();
        if(currentTime - turnStartTime > TURN_DURATION_MS){ // check if time is up
            if(activeProjectile == null){
                endActionPhase();
                nextTurn();
            }
        }
        for(Team team : teams){
            team.update();
        }

        // then update projectile if any
        if(activeProjectile != null){
            activeProjectile.update();
            checkProjectileCollision();

        }

        // Update time if player has shot.
        if (hasShot && currentTime-turnStartTime < 25000){
        turnStartTime -= 25000-(currentTime-turnStartTime);
        }
    }

    public void nextTurn() {
        // ends current turn and moves to next player alive in turn order
        if(currentPlayer != null){
            currentPlayer.stopMoving(); // stop moving at end of turn also stops the moonwalking bug
        }
        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
        Pair nextPlayerPair = turnOrder.get(currentTurnIndex);
        ActorModel nextPlayer = getPlayerFromPair(nextPlayerPair);
        if(nextPlayer == null || !nextPlayer.isAlive()){
            if(isGameOver()){
                currentPlayer = null; // Game over
                return;
            }
            nextTurn(); // skip to next if dead
            return;
        }
        startTurn();
    }

    public void checkProjectileCollision(){  // checks for projectile collision with terrain or players

        if(activeProjectile != null){
            //Get all alive players from all teams
            List<ActorModel> alivePlayers = new ArrayList<>();
            for(Team team : teams){
                for(ActorModel player : team.getTeammates()){
                    if(player.isAlive()){
                        alivePlayers.add(player);
                    }
                }
            }

//check for collision and handle explosion
            activeProjectile.checkCollisionAndExplode(terrain, alivePlayers);
            if(!activeProjectile.isActive()){
                activeProjectile = null; // reset projectile after explosion
                endActionPhase();
            }
        }
    }

    public void endActionPhase(){
        // ends the actions phase of the current player and updates HP
        //updating players HP
        for(Team team : teams){
            team.updateHP();
        }
        if (isGameOver()) {
            Team winningTeam = getWinningTeam();
            if(winningTeam != null) // if a team is alive
                System.out.println("Game Over! Winning team: " + winningTeam.getTeamSymbol());
            else { // if everyone is dead
                System.out.println("Game Over! No winning team.");}
            return;
        }
        //nextTurn(); disabled to allow retreat time
    }

    public void shoot(float dirX, float dirY) {
        if (!currentPlayer.isAlive()) return;
        if (hasShot) return; // can only shoot once per turn
        // shoots a projectile in the given direction
        // System.out.println("Player shooting with direction (" + dirX + ", " + dirY + ")");
        if (activeProjectile != null) {
            //System.out.println("Cannot shoot: projectile already active.");
            return; // cannot shoot if a projectile is already active
        }
        //System.out.println("Creating projectile...");

        float startX = currentPlayer.getX() + currentPlayer.height/ 2f;
        float startY = currentPlayer.getY() - currentPlayer.height/2f; // start from center height
        this.activeProjectile = new Projectile(startX, startY, PROJECTILE_SPEED, dirX, dirY, currentPlayer, EXPLOSION_RADIUS, EXPLOSION_DAMAGE);
        hasShot = true;
    }

    public boolean isGameOver() { // checks if only one team has alive players
        int teamsWithAlivePlayers = 0;
        for (Team team : teams) {
            if (team.nbPlayersAlive()>0) {
                teamsWithAlivePlayers++;
            }
        }
        return teamsWithAlivePlayers <= 1;
    }

    public Team getWinningTeam() { // returns the winning team if game over
        for (Team team : teams) {
            if (team.nbPlayersAlive() > 0) {
                return team;
            }
        }
        return null; // No winning team
    }

    public int getTimeRemaining(){
        long elapsed = System.currentTimeMillis() - turnStartTime;
        return (int)Math.max(0, TURN_DURATION_MS - elapsed);
    }

    public void stopMoving(){
        for(Team team : teams){
            for(ActorModel player : team.getTeammates()){
                player.stopMovingConsole();
            }
        }
    }
}


