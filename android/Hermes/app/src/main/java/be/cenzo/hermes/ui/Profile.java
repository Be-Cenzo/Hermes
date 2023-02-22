package be.cenzo.hermes.ui;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    private static String profileFileName = "profile.dat";

    private String nome;
    private String favLang;
    private int radiusValue;
    private String userId;
    private String token;
    private Date createdToken;

    public Profile(String nome, String favLang, int radiusValue) {
        this.nome = nome;
        this.favLang = favLang;
        this.radiusValue = radiusValue;
    }

    public Profile(String nome, String favLang, String userId, String token) {
        this.nome = nome;
        this.favLang = favLang;
        this.userId = userId;
        this.token = token;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreatedToken() {
        return createdToken;
    }

    public void setCreatedToken(Date createdToken) {
        this.createdToken = createdToken;
    }

    public int getRadiusValue() {
        return radiusValue;
    }

    public void setRadiusValue(int radiusValue) {
        this.radiusValue = radiusValue;
    }

    public boolean isValid(){
        return !nome.isEmpty() && !userId.isEmpty();
    }

    public boolean tokenIsValid(){
        if(token == null || token.isEmpty())
            return false;
        if(createdToken == null)
            return false;
        Date expirationDate = new Date(createdToken.getTime() + 86400000);
        if(expirationDate.before(new Date()))
            return false;
        return true;
    }

    public static Profile deserialize(File filesDir) throws Exception{
        File profileFile = new File(filesDir, profileFileName);
        FileInputStream fi = new FileInputStream(profileFile);
        ObjectInputStream oi = new ObjectInputStream(fi);
        Profile profile = (Profile)oi.readObject();
        oi.close();
        fi.close();
        return profile;
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
