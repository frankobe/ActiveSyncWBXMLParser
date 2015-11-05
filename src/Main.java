import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Main {

    public static void main(String[] args) {
        File xml = new File("/Users/frankobe/Desktop/sample.xml");
        File wbxml = new File("/Users/frankobe/Desktop/sampleOuput.wbxml");
        File output = new File("/Users/frankobe/Desktop/output.xml");

        CodePage[] mCodePages = new CodePage[22];
        mCodePages[0] = new AirSyncCodePage();
        mCodePages[1] = new ContactsCodePage();
        mCodePages[2] = new EmailCodePage();
        mCodePages[3] = new AirNotifyCodePage();
        mCodePages[4] = new CalendarCodePage();
        mCodePages[5] = new MoveCodePage();
        mCodePages[6] = new ItemEstimateCodePage();
        mCodePages[7] = new FolderHierarchyCodePage();
        mCodePages[8] = new MeetingResponseCodePage();
        mCodePages[9] = new TasksCodePage();
        mCodePages[0xa] = new ResolveRecipientsCodePage();
        mCodePages[0xb] = new ValidateCertCodePage();
        mCodePages[0xc] = new Contacts2CodePage();
        mCodePages[0xd] = new PingCodePage();
        mCodePages[0xe] = new ProvisionCodePage();
        mCodePages[0xf] = new SearchCodePage();
        mCodePages[0x10] = new GALCodePage();
        mCodePages[0x11] = new AirSyncBaseCodePage();
        mCodePages[0x12] = new SettingsCodePage();
        mCodePages[0x13] = new DocumentLibraryCodePage();
        mCodePages[0x14] = new ItemOperationsCodePage();

        WBXML mWBXMLCoder = new WBXML(mCodePages);

        try {
            FileInputStream fis = new FileInputStream(wbxml);
            FileOutputStream fos = new FileOutputStream(output);
            System.out.println("Total file size to read (in bytes) : "+ fis.available());

            mWBXMLCoder.WBXML2XML(fis, fos);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
