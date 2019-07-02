import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;

public class YandexMarketTest {
    private static final Logger logger = LogManager.getLogger(YandexMarketTest.class);
    private static final String START_PAGE = "https://ya.ru/";
    private static WebDriver driver;
    private static final long TIMEOUT = 20;
    private static WebDriverWait wait;
    private static final String smartphoneBrandOne = "ASUS";
    private static final String smartphoneBrandTwo = "Honor";

    @BeforeClass
    public static void generalSetup(){
        driver = WebDriverFactory.createDriver(WebDriverType.valueOf("CHROME"));
        //driver.manage().timeouts().implicitlyWait(TIMEOUT, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, TIMEOUT);
        driver.manage().window().maximize();

        driver.get(START_PAGE);
        driver.get("https://market.yandex.ru/");

        confirmLocation();
    }

    @AfterClass
    public static void teardown(){
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void smartphoneComparisonTableTest(){
        openSmartphoneSection();
        selectTwoSmartphoneManufacturers();
        sortSmartphonesByPriceAsc();
        selectSmartphonesToCompare();
        openComparisonTable();

        assertTrue(osSpecIsPresentInSection("все характеристики"));
        assertFalse(osSpecIsPresentInSection("различающиеся характеристики"));
    }

    public static void confirmLocation(){
        WebElement locationForm = driver.findElement(By.cssSelector("div.n-region-notification_layout_init"));
        WebElement acceptLocationBtn = locationForm.findElement(By.xpath(".//span[contains(@class, 'notification__ok')]"));
        wait.until(ExpectedConditions.elementToBeClickable(acceptLocationBtn));
        acceptLocationBtn.click();
    }

    public static void openSmartphoneSection(){
        WebElement horizontalBar = driver.findElement(By.cssSelector("div.n-w-tabs__horizontal-tabs"));
        horizontalBar.findElement(By.xpath(".//span[text()='Электроника']")).click();
        WebElement smartphoneSection = driver.findElement(By.xpath("//a[text()='Мобильные телефоны']"));
        wait.until(ExpectedConditions.elementToBeClickable(smartphoneSection));
        smartphoneSection.click();
    }

    public static void sortSmartphonesByPriceAsc(){
        WebElement listOfPhones = driver.findElement(By.xpath("//div[contains(@class, 'n-snippet-list')]"));
        WebElement filtersPanel = driver.findElement(By.cssSelector("div.n-filter-block_pos_left.i-bem"));
        filtersPanel.findElement(By.xpath(".//a[text()='по цене']")).click();
        wait.until(ExpectedConditions.stalenessOf(listOfPhones));
    }

    public static void selectTwoSmartphoneManufacturers(){
        WebElement listOfPhones = driver.findElement(By.xpath("//div[contains(@class, 'n-snippet-list')]"));
        WebElement manufacturersPanel = driver.findElement(By.cssSelector("fieldset[data-autotest-id='7893318']"));
        manufacturersPanel.findElement(By.xpath(String.format(".//span[text()='%s']", smartphoneBrandOne))).click();
        wait.until(ExpectedConditions.stalenessOf(listOfPhones));
        listOfPhones = driver.findElement(By.xpath("//div[contains(@class, 'n-snippet-list')]"));
        manufacturersPanel.findElement(By.xpath(String.format(".//span[text()='%s']", smartphoneBrandTwo))).click();
        wait.until(ExpectedConditions.stalenessOf(listOfPhones));
    }

    public static void selectSmartphonesToCompare(){
        boolean isSmartphoneOfBrandOneFound = false;
        boolean isSmartphoneOfBrandTwoFound = false;

        List<WebElement> listOfSmartphones = driver.findElements(By.xpath("//div[contains(@class, 'n-snippet-list')]/div[contains(@class, 'n-snippet-cell')]"));

        for (WebElement e: listOfSmartphones) {
            String elementTitle = e.findElement(By.xpath(".//div[contains(@class, '__title')]/a")).getAttribute("innerText");
            if(elementTitle.contains(smartphoneBrandOne) && !isSmartphoneOfBrandOneFound){
                logger.info(String.format("%s contains %s", elementTitle, smartphoneBrandOne));
                isSmartphoneOfBrandOneFound = true;
                Actions action = new Actions(driver);
                action.moveToElement(e).build().perform();
                WebElement addToComparisonIcon = e.findElement(By.xpath(".//div[contains(@class, 'n-user-lists_type_compare_in-list_no')]"));
                wait.until(ExpectedConditions.elementToBeClickable(addToComparisonIcon));
                addToComparisonIcon.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.popup-informer__title")));
                String popupTitle = driver.findElement(By.cssSelector("div.popup-informer__title")).getAttribute("textContent");
                assertEquals( String.format("Товар %s добавлен к сравнению", elementTitle), popupTitle);
            }
            else if (elementTitle.contains(smartphoneBrandTwo) && !isSmartphoneOfBrandTwoFound){
                logger.info(String.format("%s contains %s", elementTitle, smartphoneBrandTwo));
                isSmartphoneOfBrandTwoFound = true;
                Actions action = new Actions(driver);
                action.moveToElement(e).build().perform();
                WebElement addToComparisonIcon = e.findElement(By.xpath(".//div[contains(@class, 'n-user-lists_type_compare_in-list_no')]"));
                wait.until(ExpectedConditions.elementToBeClickable(addToComparisonIcon));
                addToComparisonIcon.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.popup-informer__title")));
                String popupTitle = driver.findElement(By.cssSelector("div.popup-informer__title")).getAttribute("textContent");
                assertEquals( String.format("Товар %s добавлен к сравнению", elementTitle), popupTitle);
            }
            else if(isSmartphoneOfBrandOneFound && isSmartphoneOfBrandTwoFound){
                break;
            }
        }
    }

    public static void openComparisonTable(){
        driver.findElement(By.xpath("//div[contains(@class,'header2-menu')]//span[text()='Сравнение']")).click();
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("div.n-compare-content__line div.n-compare-cell"), 0));
        List <WebElement> smartphonesInComparisonTable = driver.findElements(By.cssSelector("div.n-compare-content__line div.n-compare-cell"));
        assertEquals(2, smartphonesInComparisonTable.size());
    }

    public static boolean osSpecIsPresentInSection(String section){
        driver.findElement(By.xpath(String.format("//div[contains(@class, 'n-compare-toolbar')]//span[text()='%s']", section))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Общие характеристики']")));
        boolean isOSSpecPresent = false;
        List<WebElement> allComparisonCharacteristics = driver.findElements(By.cssSelector("div.n-compare-table div.n-compare-row-name"));
        for (WebElement item: allComparisonCharacteristics) {
            if(item.getAttribute("innerText").toLowerCase().equals("операционная система")){
                isOSSpecPresent = true;
                break;
            }
        }
        logger.info(String.format("In %s OS specification is present: %b", section, isOSSpecPresent));
        return isOSSpecPresent;
    }
}