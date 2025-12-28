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

    // Selected inventory slot
    private int selectedSlot = 0;

    //Power Gauge
    private float currentPower = 0.0f;
    private boolean increasingPower = true;
    private boolean powerGaugeActive = false;
    private static final float POWER_GAUGE_SPEED = 0.5f; // Speed of power gauge 
    private static final float POWER_GAUGE_MIN = 0.0f;
    private static final float POWER_GAUGE_MAX = 100.0f;

    // Game settings
    private static final float PROJECTILE_SPEED = 15.0f;
    private static final float EXPLOSION_RADIUS = 30.0f;
    private static final int EXPLOSION_DAMAGE = 50;
    private boolean isTeamInventory;

    public GameEngine(Terrain terrain, List<Team> teams) {
        this.terrain = terrain;
        this.teams = new ArrayList<>();
        this.teams.addAll(teams);
        this.turnOrder = initializeTurnOrder();
        this.currentTurnIndex = 0;
        startTurn();
    }
        public int getSelectedSlot() {
            return selectedSlot;
        }
        public void setSelectedSlot(int selectedSlot) {
            if(selectedSlot>=0&& selectedSlot<8)
            this.selectedSlot = selectedSlot;
        }

        public ObjectModel getSelectedItem() {
    List<ObjectModel> inventory = isTeamInventory() ? 
        getCurrentTeam().getTeamInventory() : getCurrentPlayer().getInventory();
    
    if (selectedSlot >= 0 && selectedSlot < inventory.size()) {
        return inventory.get(selectedSlot);
    }
    return null;
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
    public void setTeamInventoryMode(boolean isTeamInventory) {
        this.isTeamInventory = isTeamInventory;
    }
    public void removeFromInventory(ObjectModel item) {
        if (isTeamInventory) {
            getCurrentTeam().removeFromInventory(item);
            return;
        }
        currentPlayer.removeFromInventory(item);
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
        if (powerGaugeActive && !hasShot) {
            // Update power gauge
            if (increasingPower) {
                currentPower += POWER_GAUGE_SPEED;
                if (currentPower >= POWER_GAUGE_MAX) {
                    currentPower = POWER_GAUGE_MAX;
                    increasingPower = false;
                }
            } else {
                currentPower -= POWER_GAUGE_SPEED;
                if (currentPower <= POWER_GAUGE_MIN) {
                    currentPower = POWER_GAUGE_MIN;
                    increasingPower = true;
                }
            }
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
    public void startCharging() {
    if (!hasShot && activeProjectile == null) {
        powerGaugeActive = true;
        currentPower = POWER_GAUGE_MIN;
        increasingPower = true;
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
    public boolean isTeamInventory() {
        return isTeamInventory;
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
        if (!currentPlayer.isAlive()||hasShot||activeProjectile!=null) return;
        // can only shoot once per turn
        // shoots a projectile in the given direction
        // System.out.println("Player shooting with direction (" + dirX + ", " + dirY + ")");
        //System.out.println("Cannot shoot: projectile already active.");
        // cannot shoot if a projectile is already active
        //System.out.println("Creating projectile...");
        
        powerGaugeActive = false; // stop power gauge

        float startX = currentPlayer.getX() + currentPlayer.height/ 2f;
        float startY = currentPlayer.getY() - currentPlayer.height/2f; // start from center height
    // adjust speed based on current power
    float effectiveSpeed = PROJECTILE_SPEED * (currentPower / 100.0f);
        //effectiveSpeed = Math.max(5.0f, effectiveSpeed); // if we need a minimum speed
        
        this.activeProjectile = new Projectile(startX, startY, effectiveSpeed, dirX, dirY, currentPlayer, EXPLOSION_RADIUS, EXPLOSION_DAMAGE);
        hasShot = true;
        currentPower= 0; // reset power after shot
    }
    
    public float getCurrentPower() {
    return currentPower;
}
public boolean isCharging() {
    return powerGaugeActive;
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
    public Team getCurrentTeam(){
        // get the player pair corresponding to current turn index
    Pair currentPair = turnOrder.get(currentTurnIndex);
    // second element is the team index
    int teamIndex = currentPair.y;
        return teams.get(teamIndex);
    
}
public void useSelectedWeapon(float dirX, float dirY) {
    int slot = getSelectedSlot();
    List<ObjectModel> inv = isTeamInventory() ? getCurrentTeam().getTeamInventory() : getCurrentPlayer().getInventory();
    
    if (slot < inv.size()) {
        ObjectModel item = inv.get(slot);
        if (item instanceof ProjectileWeapon) {
            // Weapon creates and fires the projectile
            ((ProjectileWeapon) item).fire(currentPlayer, dirX, dirY, currentPower, this);
        } else if (item instanceof Weapon) {
            item.use(currentPlayer, this); // If it's a non-projectile weapon, just use it
        }
        // if its a consumable
        else if (item instanceof Consumable) {
            item.use(currentPlayer, this);
            removeFromInventory(item);
        }
    }
}
}



