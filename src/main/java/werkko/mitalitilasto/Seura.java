/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package werkko.mitalitilasto;

public class Seura implements Comparable<Seura>{

    String nimi;
    int yks;
    int kaks;
    int kolme;
    

    public Seura(String nimi) {
        this.nimi = nimi;
        yks = 0;
        kaks = 0;
        kolme = 0;
      
    }
    
    public void lisaaYkkossijoja(int numero){
        yks+=numero;
    }
    
      public void lisaaKakkossijoja(int numero){
        kaks+=numero;
    }
      
        public void lisaaKolmossijoja(int numero){
        kolme+=numero;
    }
        
     public int yhteensaMitaleja(){
         return yks+kaks+kolme;
     }

    public int compareTo(Seura o) {
        int oKullat = o.getYks();
        int oHopeat = o.getKaks();
        int oPronssit = o.getKolme();
        
        if(oKullat == this.yks && oHopeat == this.kaks && oPronssit == this.kolme){
            return 0;
        }
        
        if(yks>oKullat){
            return -1;
        }
        if(yks == oKullat  && kaks>oHopeat){
            return -1;
        }
        if(yks == oKullat && kaks == oHopeat && kolme>oPronssit){
            return -1;
        }else{
            return 1;
        }
    }

    public int getYks() {
        return yks;
    }

    public void setYks(int yks) {
        this.yks = yks;
    }

    public int getKaks() {
        return kaks;
    }

    public void setKaks(int kaks) {
        this.kaks = kaks;
    }

    public int getKolme() {
        return kolme;
    }

    public void setKolme(int kolme) {
        this.kolme = kolme;
    }
    
    @Override
    public String toString(){
        return nimi+"\n Kultamitalit: "+yks+" Hopeamitalit: "+kaks+" Pronssimitalit: "+kolme+" Yhteensä: "+yhteensaMitaleja();
    }
    
}
