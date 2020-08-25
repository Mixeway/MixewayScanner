package io.mixeway.scanner;

import io.mixeway.scanner.integrations.model.SpotbugReportXML;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.tomcat.jni.Directory;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
class ScannerApplicationTests {

    @Test
    void contextLoads() {

     }

}
