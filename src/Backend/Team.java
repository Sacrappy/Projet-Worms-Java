package Backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Team {

    public static int teamID = 0;
    private ActorModel[] teammates;
    private int maxHP;
    private int HP;
    private boolean isAlive = true;
    private final char teamSymbol;
    private int activePlayerID;
    public final ArrayList<Pair> playersOrder;

    public Team() throws IOException { // DO NOT USE UNLESS YOU UPDATE THIS CONSTRUCTOR
        teammates = new ActorModel[]{new ActorModel(),new ActorModel()};
        HP = 200; // sum of HP of all team members
        maxHP = 200;
        Random rand = new Random();
        teamSymbol = (char) rand.nextInt(65, 90);
        activePlayerID = 0;
        playersOrder = defineOrder(teamID++);
    }
    public Team(ActorModel[] teammates, char teamSymbol) { // USE THIS CONSTRUCTOR IN TESTS
        this.teammates = teammates;
        this.playersOrder = defineOrder(teamID++);
        for (ActorModel actor : teammates) {
            HP += actor.getHP();
            maxHP += actor.getMaxHP();
        }
        this.teamSymbol = Character.toUpperCase(teamSymbol);
        activePlayerID = 0;
    }

    public Team(Terrain terrain, int teamIndex) throws IOException {// USE THIS CONSTRUCTOR
        teammates = new ActorModel[]{new ActorModel(),new ActorModel()};
        Random randX= new Random();
        int mapWidth = terrain.getImage().getWidth();
        int mapHeight = terrain.getImage().getHeight();
        int margin = 50; // margin so that players don't spawn at the very edge of the terrain
        int zoneWidth = (mapWidth / 2)-(2*margin); //spawning zone width for each team
        int offsetX = (teamIndex == 0) ? margin : (mapWidth / 2) + margin; // offset for team 1 and team 2 : if team index is 0, spawn on left side, else on right side
        
        for (ActorModel player : teammates) { // Place players at random X positions on the terrain
            boolean  validSpawn = false;
            int attempts = 0; // limit attempts in case of empty map
            while (!validSpawn && attempts < 100) {
                attempts++;
                int x = randX.nextInt(zoneWidth) + offsetX;
                float y = terrain.getHeightBelow(x,  0, player.getWidth(), player.getHeight());
                //check if void
                boolean isVoid = y>= mapHeight;
                //check if water
                boolean isWater = terrain.isWater(x, (int) y);
                if(!isVoid && !isWater){
                    player.setX(x);
                    player.setY(y-player.getHeight()-5); //-5 to be sure the player is above the ground and falls on the terrain
                    validSpawn = true;
                }
                
            }
            if(!validSpawn){
            player.setX(offsetX); // if no valid spawn found, place at offsetX the player is at least on the right side
            player.setY(0);
        }
        
        }
        HP = 200; // sum of HP of all team members
        maxHP = 200;
        Random rand = new Random();
        teamSymbol = (char) rand.nextInt(65, 90);
        activePlayerID = 0;
        playersOrder = defineOrder(teamID++);
    }

    public ActorModel[] getTeammates() {
        return teammates;
    }
    public boolean isAlive() {
        return isAlive;
    }
    public int getActivePlayerID() {
        return activePlayerID;
    }
    public char getTeamSymbol() {return teamSymbol;}
    public ActorModel getActivePlayer() {
        return teammates[activePlayerID];
    }
    public int getHP() {
        return HP;
    }
    public int getMaxHP() {return maxHP;}
    public int nbPlayers(){return teammates.length;}
    public int nbPlayersAlive(){
        int nb = 0;
        for (ActorModel actor : teammates) {
            if (actor.isAlive()) {
                nb++;
            }
        }
        return nb;
    }

    public void updateHP(){
        HP = 0;
        for (ActorModel player : teammates) {
            HP += player.getHP();
        }
        if (HP == 0){isAlive = false;}
    }

    public void sufferDamage(int damage,int id) {
        teammates[id].sufferDamage(damage);
        updateHP();
        if (!isAlive) {System.out.println("Team "+ teamSymbol +" is dead");}
    }

    public String status(){
        if (!isAlive) return "Team "+ teamSymbol +" is dead ; HP = " + HP;
        return "Team "+ teamSymbol +" is alive ; HP = " + HP;
    }

    public void update(){
        int deadOnes = 0;
        for (ActorModel player : teammates) {
            player.updateDirection(false);
            if (!player.isAlive()) {
                deadOnes++;
            }
        }
        if (deadOnes == teammates.length){
            isAlive = false;
            HP = 0;
        }
        updateHP();
    }

    public void next(){
        if(isAlive){
            activePlayerID = (activePlayerID + 1) % teammates.length;
            if (!teammates[activePlayerID].isAlive()) {
                next();
            }
        }
    }

    public ArrayList<Pair> defineOrder(int teamID){
        ArrayList<Pair> order = new ArrayList<>();
        for(int i = 0; i < teammates.length; i++){
            order.add(new Pair(teamID,i));
        }
        return order;
    }
}
