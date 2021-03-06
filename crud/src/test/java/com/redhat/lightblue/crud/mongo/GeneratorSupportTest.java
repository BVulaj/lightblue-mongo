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
package com.redhat.lightblue.crud.mongo;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.mongodb.DB;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.ValueGenerator;

import com.redhat.lightblue.common.mongo.DBResolver;
import com.redhat.lightblue.common.mongo.MongoDataStore;

import com.redhat.lightblue.extensions.valuegenerator.ValueGeneratorSupport;

public class GeneratorSupportTest extends AbstractMongoCrudTest  {

    private MongoCRUDController controller;

    @Before
    public void setup() throws Exception {
        super.setup();

        final DB dbx = db;
        dbx.createCollection(COLL_NAME, null);

        controller = new MongoCRUDController(null,new DBResolver() {
            @Override
            public DB get(MongoDataStore store) {
                return dbx;
            }
        });
    }

    @Test
    public void testSeq() throws Exception {
        ValueGeneratorSupport ss=controller.getExtensionInstance(ValueGeneratorSupport.class);
        Assert.assertTrue(ss instanceof MongoSequenceSupport);
        EntityMetadata md = getMd("./testMetadata.json");
        ValueGenerator vg=new ValueGenerator(ValueGenerator.ValueGeneratorType.IntSequence);
        vg.getProperties().setProperty("name","test");
        Object value=ss.generateValue(md,vg);
        Assert.assertEquals("1",value.toString());
        value=ss.generateValue(md,vg);
        Assert.assertEquals("2",value.toString());
    }
}
