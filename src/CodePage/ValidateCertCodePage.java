package CodePage;
/**
 * Created by frankobe on 3/11/2015.
 */
public class ValidateCertCodePage extends CodePage {
    /**
     * Constructor for ValidateCertCodePage.  Initializes all of the code page values.
     */
    public ValidateCertCodePage() {
        /* Maps String to Token for the code page */
        codepageTokens.put("ValidateCert", 0x05);
        codepageTokens.put("Certificates", 0x06);
        codepageTokens.put("Certificate", 0x07);
        codepageTokens.put("CertificateChain", 0x08);
        codepageTokens.put("CheckCRL", 0x09);
        codepageTokens.put("Status", 0x0a);

        /* Maps token to string for the code page */
        for (String s : codepageTokens.keySet()) {
            codepageStrings.put(codepageTokens.get(s), s);
        }

        codePageIndex = 0x0b;
        codePageName = "ValidateCert";
    }
}
