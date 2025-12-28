package Frontend;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Backend.ActorModel;
import Backend.GameEngine;
import Backend.Team;
import Backend.Terrain;

public class GameWindow extends JFrame implements KeyListener,Runnable{

    private Menu menuPanel;
    private TerrainPanel terrainPanel;
    private GameModeSelectionPanel gameModeSelectionPanel;
    private MapSelectionPanel mapSelectionPanel;
    private GameEngine gameEngine;
    private OptionPanel optionPanel;
    private int mouseX = 0;
    private int mouseY = 0;
    private String selectedMapPath ="/Images/Maps/TerrainTest.png";
    private String selectedBackgroundPath="/Images/Backgrounds/BackGround_Test.png";
    private static long start;
    public static long elapsed;

    public GameWindow() {
        setTitle("Jeu Worms-like");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        mapSelectionPanel = new MapSelectionPanel(this);
        terrainPanel = new TerrainPanel(); // create terrain panel whith default terrain test
        gameModeSelectionPanel = new GameModeSelectionPanel(this);
        menuPanel = new Menu(this); // Pass the current GameWindow instance
        optionPanel = new OptionPanel(this);
        add(menuPanel);

        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseX = e.getX(); // get mouse click position
                mouseY = e.getY();
                if(gameEngine != null && gameEngine.getActiveProjectile() == null){
                    launchProjectile();
                }
            }
        });
        // add KeyListener to the GameWindow
        addKeyListener(this);

        // activate key event reception
        setFocusable(true);
        requestFocus();

        setVisible(true); // Leave this last so that everything is set up before displaying
    }

    public void setSelectedMapPath(String path){
        this.selectedMapPath = path;
    }

    public void setSelectedBackgroundPath(String path){
        this.selectedBackgroundPath = path;
    }

    private void launchProjectile(){
        if(terrainPanel == null || gameEngine == null) return;
        ActorModel currentPlayer = gameEngine.getCurrentPlayer();

        if(currentPlayer == null) return;

        int terrainWidth = terrainPanel.getGround().getImage().getWidth(); // native terrain dimensions
        int terrainHeight = terrainPanel.getGround().getImage().getHeight();

        int panelWidth = terrainPanel.getWidth(); // current panel dimensions
        int panelHeight = terrainPanel.getHeight();

        float scaleX = (float) panelWidth / terrainWidth; // convert screen coord to world coord
        float scaleY = (float) panelHeight / terrainHeight;

        float worldMouseX = mouseX / scaleX; //Convert mouse screen coord to world coord
        float worldMouseY = mouseY / scaleY;

        float startX = currentPlayer.getX() + currentPlayer.getWidth()/ 2f;
        float startY = currentPlayer.getY() + currentPlayer.getHeight()/2f; //get the center of the player

        float dirX = worldMouseX - startX; // direction vector from player to mouse click
        float dirY = worldMouseY - startY;
        gameEngine.shoot(dirX, dirY); // shoot the projectile
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(gameEngine == null) return;
        // when a key is pressed
        char code = e.getKeyChar(); // Caractère Unicode (ex: 'a')
        ActorModel currentPlayer = gameEngine.getCurrentPlayer();
        if(currentPlayer == null) return;
        switch (code) {
            case 'z': // jump
                currentPlayer.jump();
                break;
            case 'q': // move left
                currentPlayer.moveLeft(1);
                break;
            case 'd': // move right
                currentPlayer.moveRight(1);
                break;
            case 'a' : //exit game
                System.exit(0);
                break;
            case KeyEvent.VK_SPACE: // end turn
                if(gameEngine.getActiveProjectile() == null){
                    gameEngine.nextTurn();
                    break;

                }
        }

    }
    @Override
    public void keyTyped(KeyEvent e) {} // not used but required by KeyListener interface
    @Override
    public void keyReleased(KeyEvent e) {
        if(gameEngine == null) return;
        char code = e.getKeyChar();
        ActorModel currentPlayer = gameEngine.getCurrentPlayer();
        if(currentPlayer == null) return;
        if(code == 'q' || code == 'd'){ // stop moving when releasing q or d
            currentPlayer.stopMoving();
        }
    } // not used but required by KeyListener interface


    public void showMenuScreen(){
        getContentPane().removeAll(); // Clear current screen
        add(menuPanel); // Add the menu panel
        revalidate(); // Refresh the frame
        repaint(); // Repaint the frame
    }

    public void showMapSelectionScreen(){
        getContentPane().removeAll(); // Clear current screen
        add(mapSelectionPanel); // Add the map selection panel
        revalidate(); // Refresh the frame
        repaint(); // Repaint the frame
    }
    public void showOptionsScreen(){
        getContentPane().removeAll();
        add(optionPanel);        
        revalidate();               
        repaint();
    }
    public void replayGame(){ // restart the game, just calls showGameScreen since it already intialize new parameters
        showGameScreen();
    }
    public void showGameScreen(){
        try {
            Backend.Terrain ground = new Backend.Terrain(selectedBackgroundPath, selectedMapPath);
            Backend.ActorModel.setTerrain(ground);
            if(terrainPanel== null){
                terrainPanel = new TerrainPanel();
            }
            terrainPanel.setTerrain(ground); // set the selected terrain
            Team team1 = new Team(ground,0); // intialize two default teams might get changed to custom teams choosed from panels
            Team team2 = new Team(ground,1);

            gameEngine = new GameEngine(ground, java.util.List.of(team1, team2));

            terrainPanel.setGameEngine(gameEngine);// IMPORTANT: Links the game engine to the panel
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        getContentPane().removeAll(); // CLear the menu
        add(terrainPanel); // Add the terrain panel
        revalidate(); // refresh the frame
        repaint();  // repaint the frame
        requestFocus(); // Makes the Focus on the GamePanel to receive key events
        new Thread(this).start(); // Start the game loop in a new thread
    }

    public void showGameModeSelection(){
        getContentPane().removeAll();
        add(gameModeSelectionPanel);
        revalidate();
        repaint();
    }
    public void showGameOverScreen(String winnerName){
        gameEngine =null ; // no need to keep rendering the game
        GameOverPanel gameOverPanel = new GameOverPanel(this, winnerName);
        getContentPane().removeAll();
        add(gameOverPanel);
        revalidate();
        repaint();

    }

    public void startGameTextMode() throws IOException {

        System.out.println("Launching Console mode. . .");
        this.setVisible(false);
        this.dispose();

        try{
            Terrain t = new Terrain(selectedBackgroundPath,selectedMapPath);
            GameTextConsole gameConsole = new GameTextConsole(t);
            gameConsole.start();
        }
        catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting console game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->{
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }

    @Override
    public void run() {
        // Main game loop
        start = System.currentTimeMillis();
        while (true) {
            long startTime = System.currentTimeMillis(); //to measure frame time
            if(gameEngine != null){
                gameEngine.update(); // update game engine physics
                int timeRemaining = gameEngine.getTimeRemaining();
                setTitle("Worms-like |Time "+ timeRemaining/1000+"s");

                if (gameEngine.isGameOver()) {
                    Backend.Team winner = gameEngine.getWinningTeam();
                    String winnerName = (winner != null) 
                        ? "Team " + winner.getTeamSymbol() // ? = if true then and : = if false then
                        : "Match Nul !";
                    
                    // Switch to GameOverPanel
                    // SwingUtilities.invokeLater is better for when running on a thread
                    String finalWinnerName = winnerName;
                    SwingUtilities.invokeLater(() -> showGameOverScreen(finalWinnerName));
                elapsed = startTime - start;
                setTitle("Worms-like");
            }
            repaint(); // refresh the display

            // time management to maintain a consistent frame rate
            long endTime = System.currentTimeMillis();
            long frameTime = endTime - startTime;
            long sleepTime = 16 - frameTime; // 16 ms for 60 FPS
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime); // sleep to maintain frame rate
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        }
    }
}
}