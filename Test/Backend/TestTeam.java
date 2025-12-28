package Backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamTest {

    private ActorModel player1;
    private ActorModel player2;
    private ActorModel player3;
    private ActorModel[] teammates;
    private Team team;

    @BeforeEach
    void setUp() {
        // 1. On "Mock" (simule) les acteurs pour contrôler leur comportement
        player1 = mock(ActorModel.class);
        player2 = mock(ActorModel.class);
        player3 = mock(ActorModel.class);

        // 2. On définit le comportement par défaut de nos acteurs simulés
        // Disons que chaque joueur a 100 HP max et 100 HP actuels
        when(player1.getHP()).thenReturn(100);
        when(player1.getMaxHP()).thenReturn(100);
        when(player1.isAlive()).thenReturn(true);

        when(player2.getHP()).thenReturn(100);
        when(player2.getMaxHP()).thenReturn(100);
        when(player2.isAlive()).thenReturn(true);

        when(player3.getHP()).thenReturn(100);
        when(player3.getMaxHP()).thenReturn(100);
        when(player3.isAlive()).thenReturn(true);

        teammates = new ActorModel[]{player1, player2, player3};
        
        // On utilise le constructeur qui permet d'injecter nos acteurs simulés
        team = new Team(teammates, 'A');
    }

    // --- Tests d'initialisation ---

    @Test
    void testConstructorCalculatesTotalHP() {
        // Le constructeur doit sommer les HP et MaxHP des acteurs
        // Ici: 100 + 100 + 100 = 300
        assertEquals(300, team.getHP(), "Les HP de l'équipe doivent être la somme des HP des joueurs");
        assertEquals(300, team.getMaxHP(), "Les MaxHP de l'équipe doivent être la somme des MaxHP des joueurs");
    }

    @Test
    void testTeamSymbolIsUpperCase() {
        Team lowerCaseTeam = new Team(teammates, 'z');
        assertEquals('Z', lowerCaseTeam.getTeamSymbol(), "Le symbole doit être converti en majuscule");
    }

    @Test
    void testPlayersOrderIsCorrectlyDefined() {
        ArrayList<Pair> order = team.playersOrder;
        
        assertNotNull(order);
        assertEquals(3, order.size());
        // Vérifie que la paire contient bien l'index du joueur (le 2ème élément de Pair)
        assertEquals(0, order.get(0).y); 
        assertEquals(1, order.get(1).y);
        assertEquals(2, order.get(2).y);
    }

    // --- Tests de Gestion des Tours (Next) ---

    @Test
    void testNextRotatesToNextPlayer() {
        // Active ID initial est 0
        assertEquals(0, team.getActivePlayerID());

        team.next();
        assertEquals(1, team.getActivePlayerID(), "Devrait passer au joueur 1");

        team.next();
        assertEquals(2, team.getActivePlayerID(), "Devrait passer au joueur 2");

        team.next();
        assertEquals(0, team.getActivePlayerID(), "Devrait boucler au joueur 0");
    }

    @Test
    void testNextSkipsDeadPlayer() {
        // Scénario : Le joueur 1 meurt
        when(teammates[1].isAlive()).thenReturn(false);

        // État initial : Joueur 0 actif
        assertEquals(0, team.getActivePlayerID());

        team.next();

        // Le joueur 1 est mort, on s'attend à ce que next() saute directement au 2
        assertEquals(2, team.getActivePlayerID(), "Devrait sauter le joueur mort (index 1) pour aller au 2");
    }

    // --- Tests de Dégâts et Mise à jour ---

    @Test
    void testSufferDamageUpdatesTeamHP() {
        // Simulation : le joueur 1 prend 20 dégâts
        // Attention : Comme player1 est un Mock, sufferDamage ne change pas vraiment son état interne
        // sauf si on le programme ou si on vérifie l'appel.
        // Pour ce test, on va simuler le changement de valeur de retour de getHP()
        
        // 1. On appelle la méthode
        team.sufferDamage(20, 0);

        // 2. Vérifie que la méthode sufferDamage a bien été appelée sur l'acteur
        verify(player1).sufferDamage(20);

        // 3. Pour tester l'updateHP du team, on doit dire au mock que ses HP ont baissé
        when(player1.getHP()).thenReturn(80); 
        
        // On force l'update (car sufferDamage appelle updateHP)
        team.updateHP();

        // Total attendu : 80 (p1) + 100 (p2) + 100 (p3) = 280
        assertEquals(280, team.getHP());
    }

    @Test
    void testTeamDiesWhenAllPlayersAreDead() { // FAILED
        // On tue tout le monde
        when(player1.isAlive()).thenReturn(false);
        when(player2.isAlive()).thenReturn(false);
        when(player3.isAlive()).thenReturn(false);
        
        // update() vérifie le statut "deadOnes"
        team.update(); 

        assertFalse(team.isAlive(), "L'équipe devrait être morte si tous les joueurs sont morts");
    }

}