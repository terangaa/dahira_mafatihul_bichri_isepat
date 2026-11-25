
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Redirige la racine vers la page d'accueil publique.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/home.html";
    }

    /**
     * Affiche le tableau de bord.
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/index.html";
    }
}
