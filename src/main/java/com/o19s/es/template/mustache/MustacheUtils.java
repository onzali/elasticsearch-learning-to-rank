/*
 * Copyright [2017] Wikimedia Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.o19s.es.template.mustache;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.elasticsearch.common.io.FastStringReader;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.StringWriter;
import java.util.Map;

public class MustacheUtils {
    public static final String TEMPLATE_LANGUAGE = "mustache";
    private static final Logger logger = ESLoggerFactory.getLogger(MustacheUtils.class);
    /**
     * We store templates internally always as json
     */
    private static final CustomMustacheFactory FACTORY = new CustomMustacheFactory();

    public static Mustache compile(String name, String template) {
        // Don't use compile(String name) to avoid caching in the factory
        try {
            return FACTORY.compile(new FastStringReader(template), name);
        } catch (MustacheException me) {
            throw new IllegalArgumentException(me.getMessage(), me);
        }
    }

    public static String execute(Mustache template, Map<String, Object> params, int approxLength) {
        final StringWriter writer = new StringWriter(approxLength);
        try {
            // no need to guard with the SecurityManager we only have Maps here so no reflection
            // will be performed.
            template.execute(writer, params);
            ESLoggerFactory.getLogger("debug").info( " size diff: {}, {}", writer.getBuffer().length(), approxLength );
        } catch (Exception e) {
            logger.error((Supplier<?>) () -> new ParameterizedMessage("Error running {}", template), e);
            throw new IllegalArgumentException("Error running " + template, e);
        }
        return writer.toString();

    }
}
