package WBXML;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Stack;

import CodePage.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by frankobe on 2/11/2015.
 */


/*
This algorithm uses the following features that are specified in [WBXML1.2]:
- WBXML tokens to encode XML tags
- WBXML code pages to support multiple XML namespaces
- Inline strings
- Opaque data
This algorithm does not use the following features that are specified in [WBXML1.2]:
- String tables
- Entities
- Processing instructions
- Attribute encoding
*/
public class WBXML {
    public static final int WBXML_VERSION11 = 0x01; /* WBXML 1.1 */
    public static final int WBXML_VERSION13 = 0x03; /* WBXML 1.3 */
    public static final int WBXML_UNKNOWN_PI = 0x01; /* Unknown public identifier */
    public static final int WBXML_UTF8_ENCODING = 0x6A; /* UTF-8 encoding */

    private CodePage[] codePages;
    private Stack<String> xmlStack;

    public WBXML(CodePage[] pageList) {
        codePages = pageList;
    }

    public void setCodePage(CodePage[] pageList){
        codePages = pageList;
    }

    public void WBXML2XML(InputStream in, OutputStream out){
        BufferedInputStream istream = new BufferedInputStream(in);
        BufferedOutputStream ostream = new BufferedOutputStream(out);
        CodePage codePage = codePages[0];
        xmlStack = new Stack<String>();

        int majorVer = 0;
        int minorVer = 0;
        int publicIdentifier = 0;
        int charset = 0;

        try{
            int streamByte = istream.read();

            //header info

            // Major version is the high 4 bits + 1
            majorVer = (streamByte >>> 4) + 1;
            // Minor version is the lower 4 bits
            minorVer = (streamByte & 15);
            publicIdentifier = istream.read();
            charset = istream.read();

            //string stable length, not used by ActiveSync
            streamByte = istream.read();

            // Send header info to output
            if (charset == WBXML_UTF8_ENCODING){
                //UTF-8 only for ActiveSync
                String buffer = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
                ostream.write(buffer.getBytes(), 0, buffer.length());
            } else {
                throw new IOException("Not UTF-8 charset");
            }
            processTag(istream, ostream, codePage);
        } catch (IOException ioe){
            ioe.printStackTrace();
            return;
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    private void processTag(BufferedInputStream istream,
                            BufferedOutputStream ostream,
                            CodePage codePage) throws IOException{
        int streamByte = istream.read();

        while (streamByte != -1){
            int attribute = 0;
            String currentNamespace = codePage.getCodePageName();
            String outputBuffer = new String();
            // how to judge the switch_page?
            if ((streamByte & 15) <= 0x4 && ((streamByte >>> 4) % 4) == 0){
                switch (streamByte){
                    case 0x00: /* switch_page */
                        int nextByte = istream.read();
                        if (codePages[nextByte] != null){
                            codePage = codePages[nextByte];
                        }
                        break;
                    case 0x01: /* end */
                        // pop the latest entry and close tag
                        if (!xmlStack.empty()){
                            String tagName = (String) xmlStack.pop();
                            outputBuffer = "</" + tagName + ">";
                        }
                        break;
                    case 0x02: /* entity */
                        break;
                    case 0x03: /* str_i */
                        StringBuffer inlineString = new StringBuffer(1024);
                        int stringByte = 0x00;
                        while ((stringByte = istream.read()) > 0){
                            inlineString.append((char) stringByte);
                        }
                        outputBuffer = inlineString.toString();
                        break;
                    case 0x04: /* literal */
                        break;
                    case 0x40: /* ext_i_0 */
                        break;
                    case 0x41: /* ext_i_1 */
                        break;
                    case 0x42: /* ext_i_2 */
                        break;
                    case 0x43: /* pi */
                        break;
                    case 0x44: /* literal_c */
                        break;
                    case 0x80: /* ext_t_0 */
                        break;
                    case 0x81: /* ext_t_1 */
                        break;
                    case 0x82: /* ext_t_2 */
                        break;
                    case 0x83: /* str_t */
                        break;
                    case 0x84: /* literal_a */
                        break;
                    case 0xc0: /* ext_0 */
                        break;
                    case 0xc1: /* ext_1 */
                        break;
                    case 0xc2: /* ext_2 */
                        break;
                    case 0xc3: /* opaque */
                        byte dataLength = (byte)istream.read();
                        byte[] data = new byte[dataLength];
                        for (int i = 0; i < dataLength; i++){
                            data[i] = (byte)istream.read();
                        }

                        outputBuffer = new String("BASE64");
                        outputBuffer = outputBuffer + Base64.getEncoder().encodeToString(data);
                        break;
                    case 0xc4: /* literal_ac */
                        break;
                }
            }else{
                // Process tokens from codepage
                String elementName = new String();

                // if bit 6 is set, there is content
                byte content = (byte)(streamByte & 64);

                if (content > 0){
                    //remove content flag
                    streamByte = (streamByte ^ 64);
                }
                // if bit 7 is set, there are attribute
                attribute = (streamByte & 128);
                if (attribute > 0){
                    // remove attribute flag
                    streamByte = (streamByte ^ 128);
                }
                elementName = codePage.getCodePageString(streamByte);

                outputBuffer = "<" + elementName;
                //no attribute needed
//                if (xmlStack.empty()) {
//                    outputBuffer = outputBuffer + " xmlns=\""+currentNamespace+"\"";
//                }

                if (content > 0){
                    xmlStack.push(elementName);
                }
                if (content > 0 && attribute == 0){
                    outputBuffer = outputBuffer + ">";
                } else if (content == 0 && attribute == 0){
                    outputBuffer = outputBuffer + "/>";
                }
            }

            if (outputBuffer.length() > 0) {
                ostream.write(outputBuffer.getBytes(), 0, outputBuffer.length());
                ostream.flush();
            }
            streamByte = istream.read();
        }
    }

    public void XML2WBXML(InputStream in, OutputStream out) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();

            XMLHandler handler = new XMLHandler(out);

            xr.setContentHandler(handler);

            xr.parse(new InputSource(in));
        } catch (ParserConfigurationException pce){
            pce.printStackTrace();
        } catch (SAXException se){
            se.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public class XMLHandler extends DefaultHandler {
        private CodePage codePage = codePages[0];
        private BufferedOutputStream ostream;
        private ArrayList<Integer> pendingBuffer;

        public XMLHandler(OutputStream out){
            ostream = new BufferedOutputStream(out);
            pendingBuffer = new ArrayList<Integer>();
        }

        @Override
        public void startDocument() throws SAXException {
            try{
                //version
                ostream.write(WBXML_VERSION13);
                //Unknown public identifier
                ostream.write(WBXML_UNKNOWN_PI);
                //Charset UTF-8
                ostream.write(WBXML_UTF8_ENCODING);
                //string table not used
                ostream.write(0x00);
            } catch (IOException ioe){
                throw new SAXException("IOException writing header: "+ ioe);
            }
        }

        @Override
        public void endDocument() throws SAXException {
            if (pendingBuffer.size() > 0){
                for (Integer i: pendingBuffer) {
                    try {
                        ostream.write(i.byteValue());
                    } catch (IOException ioe){
                        throw new SAXException("IOException in writing buffer: "+ ioe);
                    }

                }

                pendingBuffer = new ArrayList<Integer>();
            }

            //Flush stream
            try {
                ostream.flush();
            } catch (IOException ioe){
                throw new SAXException("IOException flushing output stream: " + ioe);
            }
        }

        @Override
        public void startElement (String namespaceURI, String localName,
                                  String qName, Attributes atts) throws SAXException {

            //active sync doesn't use Attribute encoding
            if (namespaceURI.endsWith(":")) {
                namespaceURI = namespaceURI.substring(0, namespaceURI.length() - 1);
            }

            if (localName.equals("")) {
                if (!qName.equals("")) {
                    localName = qName.substring(qName.lastIndexOf(":")+1, qName.length());
                }
            }

            if (namespaceURI.equals("")){
                if(!qName.equals("")){
                    if (qName.lastIndexOf(":") == -1) {
                        namespaceURI = codePage.getCodePageName();
                    }else{
                        namespaceURI = qName.substring(0, qName.lastIndexOf(":"));
                    }
                }
            }
            int startToken = 0;

            if (pendingBuffer.size() > 0) {
                Integer tagByte = pendingBuffer.get(0);
                // 6 bit for content
                tagByte |= 64;
                pendingBuffer.set(0, tagByte);

                for (Integer i : pendingBuffer) {
                    try{
                        ostream.write(i.byteValue());
                    } catch (IOException ioe){
                        throw new SAXException("IOException writing buffer: " + ioe);
                    }
                }

                pendingBuffer = new ArrayList<Integer>();
            }

            if (!codePage.getCodePageName().equals(namespaceURI)) {
                for (int i=0; i < codePages.length; i++){
                    if (codePages[i].getCodePageName().equals(namespaceURI)){
                        codePage = codePages[i];
                        try{
                            ostream.write(0x00);
                            ostream.write(codePage.getCodePageIndex());
                        }catch (IOException ioe) {
                            throw new SAXException("IOException writing page change: " + ioe);
                        }
                        i = codePages.length;
                    }
                }
            }

            startToken = codePage.getCodePageToken(localName);
            pendingBuffer.add(startToken);
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) {
            pendingBuffer.add(0x01);
        }

        @Override
        public void characters(char ch[], int start, int length) {
            //remove whitespace
            String value = new String(ch, start, length).trim();

            if (value.length() == 0){
                return; //ignore white space
            }

            String hexString = new String();

            if (length > 6) {
                hexString = new String(ch, start, 6);
            }

            if (pendingBuffer.size() > 0){
                int tagByte = pendingBuffer.get(0);
                tagByte |= 64;
                pendingBuffer.set(0,tagByte);
            }

            if (hexString.equals("BASE64")) {
                String encodedData = new String(ch, start, length);
                byte[] decodedData = Base64.getDecoder().decode(encodedData);

                // add the tag saying opaque data follows
                pendingBuffer.add(codePage.getWbxmlToken("opaque"));

                // add the length of opaque data
                pendingBuffer.add(decodedData.length);
                for (int i = 0; i < decodedData.length; i++){
                    pendingBuffer.add((int) ch[i]);
                }

            }else{
                // add the tag saying inline string follows
                pendingBuffer.add(codePage.getWbxmlToken("str_i"));

                // add the inline string content
                for (int i = start; i < start+length; i++){
                    if (ch[i] != '\n') {
                        pendingBuffer.add((int) ch[i]);
                    }
                }

            }

            // end string with null terminator
            pendingBuffer.add(0x00);
        }
    }
}

