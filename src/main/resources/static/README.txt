DAHIRA MAFATIH OUL BICHRI — FRONTEND SÉPARÉ

Structure :
- index.html : entrée du site, redirige vers pages/index.html
- pages/ : une page HTML par fonctionnalité
- css/global.css : style général partagé
- css/sidebar.css : menu partagé
- css/<fonctionnalite>.css : styles propres à chaque page
- js/app.js : comportements communs d’interface
- js/<fonctionnalite>.js : emplacement de la logique propre à la page

IMPORTANT : le fichier app.js original de votre application n’était pas présent dans les fichiers fournis.
Les fichiers HTML et les sections sont séparés à partir du HTML reçu, mais les opérations qui dépendaient
de l’ancien JavaScript (API, ajout/modification/suppression, authentification, etc.) ne peuvent pas être
garanties tant que le vrai app.js et les éventuels endpoints backend ne sont pas raccordés.
Ne remplacez pas votre projet en production avant d’avoir testé et réintégré ce JavaScript.

Pour tester : ouvrir index.html avec votre serveur local, pas directement depuis l’explorateur de fichiers.
