package CodePage;
/**
 * Created by frankobe on 3/11/2015.
 */
public class GALCodePage extends CodePage {
    /**
     * Constructor for GALCodePage.  Initializes all of the code page values.
     */
    public GALCodePage() {
        /* Maps String to Token for the code page */
        codepageTokens.put("DisplayName", 0x05);
        codepageTokens.put("Phone", 0x06);
        codepageTokens.put("Office", 0x07);
        codepageTokens.put("Title", 0x08);
        codepageTokens.put("Company", 0x09);
        codepageTokens.put("Alias", 0x0a);
        codepageTokens.put("FirstName", 0x0b);
        codepageTokens.put("LastName", 0x0c);
        codepageTokens.put("HomePhone", 0x0d);
        codepageTokens.put("MobilePhone", 0x0e);
        codepageTokens.put("EmailAddress", 0x0f);

        /* Maps token to string for the code page */
        for (String s : codepageTokens.keySet()) {
            codepageStrings.put(codepageTokens.get(s), s);
        }

        codePageIndex = 0x10;
        codePageName = "GAL";
    }
}

