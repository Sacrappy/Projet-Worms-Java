package Backend;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public interface GestionSauvegarde {

    String NOM_FICHIER_OBLIGATOIRE = "sauvegarde.txt";

    // --- SAUVEGARDE (Version automatique/forcée) ---
    default void sauvegarder(String chemin) {
        if (!chemin.equals(NOM_FICHIER_OBLIGATOIRE)) {
            System.err.println("ERREUR : Nom de fichier invalide. Il faut : " + NOM_FICHIER_OBLIGATOIRE);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File fichier = new File(chemin);

        try {
            if (!fichier.exists()) {
                System.out.println("Création du fichier " + chemin + "...");
                fichier.createNewFile();
            }
            mapper.writeValue(fichier, this);
            System.out.println("Sauvegarde terminée !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- CHARGEMENT (Version manuelle avec chemin fixe) ---
    default <T> T charger(String chemin, Class<T> classeCible) {
        if (!chemin.equals(NOM_FICHIER_OBLIGATOIRE)) {
            System.err.println("ERREUR : Seul le fichier " + NOM_FICHIER_OBLIGATOIRE + " est autorisé.");
            return null;
        }

        File fichier = new File(chemin);
        if (!fichier.exists()) {
            System.err.println("Erreur : Le fichier n'existe pas.");
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(fichier, classeCible);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- NOUVELLE MÉTHODE : CHARGEMENT VIA EXPLORATEUR ---
    // Cette méthode ouvre la fenêtre, récupère le chemin, et appelle l'autre méthode charger()
    default <T> T charger(Class<T> classeCible) {
        
        // 1. On force le "Look and Feel" de Windows pour que la fenêtre soit jolie
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si ça échoue, on garde le look Java par défaut, ce n'est pas grave
        }

        // 2. Création de l'explorateur de fichiers
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Charger une sauvegarde");
        
        // On se place dans le dossier du projet par défaut (facultatif mais pratique)
        fileChooser.setCurrentDirectory(new File("."));

        // 3. Configuration du filtre "Document texte (*.txt)" comme demandé
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Document texte (*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        
        // On empêche de choisir "tous les fichiers" pour forcer le filtre txt (optionnel)
        fileChooser.setAcceptAllFileFilterUsed(false);

        // 4. Ouverture de la fenêtre
        // null signifie qu'elle s'ouvre au milieu de l'écran
        int userSelection = fileChooser.showOpenDialog(null);

        // 5. Si l'utilisateur clique sur "Ouvrir"
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            String cheminSelectionne = fileToLoad.getName(); // On récupère juste le nom (ex: sauvegarde.txt)
            
            System.out.println("Fichier sélectionné : " + fileToLoad.getAbsolutePath());

            // 6. On appelle ta méthode de chargement existante
            // Attention : Ta méthode charger(String) vérifie strictement le nom "sauvegarde.txt".
            // Si l'utilisateur choisit un autre fichier txt, ça affichera ton message d'erreur.
            return charger(cheminSelectionne, classeCible);
        }

        System.out.println("Chargement annulé.");
        return null;
    }
}