# Résolution des problèmes de build avec OneDrive

## Problème : AccessDeniedException lors du build

Si vous rencontrez des erreurs `AccessDeniedException` lors de la compilation, c'est probablement dû à OneDrive qui verrouille des fichiers dans le dossier `build`.

## Solutions

### Solution 1 : Exclure le dossier build de OneDrive (Recommandé)

1. Ouvrez les paramètres OneDrive
2. Allez dans "Sauvegarde" > "Gérer la sauvegarde"
3. Cliquez sur "Choisir les dossiers" pour le dossier contenant votre projet
4. Décochez le dossier `build` ou excluez-le

### Solution 2 : Nettoyer le build

Dans Android Studio :
- Menu : `Build` > `Clean Project`
- Puis : `Build` > `Rebuild Project`

Ou en ligne de commande :
```powershell
cd "C:\Users\Taverny\OneDrive - ISEP\Documents\GitHub\HabitForge"
.\gradlew clean
.\gradlew build
```

### Solution 3 : Déplacer le projet hors de OneDrive (Meilleure solution)

Pour éviter les problèmes de synchronisation, déplacez votre projet en dehors du dossier OneDrive :
- Par exemple : `C:\Users\Taverny\Documents\GitHub\HabitForge`
- Ou : `C:\Dev\HabitForge`

### Solution 4 : Désactiver temporairement la synchronisation

Pausez OneDrive pendant le build, puis reprenez la synchronisation après.

## Note

Le dossier `build` est généré automatiquement et ne doit pas être synchronisé avec OneDrive. Il est déjà exclu dans `.gitignore`.

