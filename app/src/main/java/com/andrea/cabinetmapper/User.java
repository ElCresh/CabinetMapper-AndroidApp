package com.andrea.cabinetmapper;

public class User {
    private int id;
    private String username;
    private String nome;
    private String cognome;
    private String email;
    private int banned;

    public User(int id, String username, String nome, String cognome, int banned){
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.banned = banned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public int getBanned() {
        return banned;
    }

    public void setBanned(int banned) {
        this.banned = banned;
    }

    public boolean isBanned(){
        if(this.banned == 1){
            return true;
        }else{
            return false;
        }
    }
}
