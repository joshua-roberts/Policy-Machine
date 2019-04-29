package gov.nist.csd.pm.demos.demo;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class ScriptParser {
    public static Script parse(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(Script.class));
        return yaml.load(is);
    }
}
