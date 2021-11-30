/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testdomain.persistence.jdo;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoInventoryJaxbVm;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import static org.apache.isis.testdomain.persistence.jdo._TestFixtures.setUp3Books;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Transactional
class JdoJaxbTest extends IsisIntegrationTestAbstract {

    @Inject private JaxbService jaxbService;
    @Inject private BookmarkService bookmarkService;

    private JdoInventoryJaxbVm inventoryJaxbVm;

    @BeforeEach
    void setUp() throws SQLException {
        setUp3Books(repositoryService);
        inventoryJaxbVm = factoryService.viewModel(new JdoInventoryJaxbVm());
    }

    @Test
    void inventoryJaxbVm_shouldRoundtripProperly() {

        // assert injection worked initially
        assertEquals("JdoInventoryJaxbVm; 3 products", inventoryJaxbVm.title());

        val books = inventoryJaxbVm.listBooks();
        val favoriteBook = books.get(0);
        assertEquals(3, books.size());
        inventoryJaxbVm.setName("bookstore");
        inventoryJaxbVm.setBooks(books);
        inventoryJaxbVm.setFavoriteBook(favoriteBook);

        // round-trip
        val xml = jaxbService.toXml(inventoryJaxbVm);
        System.err.printf("%s%n", xml);
        val recoveredVm =
                serviceInjector.injectServicesInto(
                jaxbService.fromXml(JdoInventoryJaxbVm.class, xml));

        assertEquals("JdoInventoryJaxbVm; 3 products", recoveredVm.title());
        assertEquals("bookstore", recoveredVm.getName());
        assertEquals(3, recoveredVm.getBooks().size());
        assertEquals(favoriteBook.getName(), recoveredVm.getFavoriteBook().getName());

        assertEquals(
                bookmarkService.bookmarkFor(favoriteBook),
                bookmarkService.bookmarkFor(recoveredVm.getFavoriteBook()));

    }

}
