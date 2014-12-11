/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.metadata.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.mongodb.BasicDBObject;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BSONParser extends MetadataParser<BSONObject> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BSONParser.class);

    public static final String DELIMITER_ID = "|";

    public BSONParser(Extensions<BSONObject> ex,
                      TypeResolver resolver) {
        super(ex, resolver);
    }

    @Override
    public String getStringProperty(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof String) {
                return (String) x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public BSONObject getObjectProperty(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof BSONObject) {
                return (BSONObject) x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public Object getValueProperty(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof Number
                    || x instanceof String
                    || x instanceof Date
                    || x instanceof Boolean) {
                return x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<String> getStringList(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof List) {
                ArrayList<String> ret = new ArrayList<>();
                for (Object o : (List) x) {
                    ret.add(o.toString());
                }
                return ret;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<BSONObject> getObjectList(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof List) {
                return (List<BSONObject>) x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public BSONObject newNode() {
        return new BasicDBObject();
    }

    @Override
    public Set<String> getChildNames(BSONObject object) {
        return object.keySet();
    }

    @Override
    public void putString(BSONObject object, String name, String value) {
        object.put(name, value);
    }

    @Override
    public void putObject(BSONObject object, String name, Object value) {
        object.put(name, value);
    }

    @Override
    public void putValue(BSONObject object, String name, Object value) {
        object.put(name, value);
    }

    @Override
    public Object newArrayField(BSONObject object, String name) {
        Object ret = new ArrayList();
        object.put(name, ret);
        return ret;
    }

    @Override
    public void addStringToArray(Object array, String value) {
        ((List) array).add(value);
    }

    @Override
    public void addObjectToArray(Object array, Object value) {
        ((List) array).add(value);
    }

    @Override
    public Set<String> findFieldsNotIn(BSONObject elements, Set<String> removeAllFields) {
        final Set<String> strings = new HashSet<>(elements.keySet());
        strings.removeAll(removeAllFields);
        return strings;
    }

    /**
     * Override to set _id appropriately.
     */
    @Override
    public BSONObject convert(EntityInfo info) {
        Error.push("convert[info|bson]");
        try {
            BSONObject doc = super.convert(info);

            // entityInfo._id = {entityInfo.name}|
            putValue(doc, "_id", getStringProperty(doc, "name") + DELIMITER_ID);

            return doc;
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
        } finally {
            Error.pop();
        }
    }

    /**
     * Override to set _id appropriately.
     */
    @Override
    public BSONObject convert(EntitySchema schema) {
        Error.push("convert[info|bson]");
        try {
            BSONObject doc = super.convert(schema);
            putValue(doc, "_id", getStringProperty(doc, "name") + DELIMITER_ID + getStringProperty(getObjectProperty(doc, "version"), "value"));

            return doc;
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
        } finally {
            Error.pop();
        }
    }

    @Override
    public List<BSONObject> getObjectList(BSONObject object) {
        if (object instanceof List) {
            return (List<BSONObject>) object;
        } else {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA);
        }
    }

    @Override
    public Projection parseProjection(BSONObject object) {
        return object == null ? null : Projection.fromJson(toJson(object));
    }

    @Override
    public QueryExpression parseQuery(BSONObject object) {
        return object == null ? null : QueryExpression.fromJson(toJson(object));
    }

    @Override
    public Sort parseSort(BSONObject object) {
        return object == null ? null : Sort.fromJson(toJson(object));
    }

    @Override
    public void putProjection(BSONObject object,String name,Projection p) {
        if(p!=null)
            object.put(name,toBson(p.toJson()));
    }

    @Override
    public void putQuery(BSONObject object,String name,QueryExpression  q) {
        if(q!=null)
            object.put(name,toBson(q.toJson()));
    }

    @Override
    public void putSort(BSONObject object,String name,Sort  s) {
        if(s!=null)
            object.put(name,toBson(s.toJson()));
    }

    private static Object toBson(JsonNode node) {
        if(node instanceof ObjectNode) {
            return toBson((ObjectNode)node);
        } else if(node instanceof ArrayNode) {
            return toBson((ArrayNode)node);
        } else {
            return convertValue(node);
        }
    }

    private static Object toBson(ObjectNode node) {
        BasicDBObject ret=new BasicDBObject();
        for(Iterator<Map.Entry<String,JsonNode>> itr=node.fields();itr.hasNext();) {
            Map.Entry<String,JsonNode> entry=itr.next();
            ret.put(entry.getKey(),toBson(entry.getValue()));
        }
        return ret;
    }

    private static List toBson(ArrayNode node) {
        List list=new ArrayList(node.size());
        for(Iterator<JsonNode> itr=node.elements();itr.hasNext();) {
            JsonNode n=itr.next();
            list.add(toBson(n));
        }
        return list;
    }

    private static Object convertValue(JsonNode node) {
        if(node instanceof BigIntegerNode) {
            return node.bigIntegerValue();
        } else if(node instanceof BooleanNode) {
            return new Boolean(node.asBoolean());
        } else if(node instanceof DecimalNode) {
            return node.decimalValue();
        } else if(node instanceof DoubleNode ||
                  node instanceof FloatNode) {
            return node.asDouble();
        } else if(node instanceof IntNode||
                  node instanceof LongNode||
                  node instanceof ShortNode) {
            return node.asLong();
        } 
        return node.asText();
    }

    private static JsonNode toJson(BSONObject object) {
        try {
            return JsonUtils.json(object.toString());
        } catch (Exception e) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, object.toString());
        }
    }
}
