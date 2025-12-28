package Backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectileTest {

    private Projectile projectile;

    @Mock
    private ActorModel shooter;

    @Mock
    private ActorModel victim;

    @Mock
    private Terrain terrain;

    private final float START_X = 100.0f;
    private final float START_Y = 100.0f;
    private final float SPEED = 10.0f;
    private final float EXPLOSION_RADIUS = 50.0f;
    private final int DAMAGE = 20;

    @BeforeEach
    void setUp() {
        projectile = new Projectile(START_X, START_Y, SPEED, 1.0f, 0.0f, shooter, EXPLOSION_RADIUS, DAMAGE);
    }

    @Test
    void testInitialization() {
        assertEquals(START_X, projectile.getX());
        assertEquals(START_Y, projectile.getY());
        assertTrue(projectile.isActive(), "Le projectile doit être actif à la création");
        assertEquals(shooter, projectile.getShooter());
        assertEquals(8, projectile.getSize());
    }

    @Test
    void testUpdatePhysics() {
        // Initial state: vx = 10.0, vy = 0.0
        projectile.update();

        // Après 1 update :
        // x devrait augmenter de vx (100 + 10 = 110)
        assertEquals(110.0f, projectile.getX(), 0.01f);
        
        // y devrait augmenter de vy initial (100 + 0 = 100) 
        // MAIS vy augmente de GRAVITY (0.8) après l'ajout à y dans votre code actuel :
        // Code: x+=vx; y+=vy; vy+=GRAVITY;
        // Donc au premier tour, y ne change pas si vy était 0, mais vy devient 0.8
        assertEquals(100.0f, projectile.getY(), 0.01f);
        assertEquals(0.8f, projectile.getVy(), 0.01f);

        // Update suivant pour vérifier la gravité
        projectile.update();
        assertEquals(100.0f + 0.8f, projectile.getY(), 0.01f); // y a pris l'ancienne valeur de vy
    }

    @Test
    void testDeactivationOutOfBounds() {
        // Cas 1: Trop à gauche
        Projectile pLeft = new Projectile(-101, 0, 0, 0, 0, shooter, 0, 0);
        pLeft.update();
        assertFalse(pLeft.isActive(), "Doit se désactiver si x < -100");

        // Cas 2: Trop à droite
        Projectile pRight = new Projectile(2001, 0, 0, 0, 0, shooter, 0, 0);
        pRight.update();
        assertFalse(pRight.isActive(), "Doit se désactiver si x > 2000");

        // Cas 3: Trop bas
        Projectile pDown = new Projectile(0, 2001, 0, 0, 0, shooter, 0, 0);
        pDown.update();
        assertFalse(pDown.isActive(), "Doit se désactiver si y > 2000");
    }

    @Test
    void testCollisionWithTerrain() {
        projectile = new Projectile(START_X, START_Y, SPEED, 1.0f, 0.0f, shooter, EXPLOSION_RADIUS, DAMAGE);
        List<ActorModel> players = new ArrayList<>();
        
        // Simulation : le terrain détecte une collision
        when(terrain.isColliding(projectile)).thenReturn(true);

        projectile.checkCollisionAndExplode(terrain, players);

        // Vérifications
        assertFalse(projectile.isActive(), "Le projectile doit se désactiver après impact");
        // Vérifie que le terrain est détruit aux coordonnées du centre du projectile
        verify(terrain, times(1)).destroy(anyInt(), anyInt(), eq((int)EXPLOSION_RADIUS));
    }

    @Test
    void testCollisionWithEnemy() {
        projectile = new Projectile(START_X, START_Y, SPEED, 1.0f, 0.0f, shooter, EXPLOSION_RADIUS, DAMAGE);
        List<ActorModel> players = new ArrayList<>();
        players.add(shooter);
        players.add(victim);

        // Configuration du Mock de la victime pour qu'elle soit sur le chemin du projectile
        // Le projectile est à (100, 100) avec une taille de 8.
        when(victim.getX()).thenReturn(100);
        when(victim.getY()).thenReturn(100);
        when(victim.getWidth()).thenReturn(1);
        when(victim.getHeight()).thenReturn(1);
        
        // On s'assure que le terrain ne bloque pas avant
        when(terrain.isColliding(projectile)).thenReturn(false);

        // Configuration pour les dégâts
        when(shooter.getMagicDamage()).thenReturn(DAMAGE);

        projectile.checkCollisionAndExplode(terrain, players);

        // Le projectile doit exploser
        assertFalse(projectile.isActive());
    }

    @Test
    void testExplosionDamageFalloff() {
        // Test du calcul des dégâts (plus on est loin, moins on a mal)
        List<ActorModel> players = new ArrayList<>();
        players.add(victim);

        // On force l'explosion
        when(terrain.isColliding(projectile)).thenReturn(true);
        when(shooter.getMagicDamage()).thenReturn(100);

        // Place la victime très proche (presque dégâts max)
        when(victim.getX()).thenReturn(100); 
        when(victim.getY()).thenReturn(100); 

        projectile.checkCollisionAndExplode(terrain, players);

        // On capture l'argument de dégâts
        verify(victim).sufferDamage(intThat(damage -> damage > 80));// Doit recevoir des dégâts élevés
    }
    
    @Test
    void testExplosionNoSelfDamage() {
        // Le tireur ne doit pas subir de dégâts de sa propre explosion
        List<ActorModel> players = new ArrayList<>();
        players.add(shooter);

        when(terrain.isColliding(projectile)).thenReturn(true); // Force explosion
        when(shooter.getX()).thenReturn(100); // Tireur au centre de l'explosion
        when(shooter.getY()).thenReturn(100);

        projectile.checkCollisionAndExplode(terrain, players);

        // Verify que sufferDamage n'est JAMAIS appelé sur le shooter
        verify(shooter, never()).sufferDamage(anyInt());
    }
}
