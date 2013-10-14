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

    ArrayList<String> maatunnukset = new ArrayList<String>() {
        {
            add("FIN");
            add("Russia");
            add("Russia ");
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

    public Statfinder() {
       
        List<WebElement> linkit = etsiLinkitTuloksiin();
        List<String> linkitStringina = vainVuosiMitalilinkit(2012, linkit);
        linkitStringina = poistaToimimattomat(linkitStringina);
        
        keraaTilastot(linkitStringina);
        tulostaTilastot();


    }

    private List<WebElement> etsiLinkitTuloksiin() {

        driver.get("http://www.judoshiai.fi/results/previous_fi.php");
        List<WebElement> elements = driver.findElements(By.cssSelector("a"));
        return elements;
    }

    private List<String> vainVuosiMitalilinkit(int i, List<WebElement> linkit) {
        List<String> vuodenLinkit = new ArrayList<String>();

        for (WebElement webElement : linkit) {
            if (webElement.toString().contains("2012")) {
                vuodenLinkit.add(webElement.getAttribute("href").replaceAll("index.html", "medals.html"));
            }
        }
        return vuodenLinkit;
    }

    private List<String> poistaToimimattomat(List<String> linkitStringina) {
        ArrayList<String> poistettavat = new ArrayList<String>();
        ArrayList<String> lisattavat = new ArrayList<String>();
        int kierros = 1;
        for (String string : linkitStringina) {
            System.out.println("GET (" + kierros + "/" + linkitStringina.size() + "): " + string);
            driver.get(string);
            if (driver.getPageSource().contains("Not Found") || string.endsWith(".pdf")) {
                poistettavat.add(string);
            }
            if(string.contains("junnucup")){
                poistettavat.add(string);
            }
            kierros++;
        }
        linkitStringina.removeAll(poistettavat);
        for (String string : poistettavat) {
            System.out.println("Virheellinen: "+string);
        }
        linkitStringina.addAll(lisattavat);
        
        
        return linkitStringina;
    }

    private void keraaTilastot(List<String> linkitStringina) {
        int kierros = 1;
        for (String string : linkitStringina) {
            if (!string.endsWith("/medals.html")) {
                string += "/medals.html";
            }
            driver.get(string);
            System.out.println("Analyzing (" + kierros + "/" + linkitStringina.size() + ": " + string);
            WebElement medalTable = driver.findElement(By.className("medals"));

            List<WebElement> medalRows = medalTable.findElements(By.cssSelector("td"));
            List<String> medalRowsStringeina = Stringeiksi(medalRows);
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
            kierros++;
        }


    }

    private List<String> Stringeiksi(List<WebElement> medalRows) {
        ArrayList<String> lista = new ArrayList<String>();
        for (WebElement webElement : medalRows) {
            lista.add(webElement.getText());
        }
        return lista;
    }

    private void tulostaTilastot() {
        ArrayList<Seura> seuralista = seuratListaan();
        Collections.sort(seuralista);
        int laskuri = 1;
        for (Seura seura : seuralista) {
            System.out.println(laskuri + ". " + seura.toString());
            laskuri++;
        }
    }

    private ArrayList<Seura> seuratListaan() {
        ArrayList<Seura> seuralista = new ArrayList<Seura>();
        Set<String> keys = seurat.keySet();
        for (String string : keys) {
            seuralista.add(seurat.get(string));
        }
        return seuralista;
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
}
