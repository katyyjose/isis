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
package org.apache.isis.core.metamodel.facets.object.viewmodel;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.HasPostConstructMethodCache;
import org.apache.isis.core.metamodel.methods.MethodByClassMap;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ViewModelFacetFactory
extends FacetFactoryAbstract
implements
    MetaModelRefiner,
    HasPostConstructMethodCache {

    @Inject
    public ViewModelFacetFactory(
            final MetaModelContext mmc,
            final MethodByClassMap postConstructMethodsCache) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        this.postConstructMethodsCache = postConstructMethodsCache;
    }

    /**
     * We simply attach all facets we can find;
     * the {@link #refineProgrammingModel(ProgrammingModel) meta-model validation}
     * will detect if multiple interfaces/annotations have
     * been attached.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {

        val facetHolder = processClassContext.getFacetHolder();
        val type = processClassContext.getCls();
        val postConstructMethodCache = this;

        // (with default precedence)
        FacetUtil
        .addFacetIfPresent(
            // either ViewModel interface
            ViewModelFacetForViewModelInterface.create(type, facetHolder, postConstructMethodCache)
            // or Serializable interface (if any)
            .or(()->ViewModelFacetForSerializableInterface.create(type, facetHolder, postConstructMethodCache)));

        // XmlRootElement annotation (with higher precedence)
        val xmlRootElementIfAny = processClassContext.synthesizeOnType(XmlRootElement.class);
        if(xmlRootElementIfAny.isPresent()) {
            FacetUtil.addFacet(
                    new ViewModelFacetForXmlRootElementAnnotation(
                            facetHolder, postConstructMethodCache));
        }

        // DomainObject(nature=VIEW_MODEL) is managed by the DomainObjectAnnotationFacetFactory
    }



    // //////////////////////////////////////

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec -> {

            objectSpec.lookupFacet(ViewModelFacet.class)
            .map(ViewModelFacet::getSharedFacetRankingElseFail)
            .ifPresent(facetRanking->facetRanking
                    .visitTopRankPairsSemanticDiffering(ViewModelFacet.class, (a, b)->{

                            ValidationFailure.raiseFormatted(
                                    objectSpec,
                                    "%s: has multiple incompatible annotations/interfaces indicating that " +
                                            "it is a recreatable object of some sort (%s and %s)",
                                            objectSpec.getFullIdentifier(),
                                            a.getClass().getSimpleName(),
                                            b.getClass().getSimpleName());


                    }));

        });
    }

    // //////////////////////////////////////

    @Getter(onMethod_ = {@Override})
    private final @NonNull MethodByClassMap postConstructMethodsCache;

}
