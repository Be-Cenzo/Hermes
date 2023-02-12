package be.cenzo.hermes.ui;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    private static String profileFileName = "profile.dat";

    private String nome;
    private String favLang;

    public Profile(String nome, String favLang) {
        this.nome = nome;
        this.favLang = favLang;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFavLang() {
        return favLang;
    }

    public void setFavLang(String favLang) {
        this.favLang = favLang;
    }

    public boolean isValid(){
        return !nome.isEmpty();
    }

    public static Profile deserialize(File filesDir){
        File profileFile = new File(filesDir, profileFileName);
        try {
            FileInputStream fi = new FileInputStream(profileFile);
            ObjectInputStream oi = new ObjectInputStream(fi);
            Profile profile = (Profile)oi.readObject();
            oi.close();
            fi.close();
            return profile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void serialize(File filesDir){
        File profileFile = new File(filesDir, profileFileName);
        try {
            FileOutputStream fo = new FileOutputStream(profileFile);
            ObjectOutputStream ou = new ObjectOutputStream(fo);
            ou.writeObject(this);
            ou.close();
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
