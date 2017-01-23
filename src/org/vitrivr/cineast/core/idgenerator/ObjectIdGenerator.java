package org.vitrivr.cineast.core.idgenerator;

import org.vitrivr.cineast.core.data.MediaType;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Classes implementing this interface are intended to generate ID's for multimedia-objects. The classes should
 * be designed in such a way that:
 *
 * - The same instance of the class can be re-used (e.g. for an extraction run).
 * - Two invocations of next() generate different ID's, unless path & type are identical for both invocations AND the class
 *   intends to generate ID's specific for the combination of these values.
 *
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public interface ObjectIdGenerator {
    /**
     * Can be used to initialize a particular ObjectIdGenerator instance by passing
     * a HashMap of named parameters.
     *
     * It remains up to the implementing class to use these parameters. If no initialization
     * is required, an empty HashMap should be provided!
     *
     * @param properties HashMap of named parameters.
     */
    void init(HashMap<String, String> properties);

    /**
     * Generates the next objectId and returns it as a string. That objectId should
     * already contain the MediaType prefix, if the ID type supports it.
     *
     * @param path Path to the file for which an ID should be generated.
     * @param type MediaType of the file for which an ID should be generated.
     * @return Next ID in the sequence.
     */
    String next(Path path, MediaType type);
}
