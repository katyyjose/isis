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

package org.apache.isis.core.metamodel.objectmanager.identify;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

/**
 * @since 2.0
 */
public interface ObjectIdentifier {

    Bookmark identifyObject(ManagedObject managedObject);

    // -- HANDLER
    
    public interface Handler extends ChainOfResponsibility.Handler<ManagedObject, Bookmark> {}

    // -- FACTORY
    
    public static ObjectIdentifier createDefault() {
        
        val chainOfHandlers = _Lists.of(
                new ObjectIdentifier_builtinHandlers.GuardAgainstOid(),
                new ObjectIdentifier_builtinHandlers.BookmarkForServices(),
                new ObjectIdentifier_builtinHandlers.BookmarkForValues(),
                new ObjectIdentifier_builtinHandlers.BookmarkForViewModels(),
                new ObjectIdentifier_builtinHandlers.BookmarkForEntities(),
                new ObjectIdentifier_builtinHandlers.BookmarkForOthers());
        
        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);
        
        return managedObject -> chainOfRespo
                .handle(managedObject)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "Could not identify ManagedObject: %s", managedObject));
        
        
    }
    
}
