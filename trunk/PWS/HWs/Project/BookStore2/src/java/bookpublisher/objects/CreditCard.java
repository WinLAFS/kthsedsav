/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bookpublisher.objects;

/**
 *
 * @author Shum
 */
public class CreditCard {
    private String cardNumber;
    private int expirationYear;
    private int expirationMonth;
    private String holderName;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(int expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public int getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(int expirationYear) {
        this.expirationYear = expirationYear;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

}
