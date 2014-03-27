package eu.delving.x3ml;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static eu.delving.x3ml.AllTests.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class TestBase {
    private final Logger log = Logger.getLogger(getClass());

    private void log(String title, String[] list) {
        log.info(title);
        int count = 0;
        for (String line : list) {
            log.info((count++) + " ) " + line);
        }
    }

    @Test
    public void testReadWrite() throws IOException, X3MLException {
        String xml = engine("/base/base.x3ml").toString();
        String[] lines = xml.split("\n");
        List<String> serialized = new ArrayList<String>();
        List<String> originalLines = IOUtils.readLines(resource("/base/base.x3ml"));
        List<String> original = new ArrayList<String>();
        int index = 0;
        for (String originalLine : originalLines) {
            originalLine = originalLine.trim();
            if (originalLine.startsWith("<!--")) continue;
            serialized.add(lines[index].trim());
            original.add(originalLine);
            index++;
        }
        Assert.assertEquals("Mismatch", StringUtils.join(original, "\n"), StringUtils.join(serialized, "\n"));
    }

    @Test
    public void testSimple() throws X3MLException {
        X3MLEngine engine = engine("/base/base.x3ml");
        X3MLContext context = engine.execute(document("/base/base.xml"), policy("/base/base-gen-policy.xml"));
        String[] mappingResult = context.toStringArray();
        String[] expectedResult = AllTests.xmlToNTriples("/base/base-rdf.xml");
//        log("Expected", expectedResult);
//        log("Actual", mappingResult);
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
        System.out.println(StringUtils.join(diff, "\n"));
    }
}

// to ignore stuff:
//XStream xStream = new XStream() {
//    @Override
//    protected MapperWrapper wrapMapper(MapperWrapper next) {
//        return new MapperWrapper(next) {
//            @Override
//            public boolean shouldSerializeMember(Class definedIn, String fieldName) {
//                if (definedIn == Object.class) {
//                    try {
//                        return this.realClass(fieldName) != null;
//                    } catch(Exception e) {
//                        return false;
//                    }
//                } else {
//                    return super.shouldSerializeMember(definedIn, fieldName);
//                }
//            }
//        };
//    }
//};