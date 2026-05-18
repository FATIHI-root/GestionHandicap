package ma.ac.uir.gestionhandicap.model;

import ma.ac.uir.gestionhandicap.model.enums.Role;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;

import java.time.LocalDateTime;

public abstract class Utilisateur {

    protected int idUtilisateur;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String motDePasse;
    protected String telephone;
    protected Role role;
    protected StatutCompte statutCompte;
    protected LocalDateTime dateInscription;
    protected String motifRefus;

    public Utilisateur() {
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String telephone, Role role, StatutCompte statutCompte) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.role = role;
        this.statutCompte = statutCompte;
        this.dateInscription = LocalDateTime.now();
    }

    public Utilisateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse, String telephone, Role role, 
                        StatutCompte statutCompte,LocalDateTime dateInscription) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.role = role;
        this.statutCompte = statutCompte;
        this.dateInscription = dateInscription;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public StatutCompte getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(StatutCompte statutCompte) {
        this.statutCompte = statutCompte;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "Utilisateur:" + "\nid=" + idUtilisateur + "\n nom='" + nom + '\'' +"\n prenom='" + prenom + '\'' +"\n email='" + email + '\'' +"\n role=" + role +
                "\n statutCompte=" + statutCompte ;
    }
}
