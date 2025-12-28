package Frontend;

import Backend.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameTextConsole {

    private GameEngine gameEngine;
    private Scanner sc = new Scanner(System.in);

    private static Pair aimCoords;
    private static final int CONSOLE_WIDTH=60;
    private static final int CONSOLE_HEIGHT=20;
    private static boolean running = false;

    public GameTextConsole(Terrain t) throws IOException {

        ArrayList<Team> teams = new ArrayList<>();
        teams.add(new Team(t,0));
        teams.add(new Team(t,1));
        gameEngine = new GameEngine(t,teams);
        ActorModel.setTerrain(gameEngine.getTerrain());

        System.out.println("Console Game Initialized. Controls: Z/Q/S/D to move, A to quit, T to aim.");

    }

    public void start(){
        running = true;
        String BackUp = "S";
        render();
        while(running && !gameEngine.isGameOver() && gameEngine.getWinningTeam() != null){
            //render();
            if (!gameEngine.getCurrentPlayer().isAlive()){
                running = false;
            }
            System.out.print("Enter command: ");
            String input = sc.nextLine().toUpperCase();
            input = fString(input);
            if (input.isEmpty()){input = BackUp;}
            switch(input){
                case "Z":
                    gameEngine.getCurrentPlayer().jump();
                    break;
                case "Q":
                    gameEngine.getCurrentPlayer().moveLeft(10);
                    break;
                case "S":
                    gameEngine.getCurrentPlayer().stopMoving();
                    break;
                case "D":
                    gameEngine.getCurrentPlayer().moveRight(10);
                    break;
                case "A":
                    System.out.println("Exiting game. Goodbye!");
                    running = false;
                    break;
                case "T":
                    if(!gameEngine.hasShot()) {
                        getAimCoords();
                        System.out.println(aimCoords);
                        lauchProjectile(aimCoords);//copie de GameWindow.lauchProjectile(); calls GameEngine.shoot()
                    }
                    break;
                default:// never reached because of fstring()
                    System.out.println("Invalid command. Use Z/Q/S/D to move, A to quit.");
            }
            BackUp = input;
            gameEngine.getCurrentPlayer().updateDirection(false);
            try{
                Thread.sleep(100); // Control game speed
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            if(running){
                render();
                gameEngine.nextTurn();
            }
        }
        if (gameEngine.getWinningTeam() != null && !BackUp.equals("A")){
            System.out.println("Team " + gameEngine.getWinningTeam().getTeamSymbol() + " won!");
        }
        else if (gameEngine.isGameOver()){
            System.out.println("Game Over. All teams are dead.");
        }
        sc.close();
    }

    private static String fString(String string){
        if (string.length() > 1){
            string = string.substring(0,1).toUpperCase();
        }
        ArrayList<String> F = new ArrayList<>(List.of(new String[]{"Z", "Q", "S", "D", "A","T"}));
        if (string.isEmpty() || !F.contains(string.toUpperCase())){
            return "";
        }
        return string;
    }

    private static String formatInt(String input){
        if(input.equalsIgnoreCase("A")){
            return "A";
        }

        for(int i = 0; i < input.length(); i++){
            if((int)input.charAt(i) < 48 || (int)input.charAt(i) > 57){
                return "IN";
            }
        }
        return input;
    }

    private void getAimCoords(){
        int x;
        int y;
        String input = "IN";
        while(input.equals("IN")){
            System.out.println("Enter X coordinate: ");
            input = formatInt(sc.nextLine());
        }
        if(input.equals("A")){
            return;
        }
        x = Integer.parseInt(input);
        input = "IN";
        while(input.equals("IN")){
            System.out.println("Enter Y coordinate: ");
            input = formatInt(sc.nextLine());
        }
        if(input.equals("A")){
            return;
        }
        y = Integer.parseInt(input);

        aimCoords = new Pair(x,y);
    }

    private void lauchProjectile(Pair aimCoords){
        int x = aimCoords.x;
        int y = aimCoords.y;

        if(gameEngine == null || gameEngine.getCurrentPlayer() == null) return;
        ActorModel currentPlayer = gameEngine.getCurrentPlayer();

        int terrainWidth = gameEngine.getTerrain().getImage().getWidth();
        int terrainHeight = gameEngine.getTerrain().getImage().getHeight();

        float startX = currentPlayer.getX() + currentPlayer.getWidth()/2f;//get the player's center
        float startY = currentPlayer.getY() + currentPlayer.getHeight()/2f;

        float dirX = x - startX;// direction vector from player to coords
        float dirY = y - startY;
        System.out.println("Starting projectile at " + startX + " " + startY);
        gameEngine.shoot(dirX, dirY);
    }

    public void render(){

        gameEngine.stopMoving();
        gameEngine.update();

        char[][] grid = new char[CONSOLE_HEIGHT][CONSOLE_WIDTH];

        // Fill buffer with empty spaces
        for(int x = 0; x < CONSOLE_HEIGHT;x++){
            for(int y = 0; y < CONSOLE_WIDTH;y++){
                grid[x][y]=' ';
            }
        }

        Terrain Map = gameEngine.getTerrain();

        // Draw terrain
        for(int x=0;x<CONSOLE_HEIGHT;x++){// convert console coords to world coords
            for(int y=0;y<CONSOLE_WIDTH;y++){
                float worldY= ((float)x/CONSOLE_HEIGHT) * Map.getImage().getHeight();
                float worldX= ((float)y/CONSOLE_WIDTH) * Map.getImage().getWidth();
                if (Map.isWater((int)worldX,(int)worldY)){
                    grid[x][y]='~'; // Water character
                } else if(Map.isPixelSolid((int)worldX,(int)worldY)){
                    grid[x][y]='#'; // Terrain character
                }
            }
        }

        for(Team team : gameEngine.getTeams()) {
            for (ActorModel worm : team.getTeammates()) {
                // Draw players
                int playerGridX = (int) ((float) worm.getX() / Map.getImage().getWidth() * CONSOLE_WIDTH);// Player coordinates
                int playerGridY = (int) ((float) worm.getY() / Map.getImage().getHeight() * CONSOLE_HEIGHT);

                if (playerGridX >= 0 && playerGridX < CONSOLE_WIDTH
                        && playerGridY >= 0 && playerGridY < CONSOLE_HEIGHT && worm.isAlive()) { // Check if player within bounds & alive

                    grid[playerGridY][playerGridX] = team.getTeamSymbol(); // worm's team symbol
                }
            }
        }

        System.out.println("---WORMS LIKE CONSOLE MODE (BETA)---");
        for (int x = 0; x < CONSOLE_HEIGHT; x++) {
            System.out.println(new String(grid[x]));
        }

        List<Team> teams = gameEngine.getTeams();


        ActorModel playingWorm = gameEngine.getCurrentPlayer();
        Team t = teams.get(gameEngine.getCurrentTurnIndex() % teams.size());

        System.out.println("-------------------------------------");
        System.out.printf("Player Stats: HP=%d/%d | Postion: X=%d Y=%d | Alive : %b | Team : %s\n",
                playingWorm.getHP(), playingWorm.getMaxHP(), playingWorm.getX(), playingWorm.getY(),playingWorm.isAlive(), t.getTeamSymbol());
        System.out.printf("Team status : HP = %d/%d | Number of players = %d | Number of players alive : %d | Alive : %b\n",
                t.getHP(), t.getMaxHP(), t.nbPlayers(), t.nbPlayersAlive(), t.isAlive());
        System.out.printf("Velocity: (vx=%.1f, vy=%.1f) | Speed:%d | Grounded : %b\n",
                playingWorm.getVx(),playingWorm.getVy(), playingWorm.getSpeed(), playingWorm.isOnGround(Map));
        for (Team team : teams) {
            System.out.printf(team.status() + "\n");
        }


    }

}
