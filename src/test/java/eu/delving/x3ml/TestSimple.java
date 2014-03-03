package eu.delving.x3ml;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static eu.delving.x3ml.AllTests.*;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class TestSimple {

    @Test
    public void testReadWrite() throws IOException, X3MLException {
        String xml = engine("/simple/simple-x3ml.xml").toString();
        String[] fresh = xml.split("\n");
        List<String> original = IOUtils.readLines(resource("/simple/simple-x3ml.xml"));
        int index = 0;
        for (String originalLine : original) {
            originalLine = originalLine.trim();
            String freshLine = fresh[index].trim();
            Assert.assertEquals("Line " + index, originalLine, freshLine);
            index++;
        }
    }

    @Test
    public void testSimple() throws X3MLException {
        X3MLEngine engine = engine("/simple/simple-x3ml.xml");
        X3MLContext context = engine.execute(document("/simple/simple-input.xml"), policy("/simple/simple-uri-policy.xml"));
        context.write(System.out);
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