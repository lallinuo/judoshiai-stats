package werkko.mitalitilasto;

import java.util.List;
import java.util.Scanner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
        boolean pdf = false;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Minkä vuoden mitalitilastot haluat? (0 = kaikkien aikojen): ");
        int vuosi = Integer.parseInt(scanner.nextLine());
//        System.out.print("Haluatko tilaston .pdf muodossa? (k=kyllä, muuten ei): ");
//        if (scanner.nextLine().equals("k")) {
//            pdf = true;
//        }
        System.out.println("Aloitetaan analysointi: ");
        
        //truen kohdalle myöhemmin halutaanko tulokset pdfmuodossa vai ei
        new Statfinder(vuosi, true);

    }
}
