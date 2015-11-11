# ActiveSyncWBXMLParser

Microsoft Exchange ActiveSync protocol uses [WBXML](http://www.w3.org/TR/wbxml/) as data format. This project in JAVA help convert WBXML to XML or XML to WBXML.

Project targets only for ActiveSync protocol so ActiveSync codepage is hardcoded. It doesn't fully implement WBXML features since quote from MS documentation:

```
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
```

WBXML is a binary encode format designed to allow for **compact** transmission with no loss of functionality or semantic information. So I use org.xml.sax XML parser instead of DOM to increase parse speed and save memory to build DOM tree.

Feel free to discuss in issue and enhance this project.