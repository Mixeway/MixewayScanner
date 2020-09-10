package io.mixeway.scanner;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.mixeway.scanner.utils.Pom;
import io.mixeway.scanner.utils.PomBuild;
import io.mixeway.scanner.utils.PomConfiguration;
import io.mixeway.scanner.utils.PomPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
class ScannerApplicationTests {

    @Test
    void contextLoads() throws IOException, InterruptedException, ParserConfigurationException, SAXException, DocumentException, TransformerException {
        List<String> packagePaths= FileUtils.listFiles(
                new File("/tmp/testjs"),
                new RegexFileFilter("package.json"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getAbsoluteFile).map(file -> file.toString().split("/package.json")[0]).collect(Collectors.toList());
        packagePaths.forEach(s -> System.out.println(s));


    }



}
