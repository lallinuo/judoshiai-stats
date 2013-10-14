/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package werkko.mitalitilasto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 *
 * @author Lalli
 */
class Statfinder {
    //Joskus tilastoissa näkyy seuranpaikalla maatunnus, sillon ei lasketa pisteitä seuralle

    ArrayList<String> maatunnukset = new ArrayList<String>() {
        {
            add("FIN");
            add("Russia");
            add("Russia ");
            add("Finland");
            add("USA");
            add("UKR");
            add("UZB");
            add("RUS");
            add("GER");
            add("SWE");
            add("IS");
            add("NOR");
            add("ROM");
            add("FRO");
            add("DEN");
        }
    };
    WebDriver driver = new HtmlUnitDriver();
    HashMap<String, Seura> seurat = new HashMap<String, Seura>();
    HashMap<String, Boolean> blacklist = new HashMap<String, Boolean>();

    public Statfinder(int vuosi, boolean pdf) {
        createBlacklist();
        List<WebElement> linkit = etsiLinkitTuloksiin();
        List<String> linkitStringina = haeTietynVuodenLinkit(vuosi, linkit);
        keraaTilastot(linkitStringina);
        tulostaTilastot();
    }

    private void createBlacklist() {
        blacklist.put("http://www.judoshiai.fi/junior_fjo_2011_res/medals.html", true);
        blacklist.put("http://www.stockholmsjudo.se/jswop/results13/", true);
        blacklist.put("http://www.orimattilanjudo.net/sm_Kisasivut/smjudoetusivu.htm", true);
        blacklist.put("http://www.judoshiai.fi/SM_2011", true);
        blacklist.put("http://www.erkkijokikokko.com/balticsea_20110126/contest_2010/tulokset.html", true);
        blacklist.put("http://www.judoshiai.fi/kymijoki_2010_tulokset/medals.html", true);
        blacklist.put("http://www.judoshiai.fi/Orimattila_joukkue_2010_results/index.html", true);
        blacklist.put("http://www.judoshiai.fi/SM_2010/sovellettu.html", true);
        blacklist.put("http://www.judoshiai.fi/JC_IV_2010_tulokset/medals.html", true);
        blacklist.put("http://www.judoshiai.fi/JC_I_2010_tulokset", true);
        blacklist.put("http://www.judoshiai.fi/NSM_2009_AB/medals.html", true);
        blacklist.put("http://www.erkkijokikokko.com/JC_I_2009_VK_tulokset/", true);
        blacklist.put("http://www.erkkijokikokko.com/jc_I_2009_OV_tulokset/", true);
        blacklist.put("http://www.judoshiai.fi/kymijoki_U13_U15_res/medals.html", true);
        blacklist.put("http://www.judoshiai.fi/results/2013/voru_kevad_2013/vorukevad.rei.ee/tulemused_2013/", true);

    }
    //Käy läpi judoshiain tuloslistan ja tallettaa linkit elements listaan

    private List<WebElement> etsiLinkitTuloksiin() {

        driver.get("http://www.judoshiai.fi/results/previous_fi.php");
        List<WebElement> elements = driver.findElements(By.cssSelector("a"));
        return elements;
    }

    //valitsee listasta vain halutun vuoden tulokset ja palauttaa listan linkeistä
    private List<String> haeTietynVuodenLinkit(int vuosi, List<WebElement> linkit) {
        List<String> vuodenLinkit = new ArrayList<String>();

        for (WebElement webElement : linkit) {
            if (webElement.toString().contains(vuosi + "") || vuosi == 0) {
                vuodenLinkit.add(webElement.getAttribute("href").replaceAll("index.html", "medals.html"));
            }
        }
        return vuodenLinkit;
    }

    //osa linkeistä ei toimi joten niitä ei tule käydä läpi, lisäksi junnucup kisoja ei haluta laskea mukaan
    //käydään jokainen linkki läpi ja luodaan seuraoliot niiden perusteella
    private void keraaTilastot(List<String> linkitStringina) {
        for (String string : linkitStringina) {
            if (!string.endsWith("/medals.html")) {
                string += "/medals.html";
            }
            driver.get(string);
            if (linkkiToimii()) {
                System.out.println("Analysoidaan: " + string);
                WebElement medalTable = driver.findElement(By.className("medals"));  //etsitään medals taulukko ja 
                List<WebElement> medalRows = medalTable.findElements(By.cssSelector("td")); // laitetaan sen rivit listaan
                List<String> medalRowsStringeina = Stringeiksi(medalRows);      // jonka jälkeen muutetaan rivit stringeiksi
                kasitteleTaulukonRivit(medalRowsStringeina);
            }
        }
    }

    private void kasitteleTaulukonRivit(List<String> medalRowsStringeina) {
        for (int i = 0; i < medalRowsStringeina.size() - 1; i += 6) {
            String seuranNimi = medalRowsStringeina.get(i + 2);
            seuranNimi = tarkastaNimi(seuranNimi);
            if (seuranNimi != null) {
                int kultaMitalit = Integer.parseInt(medalRowsStringeina.get(i + 3));
                int hopeaMitalit = Integer.parseInt(medalRowsStringeina.get(i + 4));
                int pronssiMitalit = Integer.parseInt(medalRowsStringeina.get(i + 5));
                if (!seurat.containsKey(seuranNimi)) {
                    seurat.put(seuranNimi, new Seura(seuranNimi));
                }
                Seura seura = seurat.get(seuranNimi);
                seura.lisaaYkkossijoja(kultaMitalit);
                seura.lisaaKakkossijoja(hopeaMitalit);
                seura.lisaaKolmossijoja(pronssiMitalit);
                seurat.put(seuranNimi, seura);
            }
        }
    }

    private String tarkastaNimi(String seuranNimi) {

        if (seuranNimi.contains(",")) {
            seuranNimi = seuranNimi.substring(0, seuranNimi.indexOf(","));
        }
        if (seuranNimi.contains("/")) {
            if (seuranNimi.indexOf("/") == 3) {
                seuranNimi = seuranNimi.substring(4, seuranNimi.length());
            } else {
                seuranNimi = seuranNimi.substring(0, seuranNimi.indexOf("/"));
            }
        }
        if (maatunnukset.contains(seuranNimi)) {
            seuranNimi = null;
        }
        return seuranNimi;
    }

    private boolean linkkiToimii() {
        if (driver.getCurrentUrl().contains("junnucup")
                || driver.getPageSource().contains("Not Found")
                || driver.getCurrentUrl().contains(".pdf")
                || blacklisted(driver.getCurrentUrl())) {
            return false;
        }
        return true;
    }

    private List<String> Stringeiksi(List<WebElement> medalRows) {
        ArrayList<String> lista = new ArrayList<String>();
        for (WebElement webElement : medalRows) {
            lista.add(webElement.getText());
        }
        return lista;
    }

    private ArrayList<Seura> seuratListaan() {
        ArrayList<Seura> seuralista = new ArrayList<Seura>();
        Set<String> keys = seurat.keySet();
        for (String string : keys) {
            seuralista.add(seurat.get(string));
        }
        return seuralista;
    }

    private boolean medalsPageDoesntExist() {

        return false;
    }

    private boolean blacklisted(String string) {
        if (blacklist.get(string) == null) {

            return false;
        }
        return true;
    }
    //sivuja jotka ei enää toimi syystä tai toisesta

    private void tulostaTilastot() {
        ArrayList<Seura> seuralista = seuratListaan();
        Collections.sort(seuralista);
        int laskuri = 1;
        for (Seura seura : seuralista) {
            System.out.println(laskuri + ". " + seura.toString());
            laskuri++;
        }
    }
    //käydään jokainen taulukon rivi läpi ja luodaan/muokataan niiden perusteella seuraolioita
}
