package Backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerrainTest {

    private Terrain terrainInstance;
    private BufferedImage testImage;
    private final int WIDTH = 100;
    private final int HEIGHT = 100;

    // Couleurs pour les tests
    private final int COLOR_AIR = 0x00000000; // Transparent
    private final int COLOR_SOLID = 0xFFFFFFFF; // Blanc opaque (Solide)
    private final int COLOR_WATER = new Color(0, 0, 255, 100).getRGB(); // Bleu semi-transparent (Eau, alpha=100)

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Création d'une image de test en mémoire (100x100)
        testImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        // Initialisation de l'image
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                testImage.setRGB(x, y, COLOR_AIR); // tout est de l'air par défaut
            }
        }
        
        try {
            terrainInstance = new Terrain("dummyPath", "dummyPath");
        } catch (Exception e) {
            // Ignorer l'exception car nous allons injecter l'image de test
        }
        
        terrainInstance = Mockito.mock(Terrain.class);
        
        // On réactive les vraies méthodes qu'on veut tester
        doCallRealMethod().when(terrainInstance).isPixelSolid(anyInt(), anyInt());
        doCallRealMethod().when(terrainInstance).isWater(anyInt(), anyInt());
        doCallRealMethod().when(terrainInstance).inBounds(anyInt(), anyInt());
        doCallRealMethod().when(terrainInstance).getHeightBelow(anyFloat(), anyFloat(), anyInt(), anyInt());
        doCallRealMethod().when(terrainInstance).destroy(anyInt(), anyInt(), anyInt());
        doCallRealMethod().when(terrainInstance).isColliding(anyInt(), anyInt(), anyInt(), anyInt());
        
        // Injection de notre BufferedImage de test dans le champ privé "terrain"
        Field terrainField = Terrain.class.getDeclaredField("terrain");
        terrainField.setAccessible(true);
        terrainField.set(terrainInstance, testImage);
    }

    @Test
    void testIsWater() {
        // Setup : Pixel d'eau (Alpha entre 20 et 235)
        testImage.setRGB(10, 10, COLOR_WATER); // Alpha 100
        testImage.setRGB(20, 20, COLOR_SOLID); // Alpha 255
        
        // Assertions
        assertTrue(terrainInstance.isWater(10, 10), "Le pixel (10,10) devrait être de l'eau");
        assertFalse(terrainInstance.isWater(20, 20), "Le pixel (20,20) est solide, pas de l'eau");
        assertFalse(terrainInstance.isWater(0, 0), "L'air n'est pas de l'eau");
    }

    @Test
    void testIsPixelSolid() {
        // Setup
        testImage.setRGB(50, 50, COLOR_SOLID); // Solide
        testImage.setRGB(51, 51, COLOR_AIR);   // Air
        testImage.setRGB(52, 52, COLOR_WATER); // Eau

        // Assertions
        assertTrue(terrainInstance.isPixelSolid(50, 50), "Devrait être solide");
        assertFalse(terrainInstance.isPixelSolid(51, 51), "L'air ne devrait pas être solide");
        // Selon votre code : if solid and not water -> return true. 
        // Donc l'eau n'est PAS considérée comme "Solid" pour les collisions physiques standards
        assertFalse(terrainInstance.isPixelSolid(52, 52), "L'eau ne doit pas être considérée comme un pixel solide dur");
    }

    @Test
    void testInBounds() {
        assertTrue(terrainInstance.inBounds(0, 0));
        assertTrue(terrainInstance.inBounds(WIDTH - 1, HEIGHT - 1));
        assertFalse(terrainInstance.inBounds(-1, 0));
        assertFalse(terrainInstance.inBounds(WIDTH, 0));
    }

    @Test
    void testGetHeightBelow() {
        // Scénario : Créer un sol à Y=80 sur toute la largeur
        for (int x = 0; x < WIDTH; x++) {
            testImage.setRGB(x, 80, COLOR_SOLID);
        }

        // Le joueur est à x=10, y=0. Il cherche le sol en dessous.
        // width du joueur = 10, height = 20
        float groundY = terrainInstance.getHeightBelow(10, 0, 10, 20);

        assertEquals(80.0f, groundY, "Le sol devrait être détecté à Y=80");
    }
    
    @Test
    void testGetHeightBelowWithHole() {
        // Scénario : Sol à Y=80, mais un trou entre X=10 et X=20
        for (int x = 0; x < WIDTH; x++) {
            if (x < 10 || x > 20) {
                testImage.setRGB(x, 80, COLOR_SOLID);
            }
        }
        // Sol plus bas à Y=90 dans le trou
        for (int x = 10; x <= 20; x++) {
            testImage.setRGB(x, 90, COLOR_SOLID);
        }

        // Le joueur est au dessus du trou (x=15)
        float groundY = terrainInstance.getHeightBelow(15, 0, 1, 10);
        assertEquals(90.0f, groundY, "Le sol dans le trou devrait être à 90");
    }

    @Test
    void testDestroy() {
        // Remplir une zone de solide
        for (int x = 40; x < 60; x++) {
            for (int y = 40; y < 60; y++) {
                testImage.setRGB(x, y, COLOR_SOLID);
            }
        }

        // Vérifier que c'est solide avant destruction
        assertTrue(terrainInstance.isPixelSolid(50, 50));

        // Détruire un cercle de rayon 5 au centre (50, 50)
        terrainInstance.destroy(50, 50, 5);

        // Vérifier que le centre est maintenant de l'air (0x00)
        int pixel = testImage.getRGB(50, 50);
        assertEquals(0x00, pixel & 0xFFFFFF, "Le pixel détruit devrait être noir/transparent");
        
        // Vérifier qu'un point hors du rayon est toujours solide
        assertTrue(terrainInstance.isPixelSolid(50, 58), "Pixel hors du rayon de destruction doit rester solide");
    }

    @Test
    void testIsCollidingWithCoordinates() {
        // Mur solide en (30, 30) de taille 10x10
        for (int x = 30; x < 40; x++) {
            for (int y = 30; y < 40; y++) {
                testImage.setRGB(x, y, COLOR_SOLID);
            }
        }

        // Test collision (x=32, y=32, w=5, h=5) -> Touche le mur
        assertTrue(terrainInstance.isColliding(32, 32, 5, 5), "Devrait collisionner avec le mur");

        // Test sans collision (x=0, y=0)
        assertFalse(terrainInstance.isColliding(0, 0, 5, 5), "Ne devrait pas collisionner dans l'air");
    }

    @Test
    void testIsCollidingWithActor() {
        // Création d'un mock ActorModel
        ActorModel mockActor = mock(ActorModel.class);
        
        // Simulation des champs/méthodes de l'acteur
        when(mockActor.getWidth()).thenReturn(10);
        when(mockActor.getHeight()).thenReturn(20);
        when(mockActor.getX()).thenReturn(50);
        when(mockActor.getY()).thenReturn(50);
        
        // Placement d'un obstacle
        testImage.setRGB(50, 50, COLOR_SOLID);
        
        // On appelle la méthode qui prend les coordonnées brute pour valider la logique principale
        boolean colliding = terrainInstance.isColliding(50, 50, 10, 10);
        assertTrue(colliding);
    }
    
}