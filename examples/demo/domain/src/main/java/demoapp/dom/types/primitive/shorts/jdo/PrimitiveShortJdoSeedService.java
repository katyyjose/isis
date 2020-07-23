package demoapp.dom.types.primitive.shorts.jdo;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class PrimitiveShortJdoSeedService extends SeedServiceAbstract {

    public PrimitiveShortJdoSeedService() {
        super(PrimitiveShortJdoEntityFixture::new);
    }

    static class PrimitiveShortJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(PrimitiveShortJdo::new)
                                        .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<Short> samples;
    }
}
