package Backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PairTest {

    // 1. Test du constructeur et de l'accès aux champs
    @Test
    void testConstructorAndFieldAccess() {
        Pair pair = new Pair(5, 10);

        assertEquals(5, pair.x, "La valeur de x devrait être 5");
        assertEquals(10, pair.y, "La valeur de y devrait être 10");
    }

    // 2. Test d'égalité : Deux paires identiques
    @Test
    void testEqualsReturnsTrueForSameValues() {
        Pair p1 = new Pair(1, 2);
        Pair p2 = new Pair(1, 2);

        assertTrue(p1.equals(p2), "Deux paires avec les mêmes valeurs devraient être égales");
    }

    // 3. Test d'inégalité : x différent
    @Test
    void testEqualsReturnsFalseForDifferentX() {
        Pair p1 = new Pair(1, 2);
        Pair p2 = new Pair(99, 2);

        assertFalse(p1.equals(p2), "Devrait être faux car x est différent");
    }

    // 4. Test d'inégalité : y différent
    @Test
    void testEqualsReturnsFalseForDifferentY() {
        Pair p1 = new Pair(1, 2);
        Pair p2 = new Pair(1, 99);

        assertFalse(p1.equals(p2), "Devrait être faux car y est différent");
    }

    // 5. Test d'inégalité : tout différent
    @Test
    void testEqualsReturnsFalseForDifferentXAndY() {
        Pair p1 = new Pair(1, 2);
        Pair p2 = new Pair(3, 4);

        assertFalse(p1.equals(p2), "Devrait être faux car x et y sont différents");
    }

    // 6. Test limite : NullPointerException (Comportement actuel de votre code)
    // Note : Votre code actuel plantera si on passe null. Ce test valide ce comportement.
    @Test
    void testEqualsThrowsExceptionForNull() {
        Pair p1 = new Pair(1, 2);
        
        // On s'attend à une NullPointerException car p.x est accédé sans vérifier si p est null
        assertThrows(NullPointerException.class, () -> {
            p1.equals(null);
        });
    }
}
